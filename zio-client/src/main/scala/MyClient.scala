import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import zio.{App, ExitCode, Schedule, ZIO}
import zio.duration._

val serverUrl = uri"http://localhost:8888/graphql"

val query = caliban.CRM.Query.customers:
  caliban.CRM.Customer.name ~ caliban.CRM.Customer.profile:
    caliban.CRM.Profile.id

val request = send(query.toRequest(serverUrl)).flatMap:
  response =>
    ZIO.fromEither(response.body)

val app = for
  body <- request.retry(Schedule.exponential(100.millis))
  _ <- zio.console.putStrLn(body.toString)
yield ()

object MyClient extends App:
  override def run(args: List[String]) =
    app.provideCustomLayer(AsyncHttpClientZioBackend.layer()).exitCode
