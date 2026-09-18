package domain;

public class MacroRatio {

    private double carbo;
    private double protein;
    private double fat;

    public MacroRatio(double carbo, double protein, double fat) {
        this.carbo = carbo;
        this.protein = protein;
        this.fat = fat;
    }

    public double getCarbo() {
        return carbo;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    @Override
    public String toString() {
        return String.format(
                "탄수화물 %.1f%% / 단백질 %.1f%% / 지방 %.1f%%",
                carbo, protein, fat
        );
    }
}