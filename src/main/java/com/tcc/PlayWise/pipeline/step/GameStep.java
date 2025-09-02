package com.tcc.PlayWise.pipeline.step;

import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.pipeline.core.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
public class GameStep {

    @Bean
    public Step<Game> markIfFree() {
        return (game, params) -> {
            String price = game.getPrice() != null ? game.getPrice().toLowerCase() : "";
            if (price.contains("gratuito") || price.contains("free") || price.trim().equals("0.0")) {
                params.put("skipPriceSteps", true);
                System.out.println("[Pipeline] Jogo gratuito detectado: " + game.getTitle());
            }
            return game;
        };
    }

    @Bean
    public Step<Game> standardizeTitle() {
        return (game, params) -> {
            if (game.getTitle() != null) {
                game.setTitle(game.getTitle().trim().toUpperCase());
            }
            return game;
        };
    }

    @Bean
    public Step<Game> standardizeType() {
        return (game, params) -> {
            if (game.getType() != null) {
                game.setType(game.getType().trim().toUpperCase());
            }
            return game;
        };
    }

    @Bean
    public Step<Game> standardizePrice() {
        return (game, params) -> {
            if (Boolean.TRUE.equals(params.get("skipPriceSteps"))) return game;

            if (game.getPrice() != null) {
                String raw = game.getPrice().toLowerCase().trim();

                if (raw.contains("gratuito") || raw.contains("free")) {
                    params.put("parsedPrice", 0.0);
                    params.put("originalPrice", 0.0);
                    return game;
                }

                raw = raw.replace("r$", "")
                        .replace("us$", "")
                        .replace(",", ".")
                        .replaceAll("[^\\d.]", "");

                try {
                    double parsed = Double.parseDouble(raw);
                    params.put("parsedPrice", parsed);
                    params.put("originalPrice", parsed);
                } catch (NumberFormatException e) {
                    System.err.println("[Pipeline] Preço inválido ignorado: " + game.getPrice());
                    params.put("parsedPrice", 0.0);
                    params.put("originalPrice", 0.0);
                }
            }
            return game;
        };
    }

    @Bean
    public Step<Game> taxPrice() {
        return (game, params) -> {
            if (Boolean.TRUE.equals(params.get("skipPriceSteps"))) return game;

            double price = (double) params.getOrDefault("parsedPrice", 0.0);
            double imposto = price * 0.15;
            double semImposto = price - imposto;

            params.put("parsedPrice", semImposto);
            params.put("imposto", imposto);
            return game;
        };
    }

    @Bean
    public Step<Game> storeRatePrice() {
        return (game, params) -> {
            if (Boolean.TRUE.equals(params.get("skipPriceSteps"))) return game;

            double price = (double) params.getOrDefault("parsedPrice", 0.0);
            double rate = price * 0.30;
            double finalPrice = price - rate;

            params.put("parsedPrice", finalPrice);
            params.put("taxa", rate);
            return game;
        };
    }

    @Bean
    public Step<Game> finalizePrice() {
        return (game, params) -> {
            if (Boolean.TRUE.equals(params.get("skipPriceSteps"))) {
                game.setPrice("Gratuito");
                return game;
            }

            Object parsed = params.get("parsedPrice");

            if (parsed instanceof Double price && price > 0) {
                Locale localeBR = new Locale("pt", "BR");
                String precoFormatado = String.format(localeBR, "R$ %.2f", price).replace(".", ",");
                game.setPrice(precoFormatado);
            } else {
                game.setPrice("Gratuito");
            }

            return game;
        };
    }


    @Bean
    public Step<Game> standardizeDate() {
        return (game, params) -> {
            if (game.getReleaseDate() != null && !game.getReleaseDate().isBlank()) {
                String rawDate = game.getReleaseDate().trim();

                if (rawDate.equalsIgnoreCase("coming soon")) {
                    game.setReleaseDate("Em breve");
                    return game;
                }

                String[] possiblePatterns = {
                        "yyyy-MM-dd",
                        "dd/MM/yyyy",
                        "MM/dd/yyyy",
                        "dd-MM-yyyy",
                        "MMM dd, yyyy",
                        "dd MMM, yyyy",
                        "d MMM, yyyy"
                };

                for (String pattern : possiblePatterns) {
                    try {
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
                        LocalDate parsedDate = LocalDate.parse(rawDate, inputFormatter);

                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        String formatted = parsedDate.format(outputFormatter);
                        game.setReleaseDate(formatted);
                        return game;
                    } catch (Exception ignored) {}
                }

                System.err.println("[Pipeline] Formato de data inválido: " + rawDate);
            }

            return game;
        };
    }
}
