import interfaces.freightTransport;

public class Trailer extends Vehicle implements freightTransport {
    public Trailer(int dimensions, int weight, int speed, CargoClass cargoClass) {
        super(dimensions, weight, speed, cargoClass);
    }

    @Override
    public String getType() {
        return "Trailer";
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
