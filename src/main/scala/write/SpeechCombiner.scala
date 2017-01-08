package write

import com.github.tototoshi.csv._
import java.io.File
import java.io.PrintWriter
import net.bramp.ffmpeg._
import net.bramp.ffmpeg.builder._

class SpeechCombiner(inputVidPath: String, inputTransPath: String, inputTextFilePath: String){

	var corpus: Map[String, SpokenWord] = Map()


	var target: List[String] = List()
	def loadTranscriptions() : Unit =  {

		val targetwords = scala.io.Source.fromFile(inputTextFilePath).mkString.toLowerCase()
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
					val fileName = inputVidPath + "/" + file.getName().substring(0, file.getName.indexOf('.')) + ".avi"
				    val word = v(0).toLowerCase
				    val loadedWord = new SpokenWord(word, v(1), v(2), v(3).toDouble, fileName)
				    if(corpus.contains(word)){
						if(corpus(word).conf < v(3).toDouble){
							corpus = corpus + (word -> loadedWord)
						}
				    }else{
				 	corpus = corpus + (word -> loadedWord)
					}
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
		val pw = new PrintWriter(new File("temp", "combination.txt" ))
		for(word <- target){
			corpus(word).clipWord()
			pw.write("file '" + word + ".mp4'\n")
		}
		pw.close()

		val ffmpeg = new FFmpeg()
		val ffprobe = new FFprobe()
		val builder = new FFmpegBuilder()
		val outName ="combination.mp4"
		builder.addExtraArgs("-f", "concat").setInput("temp/combination.txt").overrideOutputFiles(true).addOutput(outName).done()
		val executor = new FFmpegExecutor(ffmpeg, ffprobe); 
		executor.createJob(builder).run();

	}



}