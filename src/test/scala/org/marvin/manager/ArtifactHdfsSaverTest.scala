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
package org.marvin.manager

import java.io.File

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.marvin.manager.ArtifactSaver.{SaveToLocal, SaveToRemote}
import org.marvin.model.EngineMetadata
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ArtifactHdfsSaverTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val engineMetadata: EngineMetadata = new EngineMetadata("name",
    "version", "engineType", null, "artifactsRemotePath", null,
    3000, 3000, 3000, Option(3000), 3000, "testHost")

  val artifactSaver = new ArtifactHdfsSaver(engineMetadata)

  /** preStart() Test */
  artifactSaver.preStart()

  "HADOOP_CONF_DIR" when {
    "Not Null" should {
      "Assign value to confFiles" in {
        assert(artifactSaver.conf.getResource("hdfs-site.xml") != null)
      }
    }
  }

  assert(artifactSaver.conf.get("fs.defaultFS") == "testHost")

  /** generatePaths() Test */
  val paths = artifactSaver.generatePaths("artifactSample", "protocolSample")

  assert(paths.get("localPath") == Option(new Path(s"${engineMetadata.artifactsLocalPath}/${engineMetadata.name}/artifactSample")))
  assert(paths.get("remotePath") == Option(new Path(s"${engineMetadata.artifactsRemotePath}/${engineMetadata.name}/${engineMetadata.version}/artifactName/protocol")))

  /** getListOfFiles Test */
  val confFiles: List[File] = artifactSaver.getListOfFiles(sys.env.get("HADOOP_CONF_DIR").mkString)

  assert(confFiles(0).exists())

  /** receive Test*/

  "SaveTolocal and SaveToRemote" must {
    "Send back messages: Done" in {
      var actortest: ActorRef = system.actorOf(Props(new ArtifactHdfsSaver(engineMetadata)), name = "artifactSaver")
      actortest ! SaveToLocal("artifactSample", "protocolSample")
      expectMsg(Done)
      actortest ! SaveToRemote("artifactSample", "protocolSample")
      expectMsg(Done)
    }
  }


}