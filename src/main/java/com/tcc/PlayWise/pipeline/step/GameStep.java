package com.tcc.PlayWise.pipeline.step;

import com.tcc.PlayWise.model.Game;
import com.tcc.PlayWise.pipeline.core.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
public class GameStep {

    @Bean
    public Step<Game> standardizeTitle() {
        return (game, ignored) -> {
            if (game.getTitle() != null) {
                game.setTitle(game.getTitle().trim().toUpperCase());
            }
            return game;
        };
    }

    @Bean
    public Step<Game> standardizeType() {
        return (game, ignored) -> {
            if (game.getType() != null) {
                game.setType(game.getType().trim().toUpperCase());
            }
            return game;
        };
    }

    @Bean
    public Step<Game> standardizePrice() {
        return (game, ignored) -> {
            String raw = game.getPrice() != null ? game.getPrice().trim().toLowerCase() : "";

            if (raw.contains("gratuito") || raw.contains("free") || raw.equals("0") || raw.equals("0.00")) {
                game.setPriceParsed(0.0);
                game.setPriceOriginal(0.0);
                return game;
            }

            String cleaned = raw
                    .replaceAll("r\\$", "")
                    .replaceAll("us\\$", "")
                    .replaceAll("usd", "")
                    .replaceAll("u\\$", "")
                    .replaceAll("eur", "")
                    .replaceAll("€", "")
                    .replaceAll("[^\\d,\\.]", "")
                    .replace(",", ".");

            if (cleaned.chars().filter(ch -> ch == '.').count() > 1) {
                int lastDot = cleaned.lastIndexOf('.');
                cleaned = cleaned.substring(0, lastDot).replace(".", "") + cleaned.substring(lastDot);
            }

            try {
                double parsed = new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP).doubleValue();
                game.setPriceParsed(parsed);
                game.setPriceOriginal(parsed);
            } catch (NumberFormatException e) {
                System.err.printf("[Pipeline] Erro ao converter preço '%s' para jogo '%s'%n", raw, game.getTitle());
                game.setPriceParsed(-1.0);
                game.setPriceOriginal(-1.0);
            }

            return game;
        };
    }

    @Bean
    public Step<Game> markIfFree() {
        return (game, ignored) -> {
            if (game.getPriceParsed() == 0.0) {
                game.setPrice("Gratuito");
            }
            return game;
        };
    }

    @Bean
    public Step<Game> taxPrice() {
        return (game, ignored) -> {
            double price = game.getPriceParsed();
            if (price <= 0.0) return game;

            double tax = BigDecimal.valueOf(price * 0.15).setScale(2, RoundingMode.HALF_UP).doubleValue();
            double newPrice = BigDecimal.valueOf(price - tax).setScale(2, RoundingMode.HALF_UP).doubleValue();

            game.setTax(tax);
            game.setPriceParsed(newPrice);

            return game;
        };
    }

    @Bean
    public Step<Game> storeRatePrice() {
        return (game, ignored) -> {
            double price = game.getPriceParsed();
            if (price <= 0.0) return game;

            double rate = BigDecimal.valueOf(price * 0.30).setScale(2, RoundingMode.HALF_UP).doubleValue();
            double newPrice = BigDecimal.valueOf(price - rate).setScale(2, RoundingMode.HALF_UP).doubleValue();

            game.setRate(rate);
            game.setPriceParsed(newPrice);

            return game;
        };
    }

    @Bean
    public Step<Game> finalizePrice() {
        return (game, ignored) -> {
            double price = game.getPriceParsed();

            if (price == 0.0) {
                game.setPrice("Gratuito");
            } else if (price > 0.0) {
                Locale localeBR = new Locale("pt", "BR");
                String precoFormatado = String.format(localeBR, "R$ %.2f", price).replace(".", ",");
                game.setPrice(precoFormatado);
            } else {
                game.setPrice("Indisponível");
            }

            return game;
        };
    }

    @Bean
    public Step<Game> standardizeDate() {
        return (game, ignored) -> {
            if (game.getReleaseDate() == null || game.getReleaseDate().isBlank()) return game;

            String rawDate = game.getReleaseDate().trim();
            if (rawDate.equalsIgnoreCase("coming soon")) {
                game.setReleaseDate("Em breve");
                return game;
            }

            String[] patterns = {
                    "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy",
                    "MMM dd, yyyy", "dd MMM, yyyy", "d MMM, yyyy"
            };

            for (String pattern : patterns) {
                try {
                    DateTimeFormatter input = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
                    LocalDate parsed = LocalDate.parse(rawDate, input);
                    DateTimeFormatter output = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    game.setReleaseDate(parsed.format(output));
                    return game;
                } catch (Exception ignoredParse) {}
            }

            System.err.println("[Pipeline] Formato de data inválido: " + rawDate);
            return game;
        };
    }
}
