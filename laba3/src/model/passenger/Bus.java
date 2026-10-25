package model.passenger;

import interfaces.car_transport;
import model.CargoClass;
import model.Vehicle;

public class Bus extends Vehicle implements car_transport {
    private final int passengerCapacity = 50;

    public Bus(int dimensions, int weight, int speed, CargoClass cargoClass) {
        super(dimensions, weight, speed, cargoClass);
    }

    @Override
    public String getType() {
        return "Bus";
    }

    @Override
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    @Override
    public boolean canCarryPassengers(int passengerCount) {
        return passengerCount <= passengerCapacity;
    }
}
