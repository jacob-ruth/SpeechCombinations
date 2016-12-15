name := "VideoClipper"

version := "1.0"

scalaVersion := "2.11.0"

mainClass in Compile := Some("transcribe.TranscribeVideos")

libraryDependencies += "com.twitter" % "finagle-http2_2.11" % "6.40.0"


libraryDependencies += "io.humble" % "humble-video-all" % "0.2.1"

libraryDependencies += "com.ibm.watson.developer_cloud" % "speech-to-text" % "3.5.2"
