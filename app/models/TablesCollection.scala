package models

import akka.stream.Materializer
import javax.inject.Inject

import play.modules.reactivemongo.ReactiveMongoApi
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class TablesCollection @Inject()(val cc: ControllerComponents,
                                val reactiveMongoApi: ReactiveMongoApi,
                                val executionContext: ExecutionContext,
                                val materializer: Materializer)
  extends AbstractController(cc) with ModelDAO {
  val collectionName: String = "tables"
}
