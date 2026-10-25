class Model {

    public int private_number;
    public int high;
    public int low;
    public int mid = 50;

    public Model(int privat_number) {
        this.private_number = privat_number;
        high = 100;
        low = 1;
        CountMid();
    }

    public boolean CheckTrustLowerHigher(int lower, int higher) {
        if (lower < higher) {
            return true;
        }
        return false;
    }

    public void CountMid() {
        mid = low + (high - low) / 2;
    }

    public void ChangeLowToMid() {
        low = mid + 1;
        CountMid();
    }

    public void ChangeHighToMid() {
        high = mid - 1;
        CountMid();
    }
}
