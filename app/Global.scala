import play.api._
import models._
import org.bson.types.ObjectId

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    
    
    Father.findAll().toList.foreach(x => Father.delete(x.id))
    GranPa.findAll().toList.foreach(x => GranPa.delete(x.id))
    Son.findAll().toList.foreach(x => Son.delete(x.id))

  }
}