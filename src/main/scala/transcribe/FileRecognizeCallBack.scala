package transcribe

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback
import java.io.File
import scala.collection.JavaConversions._
import com.github.tototoshi.csv._

class FileRecognizeCallback(f: File, outPath: String, rest: List[File], vidTranscriber: TranscribeVideos) extends BaseRecognizeCallback{

			override def onTranscription(speechResults: SpeechResults){
				println("Transcription for " + f.getName + " finished")
				saveTranscription(f.getName, speechResults)
				println("Transcription for " + f.getName + " saved, sending next request")
				vidTranscriber.sendNext(rest);
			}

			override def onDisconnected(){
				println("Disconnected! Skipping")
				vidTranscriber.sendNext(rest);
			}

			def saveTranscription(vidName: String, result: SpeechResults): Unit = {
			val name = vidName.substring(0, vidName.indexOf('.')) + ".csv"
			val outdir = new File(outPath + "/")
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