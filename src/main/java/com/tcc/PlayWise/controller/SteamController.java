package com.tcc.PlayWise.controller;

import com.tcc.PlayWise.dto.GameDetailsDTO;
import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.service.GameService;
import com.tcc.PlayWise.service.SteamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/steam")
public class SteamController {

    private final SteamService steamService;
    private final GameService gameService;

    public SteamController(SteamService steamService, GameService gameService) {
        this.steamService = steamService;
        this.gameService = gameService;
    }

    @GetMapping("/game/search")
    public List<GameDetailsDTO> searchGame(@RequestParam String name) {
        List<Game> rawGames = steamService.searchGameByName(name);
        return gameService.processSteamGames(rawGames);
    }
}
