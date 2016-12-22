package transcribe

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import java.io.File
import com.github.tototoshi.csv._
import scala.collection.JavaConversions._

object TranscribeVideos {
	var outputDirPath: String = ""



	def main(args: Array[String]) : Unit ={
		if(args.length < 2){
			println("TranscribeVideos vidDirInPath transcribeDirOutPath")
			return;
		}
		val inputDirPath = args(0)
		outputDirPath = args(1) + "/"

		val inputDir = new File(inputDirPath)

		val audioFiles =  inputDir.listFiles.filter(_.getName.endsWith(".wav"))


		val auther = new ServiceAuth()


		val service = auther.getAuthenticatedService()

		val options = new RecognizeOptions.Builder().contentType("audio/wav").timestamps(true).wordAlternativesThreshold(0.01).continuous(false).build()
		val a = System.currentTimeMillis;
		for(file <- audioFiles){
			val result : SpeechResults = service.recognize(file, options).execute();
			val transcription = saveTranscription(file.getName, result);
		}

		// if(audioFiles.length == 0){
		// 	println("No files found")
		// }

		}



		def saveTranscription(vidName: String, result: SpeechResults): Unit = {
			val name = vidName.replace('.', '_');

			val outdir = new File(outputDirPath);
			outdir.mkdir();

			val file = new File(outdir, name);

			val writer = CSVWriter.open(file);

			for(transcript <- result.getResults().toList){
				for(alt <- transcript.getAlternatives().toList){
					for(t <- alt.getTimestamps().toList){
					val word = t.getWord();
					val start = t.getStartTime();
					val end = t.getEndTime();
					writer.writeRow(List(word, start.toString(), end.toString(), alt.getConfidence));
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