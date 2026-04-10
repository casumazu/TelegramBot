package com.example.SpringBot.model;

import lombok.Getter;

@Getter
public enum Drink {

    // Большие напитки (350 мл)
    CAPPUCCINO_LARGE("Капучино", DrinkSize.LARGE, 130, "Классический эспрессо с нежной молочной пенкой и бархатистой пеной"),
    LATTE_LARGE("Латте", DrinkSize.LARGE, 130, "Мягкий кофейный напиток с большим количеством молока"),
    MOCCACCINO_LARGE("Моккачино", DrinkSize.LARGE, 130, "Эспрессо с шоколадом, молоком и взбитыми сливками"),

    // Маленькие напитки (200 мл)
    RAF_BANANA("Раф Банановый", DrinkSize.SMALL, 100, "Нежный раф-кофе с натуральным банановым сиропом"),
    MOCCACCINO_SMALL("Моккачино", DrinkSize.SMALL, 100, "Эспрессо с шоколадом и молоком"),
    LATTE_SMALL("Латте", DrinkSize.SMALL, 100, "Мягкий кофейный напиток с молоком"),
    CAPPUCCINO_SMALL("Капучино", DrinkSize.SMALL, 100, "Классический эспрессо с молоком и пенкой"),
    HOT_CHOCOLATE("Горячий шоколад", DrinkSize.SMALL, 100, "Насыщенный горячий шоколад на молоке"),
    MILK_CHOCOLATE("Молочный шоколад", DrinkSize.SMALL, 100, "Нежный напиток на основе молока с шоколадом"),
    AMERICANO("Американо", DrinkSize.SMALL, 100, "Классический чёрный кофе на основе эспрессо");

    private final String displayName;
    private final DrinkSize size;
    private final int price;
    private final String description;

    Drink(String displayName, DrinkSize size, int price, String description) {
        this.displayName = displayName;
        this.size = size;
        this.price = price;
        this.description = description;
    }

    public String getFormattedPrice() {
        return price + " ₽";
    }

    public String getSizeLabel() {
        return size.getVolume() + " мл";
    }

    @Getter
    public enum DrinkSize {
        LARGE("Большие", 350),
        SMALL("Маленькие", 200);

        private final String label;
        private final int volume;

        DrinkSize(String label, int volume) {
            this.label = label;
            this.volume = volume;
        }
    }
}
