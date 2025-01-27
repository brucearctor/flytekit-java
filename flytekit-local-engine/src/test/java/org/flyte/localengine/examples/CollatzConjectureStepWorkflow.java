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
package org.flyte.localengine.examples;

import static org.flyte.flytekit.SdkConditions.isTrue;
import static org.flyte.flytekit.SdkConditions.when;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import org.flyte.flytekit.SdkBindingData;
import org.flyte.flytekit.SdkBindingDataFactory;
import org.flyte.flytekit.SdkRunnableTask;
import org.flyte.flytekit.SdkWorkflow;
import org.flyte.flytekit.SdkWorkflowBuilder;
import org.flyte.flytekit.jackson.JacksonSdkType;

// if x is even, then x/2 else 3x+1
@AutoService(SdkWorkflow.class)
public class CollatzConjectureStepWorkflow
    extends SdkWorkflow<CollatzConjectureStepWorkflow.Input, TestUnaryIntegerOutput> {

  public CollatzConjectureStepWorkflow() {
    super(
        JacksonSdkType.of(CollatzConjectureStepWorkflow.Input.class),
        JacksonSdkType.of(TestUnaryIntegerOutput.class));
  }

  @AutoValue
  abstract static class Input {
    abstract SdkBindingData<Long> x();

    public static Input create(SdkBindingData<Long> x) {
      return new AutoValue_CollatzConjectureStepWorkflow_Input(x);
    }
  }

  @Override
  public TestUnaryIntegerOutput expand(SdkWorkflowBuilder builder, Input input) {
    SdkBindingData<Boolean> isOdd =
        builder
            .apply("is_odd", new IsEvenTask(), IsEvenTask.Input.create(input.x()))
            .getOutputs()
            .res();

    SdkBindingData<Long> nextX =
        builder
            .apply(
                "decide",
                when(
                        "was_even",
                        isTrue(isOdd),
                        new Divide(),
                        Divide.Input.create(input.x(), SdkBindingDataFactory.of(2L)))
                    .otherwise(
                        "was_odd", new ThreeXPlusOne(), ThreeXPlusOne.Input.create(input.x())))
            .getOutputs()
            .o();

    return TestUnaryIntegerOutput.create(nextX);
  }

  @AutoService(SdkRunnableTask.class)
  public static class IsEvenTask extends SdkRunnableTask<IsEvenTask.Input, IsEvenTask.Output> {
    private static final long serialVersionUID = -1606085903949620311L;

    public IsEvenTask() {
      super(JacksonSdkType.of(IsEvenTask.Input.class), JacksonSdkType.of(IsEvenTask.Output.class));
    }

    @Override
    public IsEvenTask.Output run(IsEvenTask.Input input) {
      return IsEvenTask.Output.create(SdkBindingDataFactory.of(input.x().get() % 2 == 0));
    }

    @AutoValue
    public abstract static class Input {

      public abstract SdkBindingData<Long> x();

      public static Input create(SdkBindingData<Long> x) {
        return new AutoValue_CollatzConjectureStepWorkflow_IsEvenTask_Input(x);
      }
    }

    @AutoValue
    public abstract static class Output {

      public abstract SdkBindingData<Boolean> res();

      public static Output create(SdkBindingData<Boolean> res) {
        return new AutoValue_CollatzConjectureStepWorkflow_IsEvenTask_Output(res);
      }
    }
  }

  @AutoService(SdkRunnableTask.class)
  public static class Divide extends SdkRunnableTask<Divide.Input, TestUnaryIntegerOutput> {
    private static final long serialVersionUID = -526903889896397227L;

    public Divide() {
      super(JacksonSdkType.of(Divide.Input.class), JacksonSdkType.of(TestUnaryIntegerOutput.class));
    }

    @Override
    public TestUnaryIntegerOutput run(Divide.Input input) {
      return TestUnaryIntegerOutput.create(
          SdkBindingDataFactory.of(input.num().get() / input.den().get()));
    }

    @AutoValue
    public abstract static class Input {
      public abstract SdkBindingData<Long> num();

      public abstract SdkBindingData<Long> den();

      public static Input create(SdkBindingData<Long> num, SdkBindingData<Long> den) {
        return new AutoValue_CollatzConjectureStepWorkflow_Divide_Input(num, den);
      }
    }

    @AutoValue
    public abstract static class Output {

      public abstract SdkBindingData<Long> res();

      public static Output create(SdkBindingData<Long> res) {
        return new AutoValue_CollatzConjectureStepWorkflow_Divide_Output(res);
      }
    }
  }

  // 3x+1
  @AutoService(SdkRunnableTask.class)
  public static class ThreeXPlusOne
      extends SdkRunnableTask<ThreeXPlusOne.Input, TestUnaryIntegerOutput> {
    private static final long serialVersionUID = 932934331328064751L;

    public ThreeXPlusOne() {
      super(
          JacksonSdkType.of(ThreeXPlusOne.Input.class),
          JacksonSdkType.of(TestUnaryIntegerOutput.class));
    }

    @Override
    public TestUnaryIntegerOutput run(ThreeXPlusOne.Input input) {
      return TestUnaryIntegerOutput.create(SdkBindingDataFactory.of(3 * input.x().get() + 1));
    }

    @AutoValue
    public abstract static class Input {
      public abstract SdkBindingData<Long> x();

      public static Input create(SdkBindingData<Long> x) {
        return new AutoValue_CollatzConjectureStepWorkflow_ThreeXPlusOne_Input(x);
      }
    }
  }
}
