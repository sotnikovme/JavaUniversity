
package model;

import model.CargoClass;

public abstract class Vehicle {
    private long id;
    protected int dimensions; // Габарит
    protected int weight;
    protected int speed;
    protected CargoClass cargoClass;

    public Vehicle(int dimensions, int weight, int speed, CargoClass cargoClass) {
        this.dimensions = dimensions;
        this.weight = weight;
        this.speed = speed;
        this.cargoClass = cargoClass;
    }

    public int getDimensions() {
        return dimensions;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWeight() {
        return weight;
    }

    public int getSpeed() {
        return speed;
    }

    public CargoClass getCargoClass() {
        return cargoClass;
    }

    public abstract String getType();

    @Override
    public String toString() {
        return getType() + ": dimensions " + dimensions + " cm, weight " + weight
                + " kg, speed " + speed + " km/h, cargo: " + cargoClass;
    }
}
