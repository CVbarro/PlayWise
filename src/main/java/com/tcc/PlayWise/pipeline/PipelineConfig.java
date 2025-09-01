package com.tcc.PlayWise.pipeline;


import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.pipeline.core.Pipeline;
import com.tcc.PlayWise.pipeline.core.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PipelineConfig {

    @Bean
    public Pipeline<Game> gamePipeline(List<Step<Game>> stepsGame) {
        return new Pipeline<>(stepsGame);
    }
}
