import CalibanServer.myApp
import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import zio.{App, ExitCode, ZIO}

val serverUrl = uri"http://localhost:8088/api/graphql"

val query = caliban.My.Queries.employees(caliban.My.Role.DevOps):
  caliban.My.Employee.name ~ caliban.My.Employee.role

val app = for
  result <- send(query.toRequest(serverUrl))
  _ <- zio.console.putStrLn(result.body.map(_.toString).getOrElse(""))
yield result

object MyClient extends App:
  override def run(args: List[String]) =
    app.provideCustomLayer(AsyncHttpClientZioBackend.layer()).exitCode
