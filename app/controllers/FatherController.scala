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

object FatherController extends Controller with TablePager[Father] with CRUDer[Father] {  
  
  def index = 
    Action {	
	  implicit request =>
	  	Ok(views.html.fatherPage())
  	}
  
  val singleton = Father
  
  def elemValues(fa: Father) =
    Seq(fa.id.toStringMongod(),fa.name)
    
  override val elemsToDisplay = 
    Seq("id","name")
    
  def formTemplate(formgp: Form[Father])(implicit request: RequestHeader): play.api.templates.Html =
    views.html.fatherForm(formgp)
    
  def form =
    Form(
      mapping(
        "id" -> text,
        "name" -> nonEmptyText,
        "gp" -> text,
        "sons" -> text.verifyOptJson
       ){(id, name,gp, _sons) =>
          {
            val granpa = tryo{makeReference[GranPa](GranPa.findOneByIdString(gp).get)}
            val sons: List[Reference[Son]] = List()
            				
            (tryo(new ObjectId(id))) match {
              case Some(oid) => 			//UPDATE
                	  Father.update(oid,
            		      Father(
            		          id = oid,
            		          name = name,
            		          gp = granpa,
            		          sons = sons
            		      )
            		   )
            		   Father.findOneById(oid).get
              case _ =>						//CREATE
            		  Father.create(
            		      Father(
            		          name = name,
            		          gp = granpa,
            		          sons = sons
            		          )
            		      )
            }
          }
      }{f => {
        Some(f.id.toStringMongod(), 
    		 f.name,
    		 f.gp.map(x => x.id.toStringMongod()).getOrElse(""),
    		 ""//Json.stringify("")
    		)
      }
      }
  )  
  
}