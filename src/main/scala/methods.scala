import akka.http.scaladsl.model.Uri
import io.circe.Json
import io.circe.optics.JsonPath.root
import io.circe.parser.parse
import spray.json.DefaultJsonProtocol
import scala.collection.immutable.ListMap

object methods extends DefaultJsonProtocol {
  //Функция для получения uri по ключевому тегу
  def uriconstructor(tag:String):Uri = {
    val uri = "pagesize=100&order=desc&sort=creation&tagged="+tag+"&site=stackoverflow"
    Uri.from(scheme = "https",
      host = "api.stackexchange.com",
      path = "/2.2/search",
      queryString = Some(uri))
  }
  //Функция, которая обрабатывает ответ сайта
  def answeredCount(js: String): (List[Int], Map[String, Int]) = {
    val json: Json = parse(js).getOrElse(Json.Null)
    val isAns:List[Boolean] = root.items.each.is_answered.boolean.getAll(json)
    val answeredCount = isAns.map{x => if (x) 1 else 0}.sum
    val allCount = isAns.size
    val tagsbyFreq= ListMap(root.items.each.tags.each.string.getAll(json)
      .groupBy(identity).mapValues(_.size).toSeq.sortWith(_._2 > _._2):_*)
    (List(allCount,answeredCount),tagsbyFreq)
  }
}
