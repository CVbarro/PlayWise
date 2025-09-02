package com.tcc.PlayWise.service;

import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.pipeline.core.Pipeline;
import com.tcc.PlayWise.pipeline.core.Step;
import com.tcc.PlayWise.pipeline.step.StepFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final StepFactory stepFactory;
    private final List<Game> gameStorage = new ArrayList<>();

    public GameService(StepFactory stepFactory) {
        this.stepFactory = stepFactory;
    }

    private List<Map<String, Object>> getPipelineConfig() {
        return List.of(
                Map.of("step", "standardizeTitle", "enabled", true),
                Map.of("step", "standardizeType", "enabled", true),
                Map.of("step", "standardizePrice", "enabled", true),
                Map.of("step", "markIfFree", "enabled", true),
                Map.of("step", "taxPrice", "enabled", true),
                Map.of("step", "storeRatePrice", "enabled", true),
                Map.of("step", "finalizePrice", "enabled", true),
                Map.of("step", "standardizeDate", "enabled", true)
        );
    }

    private Pipeline<Game> buildPipeline() {
        Pipeline<Game> pipeline = new Pipeline<>();

        for (Map<String, Object> stepConfig : getPipelineConfig()) {
            String stepName = (String) stepConfig.get("step");
            boolean enabled = (Boolean) stepConfig.getOrDefault("enabled", true);
            Step<Game> step = stepFactory.createStep(Game.class, stepName);

            if (step != null) {
                pipeline.addStep(step, enabled, Collections.emptyMap()); // params não são mais usados
            } else {
                logger.warn("Step not found: {}", stepName);
            }
        }

        return pipeline;
    }

    public Game processGame(Game game) {
        Pipeline<Game> pipeline = buildPipeline();
        Game processed = pipeline.execute(game, new HashMap<>());
        gameStorage.add(processed); // simula persistência
        return processed;
    }

    public List<Game> processSteamGames(List<Game> rawGames) {
        List<Game> result = new ArrayList<>();
        for (Game game : rawGames) {
            result.add(processGame(game));
        }
        return result;
    }

    @PostConstruct
    public void processAllGames() {
        if (gameStorage.isEmpty()) return;

        Pipeline<Game> pipeline = buildPipeline();

        for (Game game : gameStorage) {
            Game processed = pipeline.execute(game, new HashMap<>());
            logger.info("Processed: {}", processed);
        }
    }

    public List<Game> getAllProcessedGames() {
        List<Game> result = new ArrayList<>();
        Pipeline<Game> pipeline = buildPipeline();

        for (Game game : gameStorage) {
            Game processed = pipeline.execute(game, new HashMap<>());
            result.add(processed);
        }

        return result;
    }
}
