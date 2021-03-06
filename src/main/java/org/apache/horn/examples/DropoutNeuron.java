/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.horn.examples;

import java.io.IOException;

import org.apache.hadoop.io.FloatWritable;
import org.apache.horn.core.Neuron;
import org.apache.horn.core.Synapse;
import org.apache.horn.utils.MathUtils;

public class DropoutNeuron extends
    Neuron<Synapse<FloatWritable, FloatWritable>> {

  private float m2;

  @Override
  public void forward(Iterable<Synapse<FloatWritable, FloatWritable>> messages)
      throws IOException {
    m2 = (isTraining()) ? MathUtils.getBinomial(1, 0.5) : 0.5f;

    if (m2 > 0) {
      float sum = 0;
      for (Synapse<FloatWritable, FloatWritable> m : messages) {
        sum += m.getInput() * m.getWeight();
      }

      this.setDrop(false);
      this.feedforward(squashingFunction.apply(sum) * m2);
    } else {
      this.setDrop(true);
      this.feedforward(0);
    }
  }

  @Override
  public void backward(Iterable<Synapse<FloatWritable, FloatWritable>> messages)
      throws IOException {
    if (!this.isDropped()) {
      float delta = 0;

      for (Synapse<FloatWritable, FloatWritable> m : messages) {
        // Calculates error gradient for each neuron
        delta += (m.getDelta() * m.getWeight());

        // Weight corrections
        float weight = -this.getLearningRate() * m.getDelta()
            * this.getOutput() + this.getMomentumWeight() * m.getPrevWeight();
        this.push(weight);
      }

      this.backpropagate(delta * squashingFunction.applyDerivative(getOutput()));
    } else {
      this.backpropagate(0);
    }
  }

}
