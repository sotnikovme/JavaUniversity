package model.freight;

import interfaces.freightTransport;
import model.CargoClass;
import model.Vehicle;

public class CarWithTrailer extends Vehicle implements freightTransport {
    public CarWithTrailer(int dimensions, int weight, int speed, CargoClass cargoClass) {
        super(dimensions, weight, speed, cargoClass);
    }

    @Override
    public String getType() {
        return "Car with trailer";
    }

    @Override
    public void checkFreight() {
        System.out.println("Cargo checked.");
    }

    @Override
    public boolean canTransportCargo() {
        return getCargoClass() != CargoClass.EXPLOSIVE;
    }
}
