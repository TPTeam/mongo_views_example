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

object SonController extends Controller with TablePager[Son] with CRUDer[Son] {  
  
  def index = 
    Action {	
	  implicit request =>
	  	Ok(views.html.sonPage())
  	}
  
  val singleton = Son
  
  def elemValues(gp: Son) =
    Seq(gp.id.toStringMongod(),gp.name)
    
   override val elemsToDisplay = 
    Seq("id","name")
    
  def formTemplate(formgp: Form[Son])(implicit request: RequestHeader): play.api.templates.Html =
    views.html.sonForm(formgp)
    
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
                	  Son.update(oid,
            		      Son(
            		          id = oid,
            		          name = name
            		          //sons = sons
            		      )
            		   )
            		   Son.findOneById(oid).get
              case _ =>						//CREATE
            		  Son.create(
            		      Son(
            		          name = name
            		          //sons = sons
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