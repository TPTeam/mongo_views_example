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

case class GranPa (
    id: ObjectId = new ObjectId,
    name: String,
    sons: List[Reference[Father]] = List()
    ) extends ModelObj(id) {
  val singleton = GranPa
}

object GranPa extends PersistanceCompanion[GranPa] with DirectRefPersistanceCompanion[GranPa, Father] {
  val collectionName = "granpas"
    
  override def delete(id: ObjectId) = {
    findOneById(id).map(gp => {
      gp.sons.foreach(x => Father.referenceChanged(None,x))
      false
    })
    super.delete(id)
  }
    
  override def create(obj: GranPa) = {
    removeFrom(obj.sons,findAll.filterNot(x => x.id==obj.id).toList)
    obj.sons.map(x => Father.referenceChanged(Some(obj),x))
    super.create(obj)
  }
  
  def removeFrom(toBeRemoved: List[Reference[Father]], from: List[GranPa]): Unit = {
    def _update(id: ObjectId, obj: GranPa) = {
    	super.update(id,obj)
    }
    
    val dup = 
      for (
          gp <- from
          ) {
        val newSons = 
          gp.sons.filterNot(e => toBeRemoved.contains(e))
        
          
        if (newSons.length!=gp.sons.length) {
          _update(gp.id,
              GranPa(
                  gp.id,
                  gp.name,
                  newSons
                  )
              )
        }
      }
  }
  
  def addTo(toBeAdded: List[Reference[Father]], to: GranPa): Unit = {
    def _update(id: ObjectId, obj: GranPa) = {
    	super.update(id,obj)
    }
    
    _update(to.id,
            GranPa(
                to.id,
                to.name,
                to.sons ++ toBeAdded
                )
        )
  }
  
  override def update(id: ObjectId, obj: GranPa) = {
    removeFrom(obj.sons,findAll.filterNot(x => x.id==obj.id).toList)
    val olds = 
    	findOneById(id).get.sons
    val news =
    	obj.sons

    for (
        o <- olds)
    if (!news.exists(x => x.id==o.id)) //delete old
    	  Father.referenceChanged(None,o)
    for (
        n <- news)
    if (!olds.exists(x => x.id == n.id)) //add new 
    	Father.referenceChanged(Some(Reference(id)),n)
   
    super.update(id,obj)
  }
}