package com.tcc.PlayWise.pipeline;

import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.pipeline.core.Step;
import com.tcc.PlayWise.pipeline.step.GameStep;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStepPriceTest {

    private final GameStep gameStep = new GameStep();

    private final Step<Game> standardizePrice = gameStep.standardizePrice();
    private final Step<Game> markIfFree = gameStep.markIfFree();
    private final Step<Game> taxPrice = gameStep.taxPrice();
    private final Step<Game> storeRatePrice = gameStep.storeRatePrice();
    private final Step<Game> finalizePrice = gameStep.finalizePrice();

    @Test
    void deveDetectarJogoGratuitoPorTexto() {
        Game game = new Game("Free Game", "game", "-", "Gratuito");
        standardizePrice.safeApply(game, null);
        markIfFree.safeApply(game, null);

        assertEquals(0.0, game.getPriceParsed());
        assertEquals("Gratuito", game.getPrice());
    }

    @Test
    void deveDetectarJogoGratuitoPorValorZero() {
        Game game = new Game("Zero Game", "game", "-", "R$ 0,00");
        standardizePrice.safeApply(game, null);
        markIfFree.safeApply(game, null);

        assertEquals(0.0, game.getPriceParsed());
        assertEquals("Gratuito", game.getPrice());
    }

    @Test
    void deveConverterPrecoValido() {
        Game game = new Game("Portal", "game", "-", "R$ 129,99");
        standardizePrice.safeApply(game, null);

        assertEquals(129.99, game.getPriceParsed());
        assertEquals(129.99, game.getPriceOriginal());
    }

    @Test
    void deveTratarPrecoMalformadoComoErro() {
        Game game = new Game("Broken Price", "game", "-", "R$ ,");
        standardizePrice.safeApply(game, null);

        assertEquals(-1.0, game.getPriceParsed());
        assertEquals(-1.0, game.getPriceOriginal());
    }

    @Test
    void deveCalcularImpostoCorretamente() {
        Game game = new Game("Taxed Game", "game", "-", "R$ 100,00");
        game.setPriceParsed(100.0);
        taxPrice.safeApply(game, null);

        assertEquals(15.0, game.getTax());
        assertEquals(85.0, game.getPriceParsed());
    }

    @Test
    void deveCalcularTaxaDaLojaCorretamente() {
        Game game = new Game("Rated Game", "game", "-", "R$ 100,00");
        game.setPriceParsed(85.0);
        storeRatePrice.safeApply(game, null);

        assertEquals(25.5, game.getRate());
        assertEquals(59.5, game.getPriceParsed());
    }

    @Test
    void deveFormatarPrecoFinalCorretamente() {
        Game game = new Game("Final Price Game", "game", "-", "R$ 100,00");
        game.setPriceParsed(59.5);
        finalizePrice.safeApply(game, null);

        assertEquals("R$ 59,50", game.getPrice());
    }

    @Test
    void deveMarcarPrecoComoGratuitoSeValorZero() {
        Game game = new Game("Free Final", "game", "-", "R$ 0,00");
        game.setPriceParsed(0.0);
        finalizePrice.safeApply(game, null);

        assertEquals("Gratuito", game.getPrice());
    }

    @Test
    void deveMarcarPrecoComoIndisponivelSeConversaoFalhar() {
        Game game = new Game("Broken Final", "game", "-", "R$ ,");
        game.setPriceParsed(-1.0);
        finalizePrice.safeApply(game, null);

        assertEquals("Indisponível", game.getPrice());
    }
}
