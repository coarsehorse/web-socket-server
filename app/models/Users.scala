package models

import javax.inject.Inject

import akka.stream.Materializer
import play.api.mvc.ControllerComponents
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext

class Users @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                      val executionContext: ExecutionContext,
                      val materializer: Materializer) extends ModelDAO {
  override val collectionName = "users"
}
