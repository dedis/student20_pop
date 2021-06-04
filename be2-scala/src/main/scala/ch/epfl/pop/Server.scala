package ch.epfl.pop

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import akka.util.Timeout
import ch.epfl.pop.pubsub.{PubSubMediator, PublishSubscribe}
import org.iq80.leveldb.Options

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object Server {

  /**
   * Create a webserver that handles http requests and websockets requests.
   */
  def main(args: Array[String]): Unit = {
    val PORT = 8000
    val PATH = ""

    val system = akka.actor.ActorSystem("pop-be2-inner-actor-system")
    implicit val typedSystem: ActorSystem[Nothing] = system.toTyped

    val root = Behaviors.setup[Nothing] { _ =>
      implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)
      // Create database
      val options: Options = new Options()
      options.createIfMissing(true)

      val pubSubMediator: ActorRef = system.actorOf(PubSubMediator.props)
      def publishSubscribeRoute: RequestContext => Future[RouteResult] = path(PATH) {
        handleWebSocketMessages(PublishSubscribe.buildGraph(pubSubMediator)(system))
      }

      implicit val executionContext: ExecutionContextExecutor = typedSystem.executionContext
      val bindingFuture = Http().newServerAt("localhost", PORT).bind(publishSubscribeRoute)
      bindingFuture.onComplete {
        case Success(_) => println("ch.epfl.pop.Server online at ws://localhost:" + PORT + "/" + PATH)
        case Failure(_) =>
          println("ch.epfl.pop.Server failed to start. Terminating actor system")
          system.terminate()
          typedSystem.terminate()
      }

      Behaviors.empty
    }

    ActorSystem[Nothing](root, "pop-be2-actor-system")

    StdIn.readLine() // let it run until user presses return
  }
}
