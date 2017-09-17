package models

import akka.stream.Materializer
import play.api.libs.json._
import play.modules.reactivemongo.{MongoController, ReactiveMongoComponents}
import play.modules.reactivemongo.json._
import reactivemongo.api.commands._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

trait ModelDAO extends MongoController with ReactiveMongoComponents {
  implicit val executionContext: ExecutionContext
  implicit val materializer: Materializer
  val collectionName: String
  private lazy val futureCollection: Future[JSONCollection] =
    database.map(_.collection[JSONCollection](collectionName))

  protected def execute[T](f: (JSONCollection) => Future[T]): Future[T] = {
    for {
      collection <- futureCollection
      result <- f(collection)
    } yield result
  }

  def find[T](selector: JsObject = Json.obj(), projection: JsObject = Json.obj())
                (implicit reads: Reads[T], writes: Writes[T]): Future[List[T]] = {
    execute(x => x.find(selector, projection).cursor[T]().collect[List]())
  }

  def findOne[T](selector: JsObject, projection: JsObject = Json.obj())
                (implicit reads: Reads[T], writes: Writes[T]): Future[Option[T]] = {
    execute(x => x.find(selector, projection).one[T])
  }

  def insert[T](that: T)(implicit writes: OWrites[T]): Future[WriteResult] = {
    println(that)
    execute(x => x.insert(that))
  }

  def count: Future[Int] = execute(_.count())

  def update[T, M](selector: T, updater: M, upsert: Boolean = false, multi: Boolean = false)
            (implicit writesT: OWrites[T], writesM: OWrites[M]): Future[UpdateWriteResult] = {
    execute(_.update(selector, updater, upsert = upsert, multi = multi))
  }

  def remove[T](selector: T, firstMatchOnly: Boolean = false)(implicit writes: OWrites[T]) = {
    execute(_.remove(selector = selector, firstMatchOnly = firstMatchOnly))
  }
}
