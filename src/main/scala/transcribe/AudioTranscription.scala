import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import com.github.tototoshi.csv._
import java.io.File
import scala.collection.JavaConversions._

class AudioTranscription(vidName: String, result: SpeechResults){

	def saveTranscription(): Unit = {
		val name = vidName.replace('.', '_');
		val file = new File(name);

		val writer = CSVWriter.open(file);

		var i = 1;

		for(transcript <- result.getResults().toList){
			println(i)
			println(transcript)
			i = i + 1;
		}
	}


}