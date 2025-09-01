package com.tcc.PlayWise.pipeline.core;

import java.util.Map;

@FunctionalInterface
public interface Step<T> {

    T apply(T input, Map<String, Object> params) throws Exception;

    default T safeApply(T input, Map<String, Object> params) {
        try {
            T result = apply(input, params);
            System.out.println("[Pipeline] Sucesso: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[Pipeline] Erro na etapa: " + e.getMessage());
            return input;
        }
    }
}
