package com.tcc.PlayWise.service;

import com.tcc.PlayWise.model.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Testes do GameService")
class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Test
    @DisplayName("Deve processar jogo pago corretamente")
    void deveProcessarJogoPago() {
        Game game = new Game("Portal", "game", "12 Mar, 2022", "R$ 129,99");
        Game processed = gameService.processGame(game);

        assertAll("Validação do Game processado",
                () -> assertEquals("PORTAL", processed.getTitle()),
                () -> assertEquals("GAME", processed.getType()),
                () -> assertEquals("12/03/2022", processed.getReleaseDate()),
                () -> assertEquals("R$ 77,34", processed.getPrice()), // valor formatado
                () -> assertEquals(129.99, processed.getPriceOriginal()),
                () -> assertEquals(77.34, processed.getPriceParsed()),
                () -> assertEquals(19.50, processed.getTax()),
                () -> assertEquals(33.15, processed.getRate())
        );
    }

    @Test
    @DisplayName("Deve processar jogo gratuito sem aplicar taxas")
    void deveProcessarJogoGratuito() {
        Game game = new Game("Free Game", "game", "Coming soon", "Gratuito");
        Game processed = gameService.processGame(game);

        assertAll("Validação de jogo gratuito",
                () -> assertEquals("FREE GAME", processed.getTitle()),
                () -> assertEquals("GAME", processed.getType()),
                () -> assertEquals("Em breve", processed.getReleaseDate()),
                () -> assertEquals("Gratuito", processed.getPrice()),
                () -> assertEquals(0.0, processed.getPriceOriginal()),
                () -> assertEquals(0.0, processed.getPriceParsed()),
                () -> assertEquals(0.0, processed.getTax()),
                () -> assertEquals(0.0, processed.getRate())
        );
    }
}
