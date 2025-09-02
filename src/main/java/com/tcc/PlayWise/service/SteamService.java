package com.tcc.PlayWise.service;

import com.tcc.PlayWise.model.Game;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class SteamService {

    private static final Logger logger = LoggerFactory.getLogger(SteamService.class);
    private static final String BASE_URL = "https://store.steampowered.com/api/appdetails?appids=";
    private static final String SEARCH_URL = "https://store.steampowered.com/api/storesearch/?term=";

    private final HttpClient client = HttpClient.newHttpClient();

    public List<Game> searchGameByName(String name) {
        List<Game> results = new ArrayList<>();

        try {
            String url = SEARCH_URL + sanitize(name) + "&cc=br";
            HttpResponse<String> response = sendRequest(url);

            JsonArray items = JsonParser.parseString(response.body())
                    .getAsJsonObject()
                    .getAsJsonArray("items");

            if (items == null || items.isEmpty()) {
                logger.warn("Nenhum jogo encontrado para: {}", name);
                return results;
            }

            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                int appId = item.has("id") ? item.get("id").getAsInt() : -1;
                if (appId == -1) continue;

                Game game = getGameInfo(appId);
                if (game != null) {
                    results.add(game);
                } else {
                    results.add(buildFallbackGame(item, appId));
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao buscar jogos da Steam: {}", e.getMessage(), e);
        }

        return results;
    }

    private Game getGameInfo(int appId) {
        try {
            String url = BASE_URL + appId + "&cc=br&l=pt";
            HttpResponse<String> response = sendRequest(url);

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject gameData = root.getAsJsonObject(String.valueOf(appId)).getAsJsonObject("data");

            String title = safeGet(gameData, "name", "Título desconhecido");
            String type = extractType(gameData);
            String releaseDate = extractReleaseDate(gameData);
            String price = extractPrice(gameData);

            return new Game(title, type, releaseDate, price);

        } catch (Exception e) {
            logger.warn("Falha ao buscar detalhes do jogo (appId: {}): {}", appId, e.getMessage());
            return null;
        }
    }

    /** -------- Métodos auxiliares -------- */

    private HttpResponse<String> sendRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String extractType(JsonObject gameData) {
        if (gameData.has("type")) {
            return safeGet(gameData, "type", "Tipo indefinido");
        }
        if (gameData.has("genres")) {
            JsonArray genres = gameData.getAsJsonArray("genres");
            if (!genres.isEmpty()) {
                return safeGet(genres.get(0).getAsJsonObject(), "description", "Tipo indefinido");
            }
        }
        return "Tipo indefinido";
    }

    private String extractReleaseDate(JsonObject gameData) {
        if (gameData.has("release_date")) {
            return safeGet(gameData.getAsJsonObject("release_date"), "date", "-");
        }
        return "-";
    }

    private String extractPrice(JsonObject gameData) {
        if (gameData.has("price_overview")) {
            return safeGet(gameData.getAsJsonObject("price_overview"), "final_formatted", "Gratuito");
        }
        return "Gratuito";
    }

    private Game buildFallbackGame(JsonObject item, int appId) {
        String title = safeGet(item, "name", "Título desconhecido");
        String type = "desconhecido";
        String releaseDate = "-";
        String price = "Indisponível";

        if (item.has("price") && item.getAsJsonObject("price").has("final")) {
            double finalPrice = item.getAsJsonObject("price").get("final").getAsDouble() / 100.0;
            price = "R$ " + String.format("%.2f", finalPrice).replace(".", ",");
        }

        logger.warn("Usando fallback para jogo {} (appId: {})", title, appId);
        return new Game(title, type, releaseDate, price);
    }

    private String safeGet(JsonObject obj, String key, String fallback) {
        return obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString()
                : fallback;
    }

    private String sanitize(String input) {
        return input.trim().replaceAll("\\s+", "%20");
    }
}
