package com.tcc.PlayWise.pipeline.step;

import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.pipeline.core.Step;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class StepFactory {

    private final Step<Game> standardizeTitle;
    private final Step<Game> standardizeType;
    private final Step<Game> standardizePrice;
    private final Step<Game> taxPrice;
    private final Step<Game> storeRatePrice;
    private final Step<Game> finalizePrice;
    private final Step<Game> standardizeDate;

    public StepFactory(
            Step<Game> standardizeTitle,
            Step<Game> standardizeType,
            Step<Game> standardizePrice,
            Step<Game> taxPrice,
            Step<Game> storeRatePrice,
            Step<Game> finalizePrice,
            Step<Game> standardizeDate
    ) {
        this.standardizeTitle = standardizeTitle;
        this.standardizeType = standardizeType;
        this.standardizePrice = standardizePrice;
        this.taxPrice = taxPrice;
        this.storeRatePrice = storeRatePrice;
        this.finalizePrice = finalizePrice;
        this.standardizeDate = standardizeDate;
    }

    public <T> Step<T> createStep(Class<T> clazz, String name) {
        if (clazz.equals(Game.class)) {
            switch (name) {
                case "standardizeTitle": return (Step<T>) standardizeTitle;
                case "standardizeType": return  (Step<T>) standardizeType;
                case "standardizePrice": return (Step<T>) standardizePrice;
                case "taxPrice": return  (Step<T>) taxPrice;
                case "storeRatePrice": return (Step<T>) storeRatePrice;
                case "finalizePrice": return (Step<T>) finalizePrice;
                case "standardizeDate": return (Step<T>) standardizeDate;
                default:
                    throw new RuntimeException("Etapa desconhecida: " + name);
            }
        }
        throw new RuntimeException("Classe não suportada: " + clazz.getSimpleName());
    }

}
