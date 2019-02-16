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

object Service {
  def main(args: Array[String]) {
    //Простейший шаблон HTTP сервера
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    val requestHandler: HttpRequest => HttpResponse = {

      case a: HttpRequest => a match {
          //Запрос к search
        case HttpRequest(GET, Uri.Path("/search"), _, _, _) =>
          //Ключевые теги из запроса
          val tags =a.getUri().query().toString.split("&")
            .flatMap(_.split("=")).toList.drop(1).sliding(1, 2).flatten.toList
          println(tags)

          val res = tags
            .map(uriconstructor)//Uri для каждого ключевого тега
            .map(uri=> Http().singleRequest(HttpRequest(uri = uri)))//Запрос по каждому
            .map(future=>future.flatMap(x=>Gzip.decodeMessage(x).entity.toStrict(5 second))
              .map(x=>x.getData().decodeString("utf8"))//Декодирование ответа
              .flatMap(x=>Future(answeredCount(x))))//Рассчёт статистики тегов в вопросах
            .map(future=>Await.result(future, 10 second))

          val zipped = tags zip res

          val data = ByteString(zipped.toJson.toString())//Упаковка в энтити ответа
          HttpResponse( entity = HttpEntity(ContentTypes.`application/json`, data))

        case r: HttpRequest =>
          r.discardEntityBytes()
          HttpResponse(404, entity = "Unknown resource!")
      }}
    val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)
    println(s"Started...\nTest request link http://localhost:8080/search?tag=clojure&tag=haskell&tag=java\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}