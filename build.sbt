name := "SpeechCombinations"

version := "1.0"

scalaVersion := "2.11.0"

mainClass in Compile := Some("Application")

libraryDependencies += "com.twitter" % "finagle-http2_2.11" % "6.40.0"

libraryDependencies += "io.humble" % "humble-video-all" % "0.2.1"

libraryDependencies += "com.ibm.watson.developer_cloud" % "speech-to-text" % "3.5.2"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.4"

libraryDependencies += "net.bramp.ffmpeg" % "ffmpeg" % "0.6.1"
