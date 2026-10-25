import interfaces.car_transport;

public class Main {
    public static void main(String[] args) {
        Checkpoint checkpoint = new Checkpoint();

        Vehicle[] vehicles = {
                new Car(470, 1800, 55, CargoClass.SAFE),
                new Car(520, 3000, 55, CargoClass.SAFE),
                new Truck(1100, 11000, 35, CargoClass.FLAMMABLE),
                new Bus(1300, 19000, 40, CargoClass.SAFE),
                new Trailer(1500, 23000, 25, CargoClass.EXPLOSIVE),
                new CarWithTrailer(950, 6000, 45, CargoClass.CHEMICALLY_DANGEROUS),
                new Truck(1000, 9000, 30, CargoClass.SPECIAL_IMPORTANCE)
        };

        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);

            if (vehicle instanceof car_transport) {
                car_transport passengerVehicle = (car_transport) vehicle;
                System.out.println("Passenger capacity: " + passengerVehicle.getPassengerCapacity());
                System.out.println("Can carry 4 passengers: "
                        + passengerVehicle.canCarryPassengers(4));
            }

            if (checkpoint.Pass(vehicle)) {
                System.out.println("Result: passage allowed.");
            } else {
                System.out.println("Result: passage denied. " + checkpoint.getReason());
            }

            System.out.println();
        }
    }
}
