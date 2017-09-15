package models

import akka.stream.Materializer
import play.api.libs.json.{JsObject, Json, Reads, Writes}
import play.modules.reactivemongo.{MongoController, ReactiveMongoComponents}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait ModelDAO extends MongoController with ReactiveMongoComponents {
  implicit val executionContext: ExecutionContext
  implicit val materializer: Materializer
  val collectionName: String
  private lazy val futureCollection: Future[JSONCollection] = database.map(_.collection[JSONCollection](collectionName))

  protected def execute[T](f: (JSONCollection) => Future[T]): Future[T] = {
    for {
      collection <- futureCollection
      result <- f(collection)
    } yield result
  }

  def findOne[T](selector: JsObject, projection: JsObject = Json.obj())
                (implicit reads: Reads[T], writes: Writes[T]): Future[Option[T]] = {
    execute(x => x.find(selector, projection).one[T])
  }
}
//
/*users.findOne[String](Json.obj("username" -> "user")).map {
  case Some(x) =>
    println("Info from db: " + x)
  case None =>
    println("No true in db")
}*/
//