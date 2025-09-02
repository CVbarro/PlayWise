package com.tcc.PlayWise.controller;

import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.service.GameService;
import com.tcc.PlayWise.service.SteamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SteamController.class)
@Import(SteamControllerTest.MockServiceConfig.class)
@DisplayName("Testes do SteamController")
class SteamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SteamService steamService;

    @Autowired
    private GameService gameService;

    @Test
    @DisplayName("Deve retornar lista de Game processado")
    void deveRetornarListaDeGameProcessado() throws Exception {
        Game game = new Game("PORTAL", "GAME", "12/03/2022", "R$ 76,49");
        game.setPriceOriginal(129.99);
        game.setPriceParsed(76.49);
        game.setTax(19.5);
        game.setRate(33.99);

        List<Game> mockList = List.of(game);
        when(steamService.searchGameByName("Portal"))
                .thenReturn(List.of(new Game("Portal", "game", "12 Mar, 2022", "R$ 129,99")));
        when(gameService.processSteamGames(anyList()))
                .thenReturn(mockList);

        mockMvc.perform(get("/api/steam/game/search")
                        .param("name", "Portal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("PORTAL"))
                .andExpect(jsonPath("$[0].type").value("GAME"))
                .andExpect(jsonPath("$[0].releaseDate").value("12/03/2022"))
                .andExpect(jsonPath("$[0].price").value("R$ 76,49"))
                .andExpect(jsonPath("$[0].priceOriginal").value(129.99))
                .andExpect(jsonPath("$[0].priceParsed").value(76.49))
                .andExpect(jsonPath("$[0].tax").value(19.5))
                .andExpect(jsonPath("$[0].rate").value(33.99));
    }

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public SteamService steamService() {
            return Mockito.mock(SteamService.class);
        }

        @Bean
        public GameService gameService() {
            return Mockito.mock(GameService.class);
        }
    }
}
