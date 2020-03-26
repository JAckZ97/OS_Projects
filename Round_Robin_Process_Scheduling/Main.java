public class Main {

    public static void main(String[] args){

        Thread roundRobinScheduler = new Thread(new RoundRobinScheduler());
        roundRobinScheduler.start();
    }
}
