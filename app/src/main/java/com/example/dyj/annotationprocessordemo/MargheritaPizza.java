package com.example.dyj.annotationprocessordemo;

import com.example.dyj.library.Factory;

@Factory(
        id = "Margherita",
        type = Meal.class
)
public class MargheritaPizza implements Meal {
    @Override
    public float getPrice() {
        return 6.5f;
    }
}
