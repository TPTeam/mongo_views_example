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

object FatherController extends Controller with TablePager[Father] with CRUDer[Father] {  
  
  def index = 
    Action {	
	  implicit request =>
	  	Ok(views.html.fatherPage())
  	}
  
  val singleton = Father
  
  def elemValues(gp: Father) =
    Seq(gp.id.toStringMongod(),gp.name)
    
  override val elemsToDisplay = 
    Seq("id","name")
    
  def formTemplate(formgp: Form[Father])(implicit request: RequestHeader): play.api.templates.Html =
    views.html.fatherForm(formgp)
    
  def form =
    Form(
      mapping(
        "id" -> text,
        "name" -> nonEmptyText,
        "sons" -> text.verifyOptJson
       ){(id, name, _sons) =>
          {
            val sons: List[Reference[Son]] = List()
            				
            (tryo(new ObjectId(id))) match {
              case Some(oid) => 			//UPDATE
                	  Father.update(oid,
            		      Father(
            		          id = oid,
            		          name = name,
            		          sons = sons
            		      )
            		   )
            		   Father.findOneById(oid).get
              case _ =>						//CREATE
            		  Father.create(
            		      Father(
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