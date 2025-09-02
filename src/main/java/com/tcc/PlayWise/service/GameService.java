package com.tcc.PlayWise.service;

import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.dto.GameDetailsDTO;
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
                Map.of("step", "taxPrice", "enabled", true),
                Map.of("step", "storeRatePrice", "enabled", true),
                Map.of("step", "finalizePrice", "enabled", true),
                Map.of("step", "standardizeDate", "enabled", true),
                Map.of("step", "markIfFree", "enabled", true)
        );
    }

    private Pipeline<Game> buildPipeline() {
        Pipeline<Game> pipeline = new Pipeline<>();

        for (Map<String, Object> stepConfig : getPipelineConfig()) {
            String stepName = (String) stepConfig.get("step");
            boolean enabled = (Boolean) stepConfig.getOrDefault("enabled", true);
            Map<String, Object> params = (Map<String, Object>) stepConfig.getOrDefault("params", new HashMap<>());
            Step<Game> step = stepFactory.createStep(Game.class, stepName);

            if (step != null) {
                pipeline.addStep(step, enabled, params);
            } else {
                logger.warn("Step not found: {}", stepName);
            }
        }

        return pipeline;
    }

    public GameDetailsDTO processGame(Game game) {
        Pipeline<Game> pipeline = buildPipeline();
        Map<String, Object> context = new HashMap<>();
        Game processed = pipeline.execute(game, context);

        gameStorage.add(processed); // simula persistência

        return buildDTO(processed, context);
    }

    public List<GameDetailsDTO> processSteamGames(List<Game> rawGames) {
        List<GameDetailsDTO> result = new ArrayList<>();
        for (Game game : rawGames) {
            result.add(processGame(game)); // reaproveita o método existente
        }
        return result;
    }

    @PostConstruct
    public void processAllGames() {
        if (gameStorage.isEmpty()) return;

        Pipeline<Game> pipeline = buildPipeline();

        for (Game game : gameStorage) {
            Map<String, Object> context = new HashMap<>();
            Game processed = pipeline.execute(game, context);
            logger.info("Processed: {}", processed);
        }
    }

    public List<GameDetailsDTO> getAllProcessedGames() {
        List<GameDetailsDTO> result = new ArrayList<>();
        Pipeline<Game> pipeline = buildPipeline();

        for (Game game : gameStorage) {
            Map<String, Object> context = new HashMap<>();
            Game processed = pipeline.execute(game, context);
            result.add(buildDTO(processed, context));
        }

        return result;
    }

    private GameDetailsDTO buildDTO(Game game, Map<String, Object> context) {
        GameDetailsDTO dto = new GameDetailsDTO();
        dto.setTitle(game.getTitle());
        dto.setType(game.getType());
        dto.setReleaseDate(game.getReleaseDate());
        dto.setPriceOriginal((double) context.getOrDefault("originalPrice", 0.0));
        dto.setPriceFinal((double) context.getOrDefault("parsedPrice", 0.0));
        dto.setTax((double) context.getOrDefault("imposto", 0.0));
        dto.setRate((double) context.getOrDefault("taxa", 0.0));

        return dto;
    }
}
