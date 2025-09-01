package com.tcc.PlayWise.dto;

import lombok.Data;

@Data
public class GameDetailsDto {

    private String title;
    private String type;
    private String releaseDate;
    private double priceOriginal;
    private double priceFinal;
    private double rate;
    private double tax;
}
