import java.io.*;
// Java Program to illustrate reading from FileReader
// using BufferedReader

public class Main {

    public static void main(String[] args) throws Exception {
        File file = new File("input.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        int arraySize = Integer.parseInt(br.readLine());
        int[] bulbStatusArray = new int[arraySize];

        // counter1 is count the number of thread
        // counter2 is count the number of defective bulb in bulbPosition array
        counter counter1 = new counter();
        counter counter2 = new counter();

        // load the bulb status in to array bulbStatusArray
        for (int i = 0; i < arraySize; i++) {
            bulbStatusArray[i] = Integer.parseInt(br.readLine());
        }

        // Print out bulbStatusArray
        System.out.print("\n");
        System.out.print("Loading bulb status \n");
        for (int i = 0; i < arraySize; i++) {
            System.out.println(bulbStatusArray[i]);
        }

        // initialize the new array to find the defective bulb positions
        int[] bulbPosition = new int[arraySize];

        // Multithreading recursion
        Thread mainThread = new Thread(() -> {
            FindDefective.findDefective(bulbPosition, bulbStatusArray, 0, counter1, counter2);
            counter1.increment(); // counting the number of thread
        });

        mainThread.start();
        try {
            mainThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("\n");
        System.out.print("\n");
        System.out.print("---------------------------------------------\n");

        // Print out the positions of defective bulbs (0s).
        for (int i = 0; i < counter2.getCount(); i++) {
            System.out.println("Position of defective light bulb: " + (bulbPosition[i] + 1));
        }

        // Print out the number of threads used.
        System.out.println("Number of Threads used: " + counter1.getCount());

        System.out.print("---------------------------------------------\n");

    }
}
