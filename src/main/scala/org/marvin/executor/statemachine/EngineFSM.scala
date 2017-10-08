package org.marvin.executor.statemachine

import akka.actor.{ActorRef, FSM}
import org.marvin.executor.actions.OnlineAction.{OnlineExecute, OnlineReload}

import scala.util.Failure

//receive events
final case class Reload(protocol: String = "")
final case class Reloaded(protocol: String)

//states
sealed trait State
case object Unavailable extends State
case object Reloading extends State
case object Ready extends State

sealed trait Data
case object NoModel extends Data
final case class ToReload(protocol: String) extends Data
final case class Model(protocol: String) extends Data

class EngineFSM(target: ActorRef) extends FSM[State, Data]{

  startWith(Unavailable, NoModel)

  when(Unavailable) {
    case Event(Reload(protocol), _) => {
      target ! OnlineReload(protocol = protocol)
      goto(Reloading) using ToReload(protocol)
    }
  }

  when(Reloading) {
    case Event(Reloaded(protocol), _) => {
      goto(Ready) using Model(protocol)
    }
    case Event(e,s) => {
      log.warning("Engine is reloading, not possible to perform event {} in state {}/{}", e, stateName, s)
      sender() ! Failure
      stay
    }
  }

  when(Ready) {
    case Event(OnlineExecute(message, params), _) => {
      target forward OnlineExecute(message, params)
      stay
    }
    case Event(Reload(protocol), _) => {
      target ! OnlineReload(protocol = protocol)
      goto(Reloading) using ToReload(protocol)
    }
  }

}
