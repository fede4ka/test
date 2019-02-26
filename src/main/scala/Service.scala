import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import scala.concurrent.duration._
import spray.json._
import methods._

object Service extends App {


    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      get {path("search") {
          parameters('tag.*) { params =>
            val res = params
              .map(uriconstructor)
              .map(uri=> Http().singleRequest(HttpRequest(uri = uri)))
              .map(future=>future.flatMap(x=>Gzip.decodeMessage(x).entity.toStrict(5 second))
                .map(x=>x.getData().decodeString("utf8"))
                .flatMap(x=>Future(answeredCount(x))))
              .map(future=>Await.result(future, 10 second))

            val zipped = params zip res
            val data = ByteString(zipped.toJson.toString())
            complete(HttpResponse( entity = HttpEntity(ContentTypes.`application/json`, data)))

          }
        }
      }


          val zipped = tags zip res

          val data = ByteString(zipped.toJson.toString())//Упаковка в энтити ответа
          HttpResponse( entity = HttpEntity(ContentTypes.`application/json`, data))

      
      }}
    val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)
    println(s"Started...\nTest request link http://localhost:8080/search?tag=clojure&tag=haskell&tag=java\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
