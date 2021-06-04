package ch.epfl.pop.pubsub.graph.handlers

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.data.ObjectType
import ch.epfl.pop.pubsub.graph.{GraphMessage, MessageDecoder, Validator}

object ParamsWithMessageHandler {
  val graph: Flow[GraphMessage, GraphMessage, NotUsed] = Flow.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] => {
      import GraphDSL.Implicits._

      /* partitioner port numbers */
      val portPipelineError = 0
      val portLao = 1
      val portMeeting = 2
      val portRollCall = 3
      val portWitness = 4
      val totalPorts = 5

      /* building blocks */
      val messageDecoder = builder.add(MessageDecoder.dataParser)
      val messageContentValidator = builder.add(Validator.messageContentValidator)

      val handlerPartitioner = builder.add(Partition[GraphMessage](totalPorts, {
        case Left(jsonRpcRequest: JsonRpcRequest) => jsonRpcRequest.getDecodedDataHeader.get match {
          case (ObjectType.LAO, _) => portLao
          case (ObjectType.MEETING, _) => portMeeting
          case (ObjectType.ROLL_CALL, _) => portRollCall
          case (ObjectType.MESSAGE, _) => portWitness
        }
        case _ => portPipelineError // Pipeline error goes directly in handlerMerger
      }))

      val laoHandler = builder.add(LaoHandler.handler)
      val meetingHandler = builder.add(MeetingHandler.handler)
      val rollCallHandler = builder.add(RollCallHandler.handler)
      val witnessHandler = builder.add(WitnessHandler.handler)

      val handlerMerger = builder.add(Merge[GraphMessage](totalPorts))

      /* glue the components together */
      messageDecoder ~> messageContentValidator ~> handlerPartitioner

      handlerPartitioner.out(portPipelineError) ~> handlerMerger
      handlerPartitioner.out(portLao) ~> laoHandler ~> handlerMerger
      handlerPartitioner.out(portMeeting) ~> meetingHandler ~> handlerMerger
      handlerPartitioner.out(portRollCall) ~> rollCallHandler ~> handlerMerger
      handlerPartitioner.out(portWitness) ~> witnessHandler ~> handlerMerger

      /* close the shape */
      FlowShape(messageDecoder.in, handlerMerger.out)
    }
  })
}
