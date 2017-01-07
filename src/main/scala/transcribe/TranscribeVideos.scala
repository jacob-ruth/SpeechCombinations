package transcribe

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback
import java.io.File
import java.io.FileInputStream
import scala.collection.JavaConversions._
import com.twitter.util.Future
import net.bramp.ffmpeg._
import net.bramp.ffmpeg.builder._
import com.github.tototoshi.csv._

class TranscribeVideos(inputDirPath: String, outputDirPath: String) {

	def run(transcode : Boolean, sync: Boolean) : Unit ={
		val inputDir = new File(inputDirPath)

		val videoFiles = inputDir.listFiles()
		if(transcode){
			val ffmpeg = new FFmpeg()
			val ffprobe = new FFprobe()
			for(file <- videoFiles){
				val builder = new FFmpegBuilder()
				val outName = file.getName.substring(0, file.getName.indexOf('.')) + ".flac"
				builder.setInput(inputDirPath + "/" + file.getName).addOutput(inputDirPath + "/" + outName).setAudioSampleRate(16000).setAudioChannels(2).done()
				val executor = new FFmpegExecutor(ffmpeg, ffprobe);
				executor.createJob(builder).run();
			}
		}

		val audioFiles =  inputDir.listFiles.filter(_.getName.endsWith(".flac")).toList
		println("Found " + audioFiles.size + " videos to upload")

		if(!sync){
			sendAsync(audioFiles)
		}else{
			sendNext(audioFiles)
		}

		// for(file <- audioFiles){
		// 	val callback = new FileRecognizeCallback(file, outputDirPath)
		// 	try{
		// 		service.recognizeUsingWebSocket(new FileInputStream(file), options, callback)
		// 	}
		// 	catch{
		// 		case _:Throwable => println("Websocket didn't work")
		// 	}
		// }
		}


	def sendAsync(files: List[File]) : Unit = {
		val auther = new ServiceAuth()
		val service = auther.getAuthenticatedService()
		val options = new RecognizeOptions.Builder().contentType("audio/flac").timestamps(true).wordAlternativesThreshold(0.01).continuous(true).profanityFilter(false).inactivityTimeout(-1).build()
		for(file <- files){
			val futureResult : Future[SpeechResults] = Future.value(service.recognize(file, options).execute());
			futureResult onSuccess{ result => 
				saveTranscription(file.getName, result);
			}
		}


	}	
	def sendNext(files: List[File]) : Unit = {
			files match {
				case file :: tail => println("Uploading " + file.getName + " now")
									val auther = new ServiceAuth()
									val service = auther.getAuthenticatedService()
									val options = new RecognizeOptions.Builder().contentType("audio/flac").timestamps(true).wordAlternativesThreshold(0.01).continuous(true).profanityFilter(false).inactivityTimeout(-1).build()
									val callback = new FileRecognizeCallback(file, outputDirPath, tail, this)
									try{
										service.recognizeUsingWebSocket(new FileInputStream(file), options, callback)
									}
									catch{
										case _:Throwable => println("Websocket didn't work")
									}
				case Nil => println("Finished all Files") 
							return
			}
	}

	def saveTranscription(vidName: String, result: SpeechResults): Unit = {
			val name = vidName.substring(0, vidName.indexOf('.')) + ".csv"
			val outdir = new File(outputDirPath + "/")
			outdir.mkdir()
			val file = new File(outdir, name)
			val writer = CSVWriter.open(file)
			for(transcript <- result.getResults().toList){
				for(alt <- transcript.getAlternatives().toList){
					for(t <- alt.getTimestamps().toList){	
					val word = t.getWord()
					val start = t.getStartTime()
					val end = t.getEndTime()
					writer.writeRow(List(word, start.toString(), end.toString(), alt.getConfidence))
					}
				}
				for(alt <- transcript.getWordAlternatives().toList){
					val start = alt.getStartTime();
					val end = alt.getEndTime();
					for(word <- alt.getAlternatives.toList){
						writer.writeRow(List(word.getWord(), start.toString(), end.toString(), word.getConfidence()));
					}
				}
			}
			writer.close();
	}


}