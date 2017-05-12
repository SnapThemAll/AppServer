logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.2.0")

addSbtPlugin("org.bytedeco" % "sbt-javacv" % "1.14")