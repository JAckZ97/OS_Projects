class counter {

    private int count = 0;

    synchronized int getCount() {
        return count;
    }

    synchronized void increment() {
        count++;
    }
}
