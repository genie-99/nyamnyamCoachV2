package domain;

public class Food {
    long foodId;
    String foodName;
    double kcal;
    double protein;
    double fat;
    double carbo;
    double sugar;

    public Food(long foodId,String foodName,double carbo, double kcal, double protein, double fat, double sugar) {
        this.foodName = foodName;
        this.carbo = carbo;
        this.foodId = foodId;
        this.kcal = kcal;
        this.protein = protein;
        this.fat = fat;
        this.sugar = sugar;
    }

    public String getFoodName() {
        return foodName;
    }

    public double getKcal() {
        return kcal;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbo() {
        return carbo;
    }

    @Override
    public String toString() {
        return "Food{" +
                "foodId=" + foodId +
                ", foodName='" + foodName + '\'' +
                ", kcal=" + kcal +
                ", protein=" + protein +
                ", fat=" + fat +
                ", carbo=" + carbo +
                ", sugar=" + sugar +
                '}';
    }
}
