package org.marvin.executor.actions

import org.marvin.model.EngineMetadata
import org.scalatest.{Matchers, WordSpec}

class ArtifactSaverFactoryTest extends WordSpec with Matchers {

  val engineMetadata1: EngineMetadata = new EngineMetadata("name",
    "version", "engineType", null, "artifactsRemotePath", "HDFS", null,
    3000, 3000, 3000, Option(3000), 3000, "testHost")

  val engineMetadata2: EngineMetadata = new EngineMetadata("name",
    "version", "engineType", null, "artifactsRemotePath", "S3", null,
    3000, 3000, 3000, Option(3000), 3000, "testHost")

  val engineMetadata3: EngineMetadata = new EngineMetadata("name",
    "version", "engineType", null, "artifactsRemotePath", "anything", null,
    3000, 3000, 3000, Option(3000), 3000, "testHost")

  val props1 = new ArtifactSaverFactory(engineMetadata1)

  val props2 = new ArtifactSaverFactory(engineMetadata2)

  val props3 = new ArtifactSaverFactory(engineMetadata3)

  "A engineMetadata with artifactsSaverType as HDFS" should {
    "return Props with actorClass ArtifactHdfsSaver" in {
    assert(props1.Selector.actorClass().toString == "class org.marvin.manager.ArtifactHdfsSaver")
    }
  }

  "A engineMetadata with artifactsSaverType as S3" should {
    "return Props with actorClass ArtifactS3Saver" in {
      assert(props2.Selector.actorClass().toString == "class org.marvin.manager.ArtifactS3Saver")
    }
  }

  "A engineMetadata with artifactsSaverType different than HDFS and S3" should {
    "return null" in {
      assert(props3.Selector == null)
    }
  }

}
