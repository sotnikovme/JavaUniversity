import interfaces.car_transport;

public class Car extends Vehicle implements car_transport {
    private final int passengerCapacity = 5;

    public Car(int dimensions, int weight, int speed, CargoClass cargoClass) {
        super(dimensions, weight, speed, cargoClass);
    }

    @Override
    public String getType() {
        return "Passenger car";
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
