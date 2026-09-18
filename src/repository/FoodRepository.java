package repository;

import domain.Food;
import java.util.*;

public class FoodRepository {

    private final List<Food> foods;

    public FoodRepository(List<Food> foods) {
        this.foods = foods;
    }

    public List<Food> findAll() {
        return Collections.unmodifiableList(foods);
    }

    public Food findByName(String foodName) {
        for(Food food : foods ) {
            if(food.getFoodName().equals(foodName)) {
                return food;
            }
        }
        return null;
    }

    public List<Food> findByNames(String input) {
        List<Food> selectedFoods = new ArrayList<>();

        String[] foodNames = input.split(",");

        for(String foodName : foodNames)  {
            foodName = foodName.trim();

            Food food = findByName(foodName);
            if(food == null) continue;
            selectedFoods.add(food);
        }
        return selectedFoods;
    }
}
