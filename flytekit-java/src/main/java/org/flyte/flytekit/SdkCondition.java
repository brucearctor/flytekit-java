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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SdkCondition extends SdkTransform {
  private final List<SdkConditionCase> cases;
  private final String otherwiseName;
  private final SdkTransform otherwise;

  SdkCondition(List<SdkConditionCase> cases, String otherwiseName, SdkTransform otherwise) {
    this.cases = cases;
    this.otherwiseName = otherwiseName;
    this.otherwise = otherwise;
  }

  public SdkCondition when(String name, SdkBooleanExpression condition, SdkTransform then) {
    List<SdkConditionCase> newCases = new ArrayList<>(cases);
    newCases.add(SdkConditionCase.create(name, condition, then));

    return new SdkCondition(newCases, this.otherwiseName, this.otherwise);
  }

  public SdkTransform otherwise(String name, SdkTransform otherwise) {
    if (this.otherwise != null) {
      // it isn't possible to double-call `otherwise` without upcasting
      // `SdkTransform` to `SdkCondition` because `otherwise` returns
      // `SdkTransform` instead of `SdkCondition` as other methods do.

      throw new IllegalStateException(
          "invariant failed: can't set 'otherwise' because it's already set");
    }

    return new SdkCondition(this.cases, name, otherwise);
  }

  @Override
  public SdkBranchNode apply(
      SdkWorkflowBuilder builder,
      String nodeId,
      List<String> upstreamNodeIds,
      Map<String, SdkBindingData> inputs) {
    if (!inputs.isEmpty()) {
      throw new IllegalArgumentException("invariant failed: inputs must be empty");
    }

    SdkBranchNode.Builder nodeBuilder = new SdkBranchNode.Builder(builder);

    for (SdkConditionCase case_ : cases) {
      nodeBuilder.addCase(case_);
    }

    if (otherwiseName != null) {
      nodeBuilder.addOtherwise(otherwiseName, otherwise);
    }

    return nodeBuilder.build(nodeId, upstreamNodeIds);
  }
}
