package com.example.dyj.annotationprocessordemo;

import com.example.dyj.library.Factory;

@Factory(
        id = "Calzone",
        type = Meal.class
)
public class CalzonePizza implements Meal {
    @Override
    public float getPrice() {
        return 8.0f;
    }
}
