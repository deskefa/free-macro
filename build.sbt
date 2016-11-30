organization := "me.bica"
name := "cats-free-macro"
version := "0.0.1-SNAPSHOT"

val scala_version = "2.11.8"
scalaVersion := scala_version

libraryDependencies ++= Seq(
	"org.scala-lang" % "scala-library" % scala_version,
	"org.scala-lang" % "scala-reflect" % scala_version,
	"org.typelevel" %% "cats-free" % "0.6.0",
	"org.typelevel" %% "cats" % "0.6.0"
)

scalacOptions := Seq("-unchecked", "-deprecation","-feature","-Xplugin-require:macroparadise")
resolvers ++= Seq(
	Resolver.mavenLocal
)
publishMavenStyle := true

autoCompilerPlugins := true
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.0" cross CrossVersion.binary)
addCompilerPlugin("com.milessabin" % s"si2712fix-plugin_$scala_version" % "1.2.0") 

//EclipseKeys.withSource := true
//EclipseKeys.withJavadoc := false
//EclipseKeys.withBundledScalaContainers := false