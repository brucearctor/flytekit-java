/*
 * Copyright 2021 Flyte Authors
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
package org.flyte.flytekit.testing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.auto.value.AutoValue;
import org.flyte.flytekit.SdkBindingData;
import org.flyte.flytekit.SdkBindingDataFactory;
import org.flyte.flytekit.SdkWorkflow;
import org.flyte.flytekit.SdkWorkflowBuilder;
import org.flyte.flytekit.jackson.JacksonSdkType;
import org.flyte.flytekit.testing.RemoteSumTask.RemoteSumInput;
import org.flyte.flytekit.testing.RemoteSumTask.RemoteSumOutput;
import org.flyte.flytekit.testing.SumTask.SumInput;
import org.flyte.flytekit.testing.SumTask.SumOutput;
import org.junit.jupiter.api.Test;

public class FibonacciWorkflowTest {

  @Test
  public void test() {
    SdkTestingExecutor.Result result =
        SdkTestingExecutor.of(new FibonacciWorkflow())
            .withFixedInput("fib0", 1)
            .withFixedInput("fib1", 1)
            .execute();

    assertThat(result.getIntegerOutput("fib2"), equalTo(2L));
    assertThat(result.getIntegerOutput("fib3"), equalTo(3L));
    assertThat(result.getIntegerOutput("fib4"), equalTo(5L));
    assertThat(result.getIntegerOutput("fib5"), equalTo(8L));
  }

  @Test
  public void testWithFixedInputs() {
    SdkTestingExecutor.Result result =
        SdkTestingExecutor.of(new FibonacciWorkflow())
            .withFixedInputs(
                JacksonSdkType.of(FibonacciWorkflowInputs.class),
                FibonacciWorkflowInputs.create(
                    SdkBindingDataFactory.of(1), SdkBindingDataFactory.of(1)))
            .execute();

    assertThat(result.getIntegerOutput("fib2"), equalTo(2L));
    assertThat(result.getIntegerOutput("fib3"), equalTo(3L));
    assertThat(result.getIntegerOutput("fib4"), equalTo(5L));
    assertThat(result.getIntegerOutput("fib5"), equalTo(8L));
  }

  @Test
  public void testWithTaskOutput_runnableTask() {
    SdkTestingExecutor.Result result =
        SdkTestingExecutor.of(new FibonacciWorkflow())
            .withFixedInput("fib0", 1)
            .withFixedInput("fib1", 1)
            .withTaskOutput(
                new SumTask(),
                SumInput.create(SdkBindingDataFactory.of(3L), SdkBindingDataFactory.of(5L)),
                SumOutput.create(SdkBindingDataFactory.of(42L)))
            .execute();

    assertThat(result.getIntegerOutput("fib2"), equalTo(2L));
    assertThat(result.getIntegerOutput("fib3"), equalTo(3L));
    assertThat(result.getIntegerOutput("fib4"), equalTo(5L));
    assertThat(result.getIntegerOutput("fib5"), equalTo(42L));
  }

  @Test
  public void testWithTaskOutput_remoteTask() {
    SdkTestingExecutor.Result result =
        SdkTestingExecutor.of(new RemoteFibonacciWorkflow())
            .withFixedInput("fib0", 1)
            .withFixedInput("fib1", 1)
            .withTaskOutput(
                RemoteSumTask.create(),
                RemoteSumInput.create(SdkBindingDataFactory.of(1L), SdkBindingDataFactory.of(1L)),
                RemoteSumOutput.create(5L))
            .withTaskOutput(
                RemoteSumTask.create(),
                RemoteSumInput.create(SdkBindingDataFactory.of(1L), SdkBindingDataFactory.of(5L)),
                RemoteSumOutput.create(10L))
            .withTaskOutput(
                RemoteSumTask.create(),
                RemoteSumInput.create(SdkBindingDataFactory.of(5L), SdkBindingDataFactory.of(10L)),
                RemoteSumOutput.create(20L))
            .withTaskOutput(
                RemoteSumTask.create(),
                RemoteSumInput.create(SdkBindingDataFactory.of(10L), SdkBindingDataFactory.of(20L)),
                RemoteSumOutput.create(40L))
            .execute();

    assertThat(result.getIntegerOutput("fib2"), equalTo(5L));
    assertThat(result.getIntegerOutput("fib3"), equalTo(10L));
    assertThat(result.getIntegerOutput("fib4"), equalTo(20L));
    assertThat(result.getIntegerOutput("fib5"), equalTo(40L));
  }

  @Test
  public void testWithTask() {
    SdkTestingExecutor.Result result =
        SdkTestingExecutor.of(new FibonacciWorkflow())
            .withFixedInput("fib0", 1)
            .withFixedInput("fib1", 1)
            .withTask(
                new SumTask(),
                input ->
                    SumOutput.create(SdkBindingDataFactory.of(input.a().get() * input.b().get())))
            // can combine withTask and withTaskOutput
            .withTaskOutput(
                new SumTask(),
                SumInput.create(SdkBindingDataFactory.of(1), SdkBindingDataFactory.of(1)),
                SumOutput.create(SdkBindingDataFactory.of(2)))
            .execute();

    assertThat(result.getIntegerOutput("fib2"), equalTo(2L));
    assertThat(result.getIntegerOutput("fib3"), equalTo(2L));
    assertThat(result.getIntegerOutput("fib4"), equalTo(4L));
    assertThat(result.getIntegerOutput("fib5"), equalTo(8L));
  }

  public static class FibonacciWorkflow
      extends SdkWorkflow<FibonacciWorkflowInputs, FibonacciWorkflowOutputs> {
    public FibonacciWorkflow() {
      super(
          JacksonSdkType.of(FibonacciWorkflowInputs.class),
          JacksonSdkType.of(FibonacciWorkflowOutputs.class));
    }

    @Override
    public FibonacciWorkflowOutputs expand(
        SdkWorkflowBuilder builder, FibonacciWorkflowInputs inputs) {

      SdkBindingData<Long> fib2 =
          builder
              .apply("fib-2", new SumTask(), SumInput.create(inputs.fib0(), inputs.fib1()))
              .getOutputs()
              .c();

      SdkBindingData<Long> fib3 =
          builder
              .apply("fib-3", new SumTask(), SumInput.create(inputs.fib1(), fib2))
              .getOutputs()
              .c();

      SdkBindingData<Long> fib4 =
          builder.apply("fib-4", new SumTask(), SumInput.create(fib2, fib3)).getOutputs().c();

      SdkBindingData<Long> fib5 =
          builder.apply("fib-5", new SumTask(), SumInput.create(fib3, fib4)).getOutputs().c();

      return FibonacciWorkflowOutputs.create(fib2, fib3, fib4, fib5);
    }
  }

  @AutoValue
  public abstract static class FibonacciWorkflowInputs {
    public abstract SdkBindingData<Long> fib0();

    public abstract SdkBindingData<Long> fib1();

    public static FibonacciWorkflowInputs create(
        SdkBindingData<Long> fib0, SdkBindingData<Long> fib1) {
      return new AutoValue_FibonacciWorkflowTest_FibonacciWorkflowInputs(fib0, fib1);
    }
  }

  @AutoValue
  public abstract static class FibonacciWorkflowOutputs {
    public abstract SdkBindingData<Long> fib2();

    public abstract SdkBindingData<Long> fib3();

    public abstract SdkBindingData<Long> fib4();

    public abstract SdkBindingData<Long> fib5();

    public static FibonacciWorkflowOutputs create(
        SdkBindingData<Long> fib2,
        SdkBindingData<Long> fib3,
        SdkBindingData<Long> fib4,
        SdkBindingData<Long> fib5) {
      return new AutoValue_FibonacciWorkflowTest_FibonacciWorkflowOutputs(fib2, fib3, fib4, fib5);
    }
  }

  /** FibonacciWorkflow, but using RemoteSumTask instead. */
  public static class RemoteFibonacciWorkflow
      extends SdkWorkflow<FibonacciWorkflowInputs, FibonacciWorkflowOutputs> {
    public RemoteFibonacciWorkflow() {
      super(
          JacksonSdkType.of(FibonacciWorkflowInputs.class),
          JacksonSdkType.of(FibonacciWorkflowOutputs.class));
    }

    @Override
    public FibonacciWorkflowOutputs expand(
        SdkWorkflowBuilder builder, FibonacciWorkflowInputs inputs) {

      SdkBindingData<Long> fib2 =
          builder
              .apply(
                  "fib-2",
                  RemoteSumTask.create(),
                  RemoteSumInput.create(inputs.fib0(), inputs.fib1()))
              .getOutputs()
              .c();

      SdkBindingData<Long> fib3 =
          builder
              .apply("fib-3", RemoteSumTask.create(), RemoteSumInput.create(inputs.fib1(), fib2))
              .getOutputs()
              .c();

      SdkBindingData<Long> fib4 =
          builder
              .apply("fib-4", RemoteSumTask.create(), RemoteSumInput.create(fib2, fib3))
              .getOutputs()
              .c();

      SdkBindingData<Long> fib5 =
          builder
              .apply("fib-5", RemoteSumTask.create(), RemoteSumInput.create(fib3, fib4))
              .getOutputs()
              .c();

      return FibonacciWorkflowOutputs.create(fib2, fib3, fib4, fib5);
    }
  }
}
