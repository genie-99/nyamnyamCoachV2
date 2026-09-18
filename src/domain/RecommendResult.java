package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendResult {
    private final List<Food> recommendedFoods;
    private final MacroRatio finalRatio;
    private final double totalCalories;
    private final double difference;

    public RecommendResult(List<Food> recommendedFoods, MacroRatio finalRatio,
                           double totalCalories, double difference) {
        this.recommendedFoods = Collections.unmodifiableList(new ArrayList<>(recommendedFoods));
        this.finalRatio = finalRatio;
        this.totalCalories = totalCalories;
        this.difference = difference;
    }

    public List<Food> getRecommendedFoods() {
        return recommendedFoods;
    }

    public MacroRatio getFinalRatio() {
        return finalRatio;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public double getDifference() {
        return difference;
    }

}
