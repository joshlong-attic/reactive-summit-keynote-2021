import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.{RootResolver, ZHttpAdapter}
import zhttp.http._
import zhttp.service.Server
import zio.{ExitCode, ZEnv, ZIO}

import scala.language.postfixOps

sealed trait Role

object Role {
  case object SoftwareDeveloper       extends Role
  case object SiteReliabilityEngineer extends Role
  case object DevOps                  extends Role
}

case class Employee(
                     name: String,
                     role: Role
                   )

case class EmployeesArgs(role: Role)
case class EmployeeArgs(name: String)

case class Queries(
                    @GQLDescription("Return all employees with specific role")
                    employees: EmployeesArgs => List[Employee],
                    @GQLDescription("Find an employee by its name")
                    employee: EmployeeArgs => Option[Employee]
                  )
object MyServer extends zio.App {

  val employees = List(
    Employee("Alex", Role.DevOps),
    Employee("Maria", Role.SoftwareDeveloper),
    Employee("James", Role.SiteReliabilityEngineer),
    Employee("Peter", Role.SoftwareDeveloper),
    Employee("Julia", Role.SiteReliabilityEngineer),
    Employee("Roberta", Role.DevOps)
  )

  val api = graphQL(
    RootResolver(
      Queries(
        args => employees.filter(e => args.role == e.role),
        args => employees.find(e => e.name == args.name)
      )
    )
  )

  val myApp = for {
    interpreter <- api.interpreter
    _ <- zio.console.putStrLn(api.render)
    _ <- Server
      .start(
        port = 8088,
        http = Http.route {
          case _ -> Root / "api" / "graphql" =>
            ZHttpAdapter.makeHttpService(interpreter)
        }
      )
      .forever
  } yield ()

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    myApp.exitCode

}