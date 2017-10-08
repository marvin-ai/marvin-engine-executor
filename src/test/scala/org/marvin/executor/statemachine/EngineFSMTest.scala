package org.marvin.executor.statemachine

import actions.OnlineActionResponse
import akka.actor.FSM.Event
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit, TestProbe}
import org.marvin.executor.actions.OnlineAction.{OnlineExecute, OnlineReload}
import org.scalatest.{Matchers, WordSpec, WordSpecLike}

import scala.util.Failure

class EngineFSMTest extends TestKit(ActorSystem("EngineFSMTest")) with ImplicitSender
  with WordSpecLike with Matchers {

  "engine finite state machine" should {

    "start with Unavailable" in {
      val probe = TestProbe()
      val fsm = TestFSMRef[State, Data, EngineFSM](new EngineFSM(probe.ref))
      fsm.stateName should be (Unavailable)
      fsm.stateData should be (NoModel)
    }

    "go to Reloading when Unavailable and receive Reload" in {
      val probe = TestProbe()
      val fsm = TestFSMRef[State, Data, EngineFSM](new EngineFSM(probe.ref))
      val testProtocol = "protocol1234"
      fsm ! Reload(testProtocol)
      probe.expectMsg(OnlineReload(testProtocol))
      fsm.stateName should be (Reloading)
      fsm.stateData should be (ToReload(testProtocol))
    }

    "go to Reloaded when Reloading and receive Reloaded" in {
      val probe = TestProbe()
      val fsm = TestFSMRef[State, Data, EngineFSM](new EngineFSM(probe.ref))
      fsm.setState(Reloading)
      fsm ! Reloaded("protocol123")
      fsm.stateName should be (Ready)
      fsm.stateData should be (Model("protocol123"))
    }

    "receive failure and stay Reloading when Reloading" in {
      val probe = TestProbe()
      val fsm = TestFSMRef[State, Data, EngineFSM](new EngineFSM(probe.ref))
      fsm.setState(Reloading)
      fsm ! OnlineExecute("test", "test")
      probe.expectNoMsg
      fsm.stateName should be (Reloading)
      expectMsg(Failure)
    }

    "forward the message when Ready" in {
      val probe = TestProbe()
      val fsm = TestFSMRef[State, Data, EngineFSM](new EngineFSM(probe.ref))
      fsm.setState(Ready)
      fsm ! OnlineExecute("test", "testMessage")
      probe.expectMsg(OnlineExecute("test", "testMessage"))
      probe.reply(OnlineActionResponse(message = "testResult"))
      expectMsg(OnlineActionResponse(message = "testResult"))
      fsm.stateName should be (Ready)
    }

    "go to Reloading when Ready" in {
      val probe = TestProbe()
      val fsm = TestFSMRef[State, Data, EngineFSM](new EngineFSM(probe.ref))
      fsm.setState(Ready)
      val protocol = "protocol99"
      fsm ! Reload(protocol)
      probe.expectMsg(OnlineReload(protocol))
      fsm.stateName should be (Reloading)
      fsm.stateData should be (ToReload(protocol))
    }
  }
}
