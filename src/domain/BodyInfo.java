package domain;

public class BodyInfo {

    private double height;
    private double weight;

    public BodyInfo(double height, double weight) {
        this.height = height;
        this.weight = weight;
    }

    public double getBmi() {

        double heightMeter = height / 100.0;

        return weight / (heightMeter * heightMeter);
    }
}