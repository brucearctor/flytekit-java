/*
 * Copyright 2020 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.flyte.flytekit;

/** Building block for tasks that execute Java code. */
public abstract class SdkRunnableTask<InputT, OutputT> {

  private final SdkType<InputT> inputType;
  private final SdkType<OutputT> outputType;

  public SdkRunnableTask(SdkType<InputT> inputType, SdkType<OutputT> outputType) {
    this.inputType = inputType;
    this.outputType = outputType;
  }

  public String getName() {
    return getClass().getName();
  }

  public SdkType<InputT> getInputType() {
    return inputType;
  }

  public SdkType<OutputT> getOutputType() {
    return outputType;
  }

  public abstract OutputT run(InputT input);
}
