package com.tcc.PlayWise.model;


import lombok.Data;

@Data
public class Game {
    public Game(String title, String type, String releaseDate, String price) {
        this.title = title;
        this.type = type;
        this.releaseDate = releaseDate;
        this.price = price;
    }

    private String title;
    private String type;
    private String releaseDate;
    private String price;
    private double priceParsed;
    private double priceOriginal;
    private double tax;
    private double rate;
    private Store store;



}
