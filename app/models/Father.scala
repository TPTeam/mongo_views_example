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

case class Father (
    id: ObjectId = new ObjectId,
    name: String,
    gp: Option[Reference[GranPa]] = None,
    sons: List[Reference[Son]] = List()
    ) extends ModelObj(id) {
  val x : PersistanceCompanion[Father] = Father
  
  val singleton: PersistanceCompanion[Father] = x
  
  def updateGranpa(rgp: Option[Reference[GranPa]]) = {
    singleton.update(this.id,
        Father(
            this.id,
            this.name,
            rgp,
            this.sons
            )
        )
  }
}

object Father extends PersistanceCompanion[Father] 
			  with ReverseRefPersistanceCompanion[Father,GranPa] 
			  with DirectRefPersistanceCompanion[Father, Son] {
  val collectionName = "fathers"
    
  def removeFrom(toBeRemoved: List[Reference[Son]], from: List[Father]): Unit = {
     
    val dup = 
      for (
          fa <- from          
          ) {
        val newSons = 
          fa.sons.filterNot(e => toBeRemoved.contains(e))
        
        if (newSons.length!=fa.sons.length) {
          _update(fa.id,
              Father(
                  fa.id,
                  fa.name,
                  fa.gp,
                  newSons
                  )
              )
        }
      }
  }
  
  def _update(id: ObjectId, obj: Father) = {
    	super.update(id,obj)
    }
  
  def addTo(toBeAdded: List[Reference[Son]], to: Father): Unit = {
    
    _update(to.id,
            Father(
                to.id,
                to.name,
                to.gp,
                to.sons ++ toBeAdded
                )
        )
  }
    
  def referenceChanged = (ogp, rel) => {
    (ogp) match {
      case None => //Delete
        delete(rel.id)
      case Some(gp) => //Update or nothing
        findOneById(rel.id).map(x => {
        	x.updateGranpa(Some(gp))
        })   
    } 
  }
  
  override def create(obj: Father) = {
    removeFrom(obj.sons,findAll.filterNot(x => x.id==obj.id).toList)
    obj.sons.map(x => Son.referenceChanged(Some(obj),x))
    super.create(obj)
  }
  
  override def delete(id: ObjectId) = {
    findOneById(id).map(fa => {
      GranPa.removeFrom(List(fa), GranPa.findAll.toList)
      fa.sons.foreach(x => Son.referenceChanged(None,x))
      false
    })
    super.delete(id)
  }
  
  override def update(id: ObjectId, obj: Father) = {
    GranPa.removeFrom(List(obj), GranPa.findAll.toList)
    val oldObj = 
      findOneById(id).get

    (obj.gp) match {
      case Some(fat) =>
        (GranPa.findOneById(fat.id)) match {
          case Some(gp) =>
          	GranPa.addTo(List(obj), gp)
          case _ =>
        }
      case _ =>
    }
    
    val olds = 
    	oldObj.sons
    val news =
    	obj.sons
    	
    for (
        o <- olds;
        n <- news
        ) {
    	if (!news.exists(x => x.id==o.id)) //delete old
    	  Son.referenceChanged(None,o)
    	else if (!olds.exists(x => x.id == n.id)) //add new
    	  Son.referenceChanged(Some(Reference(id)),o)
    }
    
    super.update(id, obj)
  }
}

