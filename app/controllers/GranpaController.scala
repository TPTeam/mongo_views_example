package controllers

import play.api._
import play.api.mvc._
import models._
import RefObj._
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
    
  override val elemsToDisplay = 
    Seq("id","name")
    
  def formTemplate(formgp: Form[GranPa])(implicit request: RequestHeader): play.api.templates.Html =
    views.html.granPaForm(formgp)
    
  def form =
    Form(
      mapping(
        "id" -> text,
        "name" -> nonEmptyText,
        "sons" -> text.verifyOptJson
       ){(id, name, _sons) =>
          {
            val sons: List[Reference[Father]] = 
              tryo{Json.parse(_sons)} match {
                case Some(s : JsArray) =>
                  s.value.seq.map(v =>
                    tryo{makeReference[Father](Father.findOneByIdString(v.as[String]).get)}).toList.flatten
                case _ => List()
              }
            				
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
    		 Json.stringify(Json.toJson(gp.sons.map(s => s.id.toStringMongod)))
    		)
      }
      }
  )  
  
}