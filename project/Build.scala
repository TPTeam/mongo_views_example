import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "mongo-views-examples"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "mongo-views-plugin" % "mongo-views-plugin_2.10" % "0.5"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
		  
		  
      resolvers += Resolver.url(
          "TPTeam Snapshots", url(
          "http://tpteam.github.io/snapshots/"
      ))(Resolver.ivyStylePatterns),
      
      resolvers += Resolver.url("TPTeam Repository", url(
          "http://tpteam.github.io/releases/"
      ))(Resolver.ivyStylePatterns)
  )
  //"file://..../play-2.2.1/repository/local"

}