package com.tcc.PlayWise.service;

import com.tcc.PlayWise.model.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Testes do SteamService")
class SteamServiceTest {

    @Autowired
    private SteamService steamService;

    @Test
    @DisplayName("Deve buscar apenas jogos do tipo 'game'")
    void deveBuscarSomenteJogosDoTipoGame() {
        List<Game> jogos = steamService.searchGameByName("Portal");
        assertFalse(jogos.isEmpty());

        for (Game jogo : jogos) {
            assertEquals("game", jogo.getType().toLowerCase());
        }
    }

    @Test
    @DisplayName("Deve retornar null para appId inválido")
    void deveRetornarNullParaAppIdInvalido() {
        Game jogo = steamService.getGameInfo(999999);
        assertNull(jogo, "Esperado null para appId inválido");
    }

}

