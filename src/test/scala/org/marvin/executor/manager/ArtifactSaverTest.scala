package org.marvin.executor.manager

import org.marvin.manager.ArtifactSaver
import org.marvin.model.EngineMetadata
import org.scalatest.{Matchers, WordSpec}

class ArtifactSaverTest extends WordSpec with Matchers {

  "A engineMetadata with artifactsSaverType as HDFS" should {
    "return Props with actorClass ArtifactHdfsSaver" in {
      val props = ArtifactSaver.build(new EngineMetadata("name",
        "version", "engineType", null, "artifactsRemotePath", "HDFS", "marvin-artifact-bucket", null,
        3000, 3000, 3000, Option(3000), 3000, "testHost"))
      assert(props.actorClass().toString == "class org.marvin.manager.ArtifactHdfsSaver")
    }
  }

  "A engineMetadata with artifactsSaverType as S3" should {
    "return Props with actorClass ArtifactS3Saver" in {
      val props = ArtifactSaver.build(new EngineMetadata("name",
        "version", "engineType", null, "artifactsRemotePath", "S3", "marvin-artifact-bucket", null,
        3000, 3000, 3000, Option(3000), 3000, "testHost"))
      assert(props.actorClass().toString == "class org.marvin.manager.ArtifactS3Saver")
    }
  }
}
