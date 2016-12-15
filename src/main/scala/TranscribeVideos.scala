package transcribe

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import java.io.File

object TranscribeVideoes {

	def main(args: Array[String]) : Unit ={
		if(args.length < 2){
			println("TranscribeVideos vidDirInPath transcribeDirOutPath")
			return;
		}
		val inputDirPath = args(0)
		val outputDirPath = args(1)

		val inputDir = new File(inputDirPath)

		val audioFiles =  inputDir.listFiles.filter(_.getName.endsWith(".wav"))


		val auther = new ServiceAuth()


		val service = auther.getAuthenticatedService()

		val options = new RecognizeOptions.Builder().contentType("audio/wav").timestamps(true).wordAlternativesThreshold(0.4).continuous(false).build()
		val a = System.currentTimeMillis;
		for(file <- audioFiles){
			val result = service.recognize(file, options).execute();
			println(result)
		}

		println("Total Time in Requests: " + (System.currentTimeMillis - a))
		// if(audioFiles.length == 0){
		// 	println("No files found")
		// }

		}
}