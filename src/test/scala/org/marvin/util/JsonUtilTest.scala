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
package org.marvin.util

import java.io.File
import org.json.JSONObject
import org.marvin.model.{EngineMetadata, MarvinEExecutorException}
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source


class JsonUtilTest extends WordSpec with Matchers {

  val testFilePath = new File(getClass.getClassLoader.getResource("metadataToValidate.json").getPath)
  val jsonToValidate = new JSONObject(Source.fromFile(testFilePath).mkString)

  val errorTestFilePath = new File(getClass.getClassLoader.getResource("metadataToValidateWithError.json").getPath)
  val errorJsonToValidate = new JSONObject(Source.fromFile(errorTestFilePath).mkString)

  "A Metadata validation" should {
    "return Unit if metadataToValidate is valid" in {
      assert { JsonUtil.validateMetadataJson(jsonToValidate) == () }
    }
  }

  "A Metadata validation" should {
    "throw Exception if metadataToValidate is invalid" in {
      assertThrows[MarvinEExecutorException] {
        JsonUtil.validateMetadataJson(errorJsonToValidate)
      }
    }
  }

  "A JsonUtil.fromJson method" should {
    "return Engine name (iris_species)" in {
      assert { JsonUtil.fromJson[EngineMetadata](Source.fromFile(testFilePath).mkString).toString == "iris_species"}
    }
  }

}
