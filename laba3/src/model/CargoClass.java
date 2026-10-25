package model;

public enum CargoClass {
    FLAMMABLE("flammable"),
    CHEMICALLY_DANGEROUS("chemically dangerous"),
    EXPLOSIVE("explosive"),
    SAFE("safe"),
    SPECIAL_IMPORTANCE("special importance");

    private String title;

    CargoClass(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
