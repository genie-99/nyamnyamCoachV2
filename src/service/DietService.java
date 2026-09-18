package service;

import domain.Food;
import domain.MacroRatio;

import java.util.List;

public class DietService {
    public MacroRatio calculateActualRatio(List<Food> foods) {
        double carbo = 0;
        double protein = 0;
        double fat = 0;

        for (Food food : foods) {
            carbo += food.getCarbo();
            protein += food.getProtein();
            fat += food.getFat();
        }
        return calculateRatioFromGrams(carbo, protein, fat);
    }

    // 비율 계산에는 탄수화물·단백질 4kcal/g, 지방 9kcal/g을 사용한다.
    public MacroRatio calculateRatioFromGrams(double carbo, double protein, double fat) {
        double carboEnergy = carbo * 4;
        double proteinEnergy = protein * 4;
        double fatEnergy = fat * 9;
        double totalEnergy = carboEnergy + proteinEnergy + fatEnergy;

        if (totalEnergy <= 0) {
            return new MacroRatio(0, 0, 0);
        }
        return new MacroRatio(
                carboEnergy / totalEnergy * 100,
                proteinEnergy / totalEnergy * 100,
                fatEnergy / totalEnergy * 100
        );
    }

    // 칼로리 제한에는 CSV의 에너지(kcal) 값을 사용한다.
    public double calculateTotalCalories(List<Food> foods) {
        double total = 0;
        for (Food food : foods) {
            total += food.getKcal();
        }
        return total;
    }

    public double calculateDifference(MacroRatio target, MacroRatio actual) {
        return Math.abs(target.getCarbo() - actual.getCarbo())
                + Math.abs(target.getProtein() - actual.getProtein())
                + Math.abs(target.getFat() - actual.getFat());
    }

    // 기존 프로젝트의 BMI별 목표 비율을 유지한다.
    public MacroRatio getTargetRatio(double bmi) {
        if (bmi < 18.5) {
            return new MacroRatio(55, 15, 30);
        } else if (bmi < 23) {
            return new MacroRatio(55, 20, 25);
        } else if (bmi < 25) {
            return new MacroRatio(50, 20, 30);
        } else {
            return new MacroRatio(50, 20, 30);
        }
    }
}
