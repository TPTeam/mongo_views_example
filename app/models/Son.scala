package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._
import models.persistance._
import com.mongodb.casbah.Imports.ObjectId
import RefObj._

case class Son (
    id: ObjectId = new ObjectId,
    name: String,
    fa: Option[Reference[Father]] = None
    ) extends ModelObj(id) {
  val singleton = Son
  
  def updateFather(rfa: Option[Reference[Father]]) =
    singleton.update(this.id,
        Son(
            this.id,
            this.name,
            rfa
            )
        )    
  
}

object Son extends PersistanceCompanion[Son] with ReverseRefPersistanceCompanion[Son, Father] {  
  val collectionName = "sons"
  
  def referenceChanged = (ogp, rel) => {
    (ogp) match {
      case None => //Delete
        delete(rel.id)
      case Some(fa) => //Update or nothing
        findOneById(rel.id).map(x =>
        	x.updateFather(Some(fa))
        )   
    } 
  }
  
  override def create(obj: Son) = {
    if (obj.fa.isDefined)
    	Father.addTo(List(obj), obj.fa.get)
    super.create(obj)
  }
  
  override def delete(id: ObjectId) = {
    findOneById(id).map(fa => {
      Father.removeFrom(List(fa), Father.findAll.toList)
      false
    })
    super.delete(id)  
  }
  
  override def update(id: ObjectId, obj: Son) = {
    Father.removeFrom(List(obj), Father.findAll.toList)
    val oldObj = 
      findOneById(id).get
      
    if (obj.fa.isDefined)
    	Father.addTo(List(obj), obj.fa.get)
    
    super.update(id, obj)
  }
  
}