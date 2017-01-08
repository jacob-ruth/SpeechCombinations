package write

import net.bramp.ffmpeg._
import net.bramp.ffmpeg.builder._

class SpokenWord(word: String, startTime: String, endTime: String,  confidence: Double, vidFile: String){

	def conf = confidence
	//creates a video clip for the chosen word
	def clipWord(): Unit = {
			val ffmpeg = new FFmpeg()
			val ffprobe = new FFprobe()
			val builder = new FFmpegBuilder()
			val outName = word + ".mp4"

			builder.setInput(vidFile).overrideOutputFiles(true).addOutput("temp/" + word +".mp4").addExtraArgs("-ss", startTime, "-t" , (endTime.toFloat - startTime.toFloat).toString).setVideoCodec("libx264") .setVideoFrameRate(24, 1).setVideoResolution(640, 480).done()
			val executor = new FFmpegExecutor(ffmpeg, ffprobe); 
			executor.createJob(builder).run();
	}

}