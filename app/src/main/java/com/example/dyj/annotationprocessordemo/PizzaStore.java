package com.example.dyj.annotationprocessordemo;

public class PizzaStore {

    private MealFactory mMealFactory = new MealFactory();
    public Meal order(String id){
        return mMealFactory.create(id);
    }

}
