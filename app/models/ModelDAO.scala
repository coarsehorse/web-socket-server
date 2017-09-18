package models

import akka.stream.Materializer
import play.api.libs.json._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{MongoController, ReactiveMongoComponents}
import reactivemongo.api.commands._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait ModelDAO extends MongoController with ReactiveMongoComponents {
  implicit val executionContext: ExecutionContext
  implicit val materializer: Materializer
  val collectionName: String
  private lazy val futureCollection: Future[JSONCollection] =
    database.map(_.collection[JSONCollection](collectionName))

  /**
    * Service execution process
    * @param f Mongo function(find, insert etc.)
    * @return Query result
    */
  protected def execute[T](f: (JSONCollection) => Future[T]): Future[T] = {
    for {
      collection <- futureCollection
      result <- f(collection)
    } yield result
  }

  /**
    * Multiple search
    * @param selector Selector query
    * @param projection Projection query
    * @return List of results
    */
  def find[T](selector: JsObject = Json.obj(), projection: JsObject = Json.obj())
                (implicit reads: Reads[T], writes: Writes[T]): Future[List[T]] = {
    execute(x => x.find(selector, projection).cursor[T]().collect[List]())
  }

  /**
    * Find one document
    * @param selector Selector query
    * @param projection Projection query
    * @return Result of search
    */
  def findOne[T](selector: JsObject, projection: JsObject = Json.obj())
                (implicit reads: Reads[T], writes: Writes[T]): Future[Option[T]] = {
    execute(x => x.find(selector, projection).one[T])
  }

  /**
    * Insert document(s)
    * @param that what will be inserted
    * @param writes Writes format
    * @return Result of insert operation
    */
  def insert[T](that: T)(implicit writes: OWrites[T]): Future[WriteResult] = {
    println(that)
    execute(x => x.insert(that))
  }

  /**
    * Count documents in the collection
    * @return
    */
  def count: Future[Int] = execute(_.count())

  /**
    *
    * @param selector Selector query
    * @param updater Updater query
    * @param upsert Upsert tick
    * @param multi Multi tick
    * @return Update result
    */
  def update[T, M](selector: T, updater: M, upsert: Boolean = false, multi: Boolean = false)
            (implicit writesT: OWrites[T], writesM: OWrites[M]): Future[UpdateWriteResult] = {
    execute(_.update(selector, updater, upsert = upsert, multi = multi))
  }

  def remove[T](selector: T, firstMatchOnly: Boolean = false)(implicit writes: OWrites[T]) = {
    execute(_.remove(selector = selector, firstMatchOnly = firstMatchOnly))
  }
}
