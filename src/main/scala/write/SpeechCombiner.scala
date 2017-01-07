package write

import com.github.tototoshi.csv._
import java.io.File

class SpeechCombiner(inputVidPath: String, inputTransPath: String, inputTextFilePath: String){

	var corpus: Map[String, SpokenWord] = Map()


	var target: List[String] = List()
	def loadTranscriptions() : Unit =  {

		val targetwords = scala.io.Source.fromFile(inputTextFilePath).mkString
		target = targetwords.split("[\\p{Punct}\\s]+").toList



		val inputTransDir = new File(inputTransPath)
		val transcriptionFiles = inputTransDir.listFiles.filter(_.getName.endsWith(".csv"))
		println(transcriptionFiles.size)
		for(file <- transcriptionFiles){
			val reader = CSVReader.open(file)
			val values = reader.all()
			reader.close()

			for(v <- values){
				if(v.size != 4){
					println("Problem! Skipping line")
				}else{
				    val loadedWord = new SpokenWord(v(0), v(1), v(2), v(3).toDouble, file.getName())

				 	corpus = corpus + (v(0) -> loadedWord)
				}
			}

		}
		println(corpus.size)
	}

	def validTranscription() : Boolean = {
		var missingWords = List[String]()
		for(i <- target){
			if(!corpus.contains(i)){
				missingWords = i :: missingWords
			}
		}

		if(missingWords.size != 0){
			println("Unable to continue!  Missing words:")
			println(missingWords.mkString(" "))
		}
		return missingWords.size == 0


	}

	def createVideo(): Unit = {
		println("We can make it!")
	}



}