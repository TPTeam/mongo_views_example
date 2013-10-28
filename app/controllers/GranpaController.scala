package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.collection.mutable.Map
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.data.format.Formats._
import play.api.libs.concurrent.Execution.Implicits._
import controllerhelper._
import tp_utils.Tryer._

object GranpaController extends Controller with TablePager[GranPa] with CRUDer[GranPa] {  
  
  def index = 
    Action {	
	  implicit request =>
	  	Ok(views.html.granPaPage())
  	}
  
  val singleton = GranPa
  
  def elemValues(gp: GranPa) =
    Seq(gp.id.toStringMongod(),gp.name)
    
  def formTemplate(formgp: Form[GranPa])(implicit request: RequestHeader): play.api.templates.Html =
    views.html.granPaForm(formgp)
    
   /*
  def elemsFromJson(elems: JsValue):  scala.collection.immutable.Map[String,Reference[Building]] = {
    elems.\("data") match {
      case builds: JsArray =>
        (builds.value.map(b =>
          if (b.\("selected").asOpt[Boolean].getOrElse(false))
        	  tryo {b.\("key").as[String] -> Reference[Building](new ObjectId(b.\("value").as[String]))}
          else None
         )).flatten.toMap
      case any =>
        scala.collection.immutable.Map.empty
    }
        
  }
  
  import Json._
  def elemsToJson(available: List[Building],selected: List[Building]): JsValue = {
    Json.obj(
        "name" -> Building.collectionName,
        "data" -> JsArray(
        			available.map(b =>
        				JsObject(
        						Seq(
        							"key" -> toJson(b.name),
        							"value" -> toJson(b.id.toStringMongod),
        							"selected" -> toJson(selected.exists(b2 => b.id == b2.id))
        							))
        					).toSeq
        			)
    	)
  }
    
  def elemsToJson(available: Iterator[Building],selected: Iterator[Building]): JsValue =
    elemsToJson(available.toList, selected.toList)
  */  
  
  def form =
    Form(
      mapping(
        "id" -> text,
        "name" -> nonEmptyText,
        "sons" -> text.verifyOptJson
       ){(id, name, _sons) =>
          {
            val sons: List[Reference[Father]] = List()
            				
            (tryo(new ObjectId(id))) match {
              case Some(oid) => 			//UPDATE
                	  GranPa.update(oid,
            		      GranPa(
            		          id = oid,
            		          name = name,
            		          sons = sons
            		      )
            		   )
            		   GranPa.findOneById(oid).get
              case _ =>						//CREATE
            		  GranPa.create(
            		      GranPa(
            		          name = name,
            		          sons = sons
            		          )
            		      )
            }
          }
      }{gp => {
        Some(gp.id.toStringMongod(), 
    		 gp.name,
    		 ""//Json.stringify("")
    		)
      }
      }
  )  
  
}