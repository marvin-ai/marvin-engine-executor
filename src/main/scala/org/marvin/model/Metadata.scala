/**
  * Copyright [2017] [B2W Digital]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
  */
package org.marvin.model

case class EngineMetadata(name:String, version:String, engineType:String,
                          actions:List[EngineActionMetadata], artifactsLocalPath:String,
                          artifactsRemotePath:String,
                          onlineActionTimeout:Int,
                          healthCheckTimeout:Int,
                          reloadTimeout:Int,
                          hdfsHost:String){
  override def toString: String = name
}

case class EngineActionMetadata(name:String, actionType:String, port:Int, host:String, artifactsToPersist:List[String], artifactsToLoad:List[String]){
  override def toString: String = name
}