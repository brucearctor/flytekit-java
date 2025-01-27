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
package org.flyte.examples;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import org.flyte.flytekit.SdkBindingData;
import org.flyte.flytekit.SdkBindingDataFactory;
import org.flyte.flytekit.SdkRunnableTask;
import org.flyte.flytekit.jackson.Description;
import org.flyte.flytekit.jackson.JacksonSdkType;

@AutoService(SdkRunnableTask.class)
public class SumTask extends SdkRunnableTask<SumTask.SumInput, SumTask.SumOutput> {
  public SumTask() {
    super(JacksonSdkType.of(SumInput.class), JacksonSdkType.of(SumOutput.class));
  }

  @AutoValue
  public abstract static class SumInput {
    @Description("First operand")
    public abstract SdkBindingData<Long> a();

    @Description("Second operand")
    public abstract SdkBindingData<Long> b();

    public static SumInput create(SdkBindingData<Long> a, SdkBindingData<Long> b) {
      return new AutoValue_SumTask_SumInput(a, b);
    }
  }

  @AutoValue
  public abstract static class SumOutput {

    @Description("Sum of values")
    public abstract SdkBindingData<Long> c();

    public static SumOutput create(SdkBindingData<Long> c) {
      return new AutoValue_SumTask_SumOutput(c);
    }
  }

  @Override
  public SumOutput run(SumInput input) {
    return SumOutput.create(SdkBindingDataFactory.of(input.a().get() + input.b().get()));
  }

  @Override
  public boolean isCached() {
    return true;
  }

  @Override
  public String getCacheVersion() {
    return "1";
  }

  @Override
  public boolean isCacheSerializable() {
    return true;
  }
}
