addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.8")
addSbtPlugin("com.github.sbt" % "sbt-license-report" % "1.5.0")

// In order to import proper version of com.google.protobuf.ByteString we need to add this dependency
libraryDependencies ++= Seq("com.thesamet.scalapb" %% "compilerplugin" % "0.11.13")
