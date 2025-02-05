/*
 * Copyright 2021 Flyte Authors.
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
package org.flyte.examples.flytekitscala

import org.flyte.flytekit.{
  SdkBindingData,
  SdkDynamicWorkflowTask,
  SdkWorkflowBuilder
}
import org.flyte.flytekitscala.SdkScalaType

import scala.annotation.tailrec
import org.flyte.flytekitscala.SdkBindingDataFactory._

case class DynamicFibonacciWorkflowTaskInput(n: SdkBindingData[Long])
case class DynamicFibonacciWorkflowTaskOutput(output: SdkBindingData[Long])

class DynamicFibonacciWorkflowTask
    extends SdkDynamicWorkflowTask[
      DynamicFibonacciWorkflowTaskInput,
      DynamicFibonacciWorkflowTaskOutput
    ](
      SdkScalaType[DynamicFibonacciWorkflowTaskInput],
      SdkScalaType[DynamicFibonacciWorkflowTaskOutput]
    ) {

  override def run(
      builder: SdkWorkflowBuilder,
      input: DynamicFibonacciWorkflowTaskInput
  ): DynamicFibonacciWorkflowTaskOutput = {

    @tailrec
    def fib(
        n: Long,
        value: SdkBindingData[Long],
        prev: SdkBindingData[Long]
    ): SdkBindingData[Long] = {
      if (n == input.n.get) value
      else
        fib(
          n + 1,
          builder(
            s"fib-${n + 1}",
            new SumTask(),
            SumTaskInput(value, prev)
          ).getOutputs.c,
          value
        )
    }

    require(input.n.get > 0, "n < 0")
    val value = if (input.n.get == 0) {
      of(0)
    } else {
      fib(1, of(1), of(0))
    }
    DynamicFibonacciWorkflowTaskOutput(value)
  }

}
