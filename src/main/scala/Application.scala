
import write._
import transcribe._
object Application{
	val usageString = "speechcombinations [--transcribe inputVideoDirectory outputTranscriptionDirectory] [--write inputVideoDirectory  inputTranscriptionsDirectory inputTargetSpeech]"

	def main(args: Array[String]) : Unit ={
		
		if (args.length == 0){
			println(usageString);
			System.exit(0)
		}

		val list = args.toList

		def getOptions(map : Map[Symbol, List[String]], list: List[String]): Map[Symbol,List[String]] = {
			def isArg(s: String)  : Boolean = {s.length > 2 && s(0) == '-' && s(1) == '-'}

			list match {
				case Nil => map
				case "--transcribe" :: value1 :: value2 :: tail if(!isArg(value1) && !isArg(value2)) => getOptions(map ++ Map('transcribe -> List(value1, value2)), tail)
				case "--write" :: value1 :: value2 :: value3 :: tail if(!isArg(value1) && !isArg(value2) && !isArg(value3)) => getOptions(map ++ Map('write -> List(value1, value2, value3)), tail)
				case "--a" :: tail => getOptions(map ++ Map('a -> List()), tail)
				case "--s" :: tail => getOptions(map ++ Map('s -> List()), tail)
				case whatever => println(usageString)
								 map
				}
		}

		val options = getOptions(Map(), list)

		val transargs = options.get('transcribe)

		val writeargs = options.get('write)

		if(options.contains('transcribe)){
			val transargs = options('transcribe)
			val transcriber = new TranscribeVideos(transargs(0), transargs(1))
			transcriber.run(options.contains('a), options.contains('s));
		}

		if(options.contains('write)){
			val writeArgs = options('write)
			val combiner = new SpeechCombiner(writeArgs(0), writeArgs(1), writeArgs(2))
			combiner.loadTranscriptions()
			if(combiner.validTranscription()){
				combiner.createVideo()
			}
		}

	}
}

