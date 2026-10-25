import interfaces.freightTransport;

public class Truck extends Vehicle implements freightTransport {
    public Truck(int dimensions, int weight, int speed, CargoClass cargoClass) {
        super(dimensions, weight, speed, cargoClass);
    }

    @Override
    public String getType() {
        return "Truck";
    }

    @Override
    public void checkFreight() {
        System.out.println("Cargo checked.");
    }

    @Override
    public boolean canTransportCargo() {
        return true;
    }
}
