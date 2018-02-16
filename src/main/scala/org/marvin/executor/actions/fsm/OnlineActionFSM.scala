/*
 * Copyright [2017] [B2W Digital]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.marvin.executor.actions.fsm

import akka.actor.{ActorRef, FSM, Props}
import org.marvin.exception.MarvinEExecutorException
import org.marvin.executor.actions.OnlineAction
import org.marvin.executor.actions.OnlineAction._
import org.marvin.executor.proxies.{FailedToReload, Reloaded}
import org.marvin.executor.statemachine.{Model, Ready, Reloading, Unavailable}
import org.marvin.model.EngineMetadata

import scala.concurrent.duration._

//states
sealed trait State
case object Unavailable extends State
case object Reloading extends State
case object Ready extends State
case object Stopped extends State
case object Terminated extends State

sealed trait Data
final case class ToTerminate() extends Data
final case class ToReload(protocol: String) extends Data
final case class ToReload(protocol: String) extends Data

final case class Model(protocol: String) extends Data

class OnlineActionFSM(var actor: ActorRef, metadata: EngineMetadata) extends FSM[State, Data]{
  def this(metadata: EngineMetadata) = this(null, metadata)

  var reloadStateTimeout: FiniteDuration = metadata.reloadStateTimeout.getOrElse(180000D) milliseconds

  startWith(Unavailable, _)

  when(Unavailable) {
    case Event(OnlineReload(protocol), _) => {
      actor ! OnlineReload(protocol)
      goto(Reloading) using ToReload(protocol)
    }
    case Event(ToTerminate, _) => {
      actor ! OnlineReload(protocol)
      goto(Reloading) using ToReload(protocol)
    }
    case Event(e, s) => {
      log.warning("Engine is unavailable, not possible to perform event {} in state {}/{}", e, stateName, s)
      sender ! akka.actor.Status.Failure(new MarvinEExecutorException("It's not possible to process the request now, the model is unavailable. Perform a reload and try again."))
      stay
    }
  }

  when(Reloading, stateTimeout = reloadStateTimeout) {
    case Event(Reloaded(protocol), _) => {
      goto(Ready) using Model(protocol)
    }
    case Event(FailedToReload(protocol), _) => {
      log.error(s"Failed to reload with protocol {$protocol}")
      goto(Unavailable)
    }
    case Event(StateTimeout, _) => {
      log.error("Reloading state timed out.")
      goto(Unavailable)
    }
    case Event(e, s) => {
      log.warning("Engine is reloading, not possible to perform event {} in state {}/{}", e, stateName, s)
      sender ! akka.actor.Status.Failure(new MarvinEExecutorException("It's not possible to process the request now, the model is being reloaded."))
      stay
    }
  }

  when(Terminated) {
    case Event(Start, _) => {
      goto(Ready) using Model(protocol)
    }
    case Event(_, _) => {
      log.warning("Engine is terminated, not possible to perform event {} in state {}/{}", e, stateName, s)
      sender ! akka.actor.Status.Failure(new MarvinEExecutorException("It's not possible to process the request now, the model is being reloaded."))
      stay
    }
  }

  initialize()
}
