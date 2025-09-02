package com.tcc.PlayWise.pipeline.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Pipeline<T> {

    private final List<StepConfig<T>> steps = new ArrayList<>();

    public Pipeline(List<Step<T>> stepsList) {
        if (stepsList != null) {
            for (Step<T> step : stepsList) {
                this.steps.add(new StepConfig<>(step, null)); // sem parâmetros extras
            }
        }
    }

    public Pipeline() {}

    public Pipeline<T> addStep(Step<T> step, boolean enabled, Map<String, Object> params) {
        if (enabled) steps.add(new StepConfig<>(step, params));
        return this;
    }

    public Pipeline<T> addStep(Step<T> step) {
        return addStep(step, true, null);
    }


    public T execute(T input, Map<String, Object> context) {
        T result = input;
        for (StepConfig<T> stepConfig : steps) {
            Map<String, Object> stepParams = stepConfig.params != null ? stepConfig.params : context;
            result = stepConfig.step.safeApply(result, stepParams);
        }
        return result;
    }

    public List<T> executeParallel(List<T> inputs, Map<String, Object> context) {
        return inputs.parallelStream()
                .map(input -> execute(input, context))
                .collect(Collectors.toList());
    }

    private static class StepConfig<T> {
        Step<T> step;
        Map<String, Object> params;

        StepConfig(Step<T> step, Map<String, Object> params) {
            this.step = step;
            this.params = params;
        }
    }
}
