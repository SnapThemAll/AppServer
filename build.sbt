name := "SteepUpServer"

version := "1.0"

lazy val steepupserver = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xlint" // Enable recommended additional warnings.
)

libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    specs2                   % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test",
    "net.codingwell"         %% "scala-guice" % "4.1.0",
    "com.iheart"             %% "ficus" % "1.3.0", //lightweight companion to Typesafe config that makes it more Scala-friendly
    //Silhouette
    "com.mohiva"        %% "play-silhouette"                 % "4.0.0",
    "com.mohiva"        %% "play-silhouette-password-bcrypt" % "4.0.0",
    "com.mohiva"        %% "play-silhouette-crypto-jca"      % "4.0.0",
    "com.mohiva"        %% "play-silhouette-persistence"     % "4.0.0",
    "com.mohiva"        %% "play-silhouette-testkit"         % "4.0.0" % "test",
    "org.reactivemongo" %% "play2-reactivemongo"             % "0.12.0",
    //breeze
    "org.scalanlp" %% "breeze"         % "0.12",
    "org.scalanlp" %% "breeze-natives" % "0.12",
    "org.scalanlp" %% "breeze-viz" % "0.12",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers ++= Seq(
    "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    //breeze
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
    Resolver.jcenterRepo //For ficus
)

jacoco.settings
jacoco.excludes in jacoco.Config := Seq("views*", "*Routes*", "controllers*routes*", "controllers*Reverse*", "controllers*javascript*", "controller*ref*", "concurrent*")
