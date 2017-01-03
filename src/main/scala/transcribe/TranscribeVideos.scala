package transcribe

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import java.io.File
import com.github.tototoshi.csv._
import scala.collection.JavaConversions._
import com.twitter.util.Future
import net.bramp.ffmpeg._
import net.bramp.ffmpeg.builder._

class TranscribeVideos(inputDirPath: String, outputDirPath: String) {

	def run() : Unit ={
		val inputDir = new File(inputDirPath)

		val videoFiles = inputDir.listFiles.filter(_.getName.endsWith(".mp4"))

		val ffmpeg = new FFmpeg()
		val ffprobe = new FFprobe()
		for(file <- videoFiles){
			val builder = new FFmpegBuilder()
			val outName = file.getName.substring(0, file.getName.indexOf('.')) + ".flac"
			builder.setInput(inputDirPath + "/" + file.getName).addOutput(inputDirPath + "/" + outName).setAudioSampleRate(16000).setAudioChannels(2).done()
			val executor = new FFmpegExecutor(ffmpeg, ffprobe);
			executor.createJob(builder).run();
		}


		val audioFiles =  inputDir.listFiles.filter(_.getName.endsWith(".flac"))
		val auther = new ServiceAuth()
		val service = auther.getAuthenticatedService()
		val options = new RecognizeOptions.Builder().contentType("audio/flac").timestamps(true).wordAlternativesThreshold(0.01).continuous(true).inactivityTimeout(-1).build()
		for(file <- audioFiles){
			val futureResult : Future[SpeechResults] = Future.value(service.recognize(file, options).execute());
			futureResult onSuccess{ result => 
				saveTranscription(file.getName, result);
			}
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