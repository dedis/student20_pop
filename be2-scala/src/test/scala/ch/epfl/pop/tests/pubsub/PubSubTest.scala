package ch.epfl.pop.tests.pubsub


import akka.NotUsed
import ch.epfl.pop.tests.MessageCreationUtils.{b64Decode, b64Encode, b64EncodeToString, generateKeyPair, getMessageParams, sign}

import java.io.File
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.TimeUnit
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Props, SpawnProtocol}
import akka.stream.{ClosedShape, SourceShape}
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Flow, GraphDSL, Keep, Merge, MergeHub, Partition, RunnableGraph, Sink, Source}
import akka.util.Timeout
import ch.epfl.pop
import ch.epfl.pop.DBActor
import ch.epfl.pop.DBActor.DBMessage
import ch.epfl.pop.crypto.{Hash, Signature}
import ch.epfl.pop.json.JsonMessages._
import ch.epfl.pop.json.{Actions, Base64String, ChannelName, KeySignPair, MessageContent, MessageContentData, MessageErrorContent, MessageParameters, Methods, Objects, SignatureJson}
import ch.epfl.pop.pubsub.{ChannelActor, PublishSubscribe}
import org.iq80.leveldb.Options
import org.scalatest.FunSuite
import ch.epfl.pop.json.JsonUtils.{ErrorCodes, MessageContentDataBuilder}
import org.scalactic.Equality
import com.google.crypto.tink.subtle.Ed25519Sign.KeyPair

import java.nio.charset.StandardCharsets
import java.util
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.pow
import scala.reflect.io.Directory

class PubSubTest extends FunSuite {

  private def sendAndVerify(messages: List[(Option[JsonMessagePubSubClient], Option[JsonMessageAnswerServer])]): Unit = {
    sendAndVerify(messages.map { case (m, o) => (m, o, 0) }, 1)
  }

  private def sendAndVerify(messages: List[(Option[JsonMessagePubSubClient], Option[JsonMessageAnswerServer], Int)], numberProcesses: Int): Unit = {
    val root = Behaviors.setup[SpawnProtocol.Command] { context =>
      SpawnProtocol()
    }

    val DatabasePath: String = "database_test"
    val file = new File(DatabasePath)
    //Reset db from previous tests
    val directory = new Directory(file)
    directory.deleteRecursively()

    implicit val system: ActorSystem[SpawnProtocol.Command] = ActorSystem[SpawnProtocol.Command](root, "test")
    implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)
    val (pubEntry, actor, dbActor) = setup(DatabasePath)

    val options: Options = new Options()
    options.createIfMissing(true)

    val flows = (1 to numberProcesses).map(_ => PublishSubscribe.jsonFlow(actor, dbActor)(timeout, system, pubEntry))

    //Generates a list of answers from the publish-subscribe given a list of requests
    val g: Source[JsonMessageAnswerServer, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      //Send the next message when receiving an answer
      val tester = Flow[JsonMessageAnswerServer].statefulMapConcat{ () =>
        var current = messages.tail

        { answer =>
          current match {
            case msg :: tail =>
              current = tail
              msg._1 match {
                case Some(clientMsg) => (msg._3, clientMsg) :: Nil
                case None => Nil
              }
            case Nil =>
              Nil
          }
        }
      }
      val merge = builder.add(Merge[JsonMessageAnswerServer](flows.length))
      val mergePart = builder.add(Merge[(Int, JsonMessagePubSubClient)](2))
      val broadcast = builder.add(Broadcast[JsonMessageAnswerServer](2))
      val partitioner = builder.add(Partition[(Int, JsonMessagePubSubClient)](flows.length, _._1))
      val removeIndex = Flow[(Int, JsonMessagePubSubClient)].map(_._2)
      val start = Source.single((messages.head._3, messages.head._1.get))

      flows.zipWithIndex.foreach(f => partitioner.out(f._2) ~> removeIndex ~> f._1 ~> merge)
      merge ~> broadcast
      broadcast.out(0) ~> tester ~> mergePart
      start ~> mergePart
      mergePart ~> partitioner
      SourceShape(broadcast.out(1))
    }
    )

    val sink: Sink[JsonMessageAnswerServer, Future[Iterable[JsonMessageAnswerServer]]] = Sink.collection
    val future = g.take(messages.length).runWith(sink)
    val rcvMsg = Await.result(future, 5.seconds)

    val expectedMsg = messages.flatMap(_._2)
    assert(expectedMsg.size == rcvMsg.size)
    expectedMsg.foreach { msg =>
      assert(rcvMsg.exists(_ === msg))
    }
  }

  //We need a special case for catchup, because arrays are compared by reference by default
  private implicit val catchupEq: Equality[JsonMessageAnswerServer] = new Equality[JsonMessageAnswerServer] {
    override def areEqual(a: JsonMessageAnswerServer, b: Any): Boolean = (a, b) match {
      case (
        AnswerResultArrayMessageServer(id1, messages1, _),
        AnswerResultArrayMessageServer(id2, messages2, _)
        ) =>
        id1 == id2 &&
          messages1.length == messages2.length &&
          messages1.forall(
            //As there is no specified order in the list, we need to check at least one element is equals
            m1 => messages2.exists(m2 => messageEqual(m1, m2))
          )
      case _ => a == b
    }

    private def messageEqual(msg1: MessageContent, msg2: MessageContent): Boolean = {
      msg1.encodedData == msg2.encodedData &&
      util.Arrays.equals(msg1.sender, msg2.sender) &&
        util.Arrays.equals(msg1.signature, msg2.signature) &&
        util.Arrays.equals(msg1.message_id, msg2.message_id) &&
        msg1.witness_signatures.length == msg2.witness_signatures.length &&
        msg1.witness_signatures.zip(msg2.witness_signatures).forall(s => util.Arrays.equals(s._1.witness, s._2.witness)
          && util.Arrays.equals(s._1.signature, s._2.signature)
        )
    }

  }


  private def setup(databasePath : String)(implicit system: ActorSystem[SpawnProtocol.Command], timeout: Timeout) = {
    implicit val ec: ExecutionContext = system.executionContext

    val (entry, exit) = MergeHub.source[PropagateMessageServer].toMat(BroadcastHub.sink)(Keep.both).run()
    val futureActor: Future[ActorRef[ChannelActor.ChannelMessage]] = system.ask(SpawnProtocol.Spawn(pop.pubsub.ChannelActor(exit),
      "actor", Props.empty, _))
    val actor = Await.result(futureActor, 5.seconds)
    val futureDBActor: Future[ActorRef[DBMessage]] = system.ask(SpawnProtocol.Spawn(DBActor(databasePath), "actorDB", Props.empty, _))
    val dbActor = Await.result(futureDBActor, 5.seconds)

    (entry, actor, dbActor)
  }



  private def getCreateLao(kp: KeyPair, id : Int, laoName: String): CreateLaoMessageClient = {

    val creationTime = pow(2,24).toLong
    val digest = MessageDigest.getInstance("SHA-256")
    val pk = kp.getPublicKey
    val data = "[" +
    '"' + b64EncodeToString(pk) + "\",\"" +
    creationTime.toString + "\",\"" +
    laoName + "\"]"
    val laoID = digest.digest(data.getBytes(StandardCharsets.UTF_8))

    val laoData : MessageContentData = new MessageContentDataBuilder()
      .setHeader(Objects.Lao, Actions.Create)
      .setId(laoID)
      .setName(laoName)
      .setCreation(creationTime)
      .setLastModified(creationTime)
      .setOrganizer(pk)
      .setWitnesses(Nil)
      .build()

    val channel = "/root"
    val params = getMessageParams(laoData, kp, channel)
    CreateLaoMessageClient(params, id, Methods.Publish)
  }

  private def createLaoSetup(kp: KeyPair, name: String = "The best LAO in the world", startingId: Int = 0) = {
    val createLao = getCreateLao(kp, startingId + 0, name)
    val laoID =  new String(Base64.getEncoder.encode(createLao.params.message.get.data.id))
    val channel = "/root/" +laoID

    val state = getBroadcastState(createLao, kp, channel, startingId + 2)
    val prop = getPropagate(state.params)

    val l: List[(Option[JsonMessagePubSubClient], Option[JsonMessageAnswerServer])] =
      List((Some(createLao), Some(AnswerResultIntMessageServer(startingId + 0))),
        (Some(SubscribeMessageClient(MessageParameters(channel, None), startingId + 1)), Some(AnswerResultIntMessageServer(startingId + 1))),
        (Some(state), Some(AnswerResultIntMessageServer(startingId + 2))),
        (None, Some(prop))
      )

    (l, laoID)
  }

  private def getBroadcastState(create: CreateLaoMessageClient, kp: KeyPair, channel : ChannelName, id: Int): BroadcastLaoMessageClient = {

    val dataCreate = create.params.message.get.data
    val dataBroadcast = new MessageContentDataBuilder()
      .setHeader(Objects.Lao, Actions.State)
      .setId(dataCreate.id)
      .setName(dataCreate.name)
      .setCreation(dataCreate.creation)
      .setLastModified(dataCreate.creation)
      .setOrganizer(dataCreate.organizer)
      .setWitnesses(Nil)
      .setModificationId(create.params.message.get.message_id)
      .setModificationSignatures(Nil)
      .build()

    val params = getMessageParams(dataBroadcast, kp, channel)
    val content = params.message.get
    println("Broadcast info")
    println("Content: " + content.encodedData)
    println("Signature: " + util.Arrays.toString(content.signature))
    println("Sender: " + util.Arrays.toString(content.sender))
    println(Signature.verify(content.encodedData, content.signature, content.sender))
    BroadcastLaoMessageClient(params, id, Methods.Publish)
  }

  private def getPropagate(params: MessageParameters): PropagateMessageServer = {
    PropagateMessageServer(params)
  }

  def getCreateMeeting(laoId: Base64String, kp: KeyPair, id: Int) : CreateMeetingMessageClient = {
    val name = "My awesome meeting"
    val creationTime = pow(2,24).toLong
    val location = "Zoom"
    val digest = MessageDigest.getInstance("SHA-256")
    println("Create meeting")
    println("LaoId: " + util.Arrays.toString(Base64.getDecoder.decode(laoId.getBytes())))
    println("Creation time: " + creationTime)
    println("Name: " + name)

    val eventId = Hash.computeMeetingId(Base64.getDecoder.decode(laoId), creationTime, name)
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Meeting, Actions.Create)
      .setId(eventId)
      .setName(name)
      .setCreation(creationTime)
      .setLastModified(creationTime)
      .setLocation(location)
      .setStart(1222222222L)
      .setEnd(1222222222L + 1L)
      .build()
    val channel = "/root/" + laoId
    val params = getMessageParams(data, kp, channel)
    println(util.Arrays.equals(Hash.computeMeetingId(Base64.getDecoder.decode(laoId.getBytes()), creationTime, name), params.message.get.data.id))
    CreateMeetingMessageClient(params, id, Methods.Publish)
  }

  private def getWitnessMessage(messageId: Base64String, kp: KeyPair, channel: ChannelName, id: Int) = {
    val signature = sign(kp, b64Decode(messageId))

    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Message, Actions.Witness)
      .setMessageId(messageId)
      .setSignature(signature)
      .build()
    val params = getMessageParams(data, kp, channel)
    WitnessMessageMessageClient(params, id, Methods.Publish)
  }

  test("Create a LAO, subscribe to its channel and publish the state") {
    val kp = generateKeyPair()
    val (l, _) = createLaoSetup(kp)
    sendAndVerify(l)
  }

  test("Create a channel, subscribe to it and publish multiple messages") {
    val kp = generateKeyPair()
    val (l, laoID) = createLaoSetup(kp)

    val meeting = getCreateMeeting(laoID, kp, 4)

    val messages = List(
      (Some(meeting),Some(AnswerResultIntMessageServer(4))),
      (None, Some(getPropagate(meeting.params)))
    )
    sendAndVerify(l ::: messages)
  }

  test("Create multiple LAOs, subscribe to their channel and publish multiple messages") {
    val kp = generateKeyPair()
    val (l1, laoID1) = createLaoSetup(kp)
    val (l2, laoID2) = createLaoSetup(kp, "My new LAO", 3)

    val msg1 = getCreateMeeting(laoID1,kp, 4)
    val msg2 = getCreateMeeting(laoID2, kp, 5)

    val messages : List[(Option[JsonMessagePubSubClient], Option[JsonMessageAnswerServer])] = List(
      (Some(msg1), Some(AnswerResultIntMessageServer(4))),
      (None, Some(getPropagate(msg1.params))),
      (Some(msg2), Some(AnswerResultIntMessageServer(5))),
      (None, Some(getPropagate(msg2.params)))
    )

    sendAndVerify(l1 ::: l2 ::: messages)

  }

  test("Two process subscribe and publish on the same channel") {
    val kp1 = generateKeyPair()
    val kp2 = generateKeyPair()
    val (l, laoID) = createLaoSetup(kp1)
    val channel = "/root/" + laoID
    val meeting1 = getCreateMeeting(laoID, kp1, 4)
    val meeting2 = getCreateMeeting(laoID, kp2, 1)

    val messages: List[(Option[JsonMessagePubSubClient], Option[JsonMessageAnswerServer], Int)] = List(
      (Some(SubscribeMessageClient(MessageParameters(channel, None), 0)), Some(AnswerResultIntMessageServer(0)), 1),
      (Some(meeting1), Some(AnswerResultIntMessageServer(4)), 0),
      (None, Some(getPropagate(meeting1.params)), 0),
      (None, Some(getPropagate(meeting1.params)), 1),
      (Some(meeting2), Some(AnswerResultIntMessageServer(1)), 1),
      (None, Some(getPropagate(meeting2.params)), 1),
      (None, Some(getPropagate(meeting2.params)), 0)
    )

    sendAndVerify(l.map{ case (msg, ans) => (msg, ans, 0)} ::: messages, 2)
  }

  test("Error when subscribing to a channel that does not exist") {
    val l = List(
      (Some(SubscribeMessageClient(MessageParameters("/root/unknow", None), 0)),
      Some(AnswerErrorMessageServer(Some(0), MessageErrorContent(-2, "Invalid resource: channel /root/unknow does not exist."))))
    )
    sendAndVerify(l)
  }

  test("Error when creating an existing LAO") {
    val kp = generateKeyPair()
    val laoName = "My LAO"
    val (l1, _) = createLaoSetup(kp, laoName)
    val createMsg = getCreateLao(kp, 3, laoName)
    val laoID =  new String(Base64.getEncoder.encode(createMsg.params.message.get.data.id))
    val l = List(
      (Some(createMsg), Some(AnswerErrorMessageServer(Some(3), MessageErrorContent(-3, "Channel /root/"
        + laoID +  " already exists."))))
    )
    sendAndVerify(l1 ::: l)
  }



  test("Don't receive messages when unsubscribing from a channel") {
    val kp = generateKeyPair()
    val (l1, laoId) = createLaoSetup(kp)
    val channel = "/root/" + laoId
    val meeting1 = getCreateMeeting(laoId, kp, 4)
    val meeting2 = getCreateMeeting(laoId, kp, 5)

    val messages = List(
      (Some(UnsubscribeMessageClient(MessageParameters(channel, None), 3)), Some(AnswerResultIntMessageServer(3))),
      (Some(meeting1), Some(AnswerResultIntMessageServer(4))),
      (None, None)
    )
    val root = Behaviors.setup[SpawnProtocol.Command] { context =>
      SpawnProtocol()
    }

    val l = l1 ::: messages

    val DatabasePath: String = "database_test"
    val file = new File(DatabasePath)
    //Reset db from previous tests
    val directory = new Directory(file)
    directory.deleteRecursively()

    implicit val system: ActorSystem[SpawnProtocol.Command] = ActorSystem[SpawnProtocol.Command](root, "test")
    implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)
    val (pubEntry, actor, dbActor) = setup(DatabasePath)

    val options: Options = new Options()
    options.createIfMissing(true)

    val in = l.map(_._1).flatten
    val out = l.map(_._2).flatten

    val flow = PublishSubscribe.jsonFlow(actor, dbActor)(timeout, system, pubEntry).take(out.length)
    val sink: Sink[JsonMessage, Future[Seq[JsonMessage]]] = Sink.fold(Seq.empty[JsonMessage])(_ :+ _)

    val future = Source(in).throttle(1, 200.milli).via(flow).runWith(sink)
    val res = Await.result(future, 5.seconds)
    assert(res == out)
  }

  test("Unsubscribe from a channel you are not subscribed to fails") {
    val l = List(
      (Some(UnsubscribeMessageClient(MessageParameters("/root/notsubscribed", None), 0)),
        Some(AnswerErrorMessageServer(Some(0), MessageErrorContent(-2, "Invalid resource: you are not subscribed to channel /root/notsubscribed."))))
    )
    sendAndVerify(l)
  }

  test("Catchup messages on a channel") {
    val kp = generateKeyPair()
    val createLao = getCreateLao(kp, 0, "My LAO")
    val laoId =  new String(Base64.getEncoder.encode(createLao.params.message.get.data.id))
    val channel = "/root/" + laoId
    val state = getBroadcastState(createLao, kp, channel, 1)
    val catchup = CatchupMessageClient(MessageParameters(channel, None), 2)
    val res = List(createLao.params.message.get, state.params.message.get)

    val l = List(
      (Some(createLao), Some(AnswerResultIntMessageServer(0))),
      (Some(state), Some(AnswerResultIntMessageServer(1))),
      (Some(catchup), Some(AnswerResultArrayMessageServer(2, res)))
    )
    sendAndVerify(l)
  }

  private def createLaoAndWitness() = {
    val kpOrga = generateKeyPair()
    val kpWitness = generateKeyPair()

    val createLao = getCreateLao(kpOrga,0, "LAOOOOO")
    val messageId = new String(b64Encode(createLao.params.message.get.message_id))
    val channel = "/root/" + new String(b64Encode(createLao.params.message.get.data.id))
    val witnessLaoCreation = getWitnessMessage(messageId, kpWitness, channel, 0)

    (createLao, witnessLaoCreation)

  }

  test("Create a LAO and witness it") {
    val (createLao, witnessLaoCreation) = createLaoAndWitness()
    val l = List(
      (Some(createLao), Some(AnswerResultIntMessageServer(0))),
      (Some(witnessLaoCreation), Some(AnswerResultIntMessageServer(0)))
    )
    sendAndVerify(l)
  }

  test("Witness an unknown message") {
    val kpWitness = generateKeyPair()
    val witnessInvalid = getWitnessMessage("", kpWitness, "", 0)
    val l = List(
      (Some(witnessInvalid),
        Some(AnswerErrorMessageServer(Some(0), MessageErrorContent(ErrorCodes.InvalidData.id, "The id the witness message refers to does not exist."))))
    )
    sendAndVerify(l)
  }

  test("Signatures are added to messages when witnessing") {
    val (createLao, witnessLaoCreation) = createLaoAndWitness()
    val key = witnessLaoCreation.params.message.get.sender
    val signature = witnessLaoCreation.params.message.get.data.signature
    val createLaoUpdated = createLao.params.message.get.updateWitnesses(KeySignPair(key, signature))

    val catchup = CatchupMessageClient(MessageParameters(witnessLaoCreation.params.channel, None), 1)
    val res = List(createLaoUpdated, witnessLaoCreation.params.message.get)

    val l = List(
      (Some(createLao), Some(AnswerResultIntMessageServer(0))),
      (Some(witnessLaoCreation), Some(AnswerResultIntMessageServer(0))),
      (Some(catchup), Some(AnswerResultArrayMessageServer(1, res)))
    )
    sendAndVerify(l)
  }
}
