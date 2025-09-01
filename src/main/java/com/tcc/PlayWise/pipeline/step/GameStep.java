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
            if(game.getType() != null) {
                game.setType(game.getType().trim().toUpperCase());
            }
            return game;
        };
    }

    @Bean
    public Step<Game> standardizePrice() {
        return (game, params) -> {
            if (game.getPrice() != null) {
                try {
                    double parsed= Double.parseDouble(game.getPrice().replace(",", "."));
                    params.put("parsedPrice", parsed);
                    params.put("originalPrice", parsed);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Preço inválido: " + game.getPrice());
                }
            }
            return game;
        };
    }

    @Bean
    public Step<Game> taxPrice() {
        return (game, params) -> {
            if (!params.containsKey("parsedPrice")) {
                throw new IllegalStateException("Preço não foi convertido antes de aplicar imposto.");
            }
            double price = (double) params.get("parsedPrice");
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
            if (!params.containsKey("parsedPrice")) {
                throw new IllegalStateException("Preço não foi convertido antes de aplicar imposto.");
            }
            double price = (double) params.get("parsedPrice");
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
            double finalPrice = (double) params.get("parsedPrice");
            Locale localeBR = new Locale("pt", "BR");
            String precoFormatado = String.format(localeBR, "%.2f", finalPrice);
            game.setPrice(precoFormatado);
            return game;
        };
    }

    @Bean
    public Step<Game> standardizeDate() {
        return (game, params) -> {
            if (game.getReleaseDate() != null && !game.getReleaseDate().isBlank()) {
                String rawDate = game.getReleaseDate().trim();

                String[] possiblePatterns = {
                        "yyyy-MM-dd",
                        "dd/MM/yyyy",
                        "MM/dd/yyyy",
                        "dd-MM-yyyy",
                        "MMM dd, yyyy"
                };

                for (String pattern : possiblePatterns) {
                    try {
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(pattern, new Locale("pt", "BR"));
                        LocalDate parsedDate = LocalDate.parse(rawDate, inputFormatter);

                        // Formata para padrão brasileiro
                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        String formatted = parsedDate.format(outputFormatter);
                        game.setReleaseDate(formatted);
                        return game;
                    } catch (Exception ignored) {
                        // tenta próximo padrão
                    }
                }

                throw new IllegalArgumentException("Formato de data inválido: " + rawDate);
            }

            return game;
        };
    }

}
