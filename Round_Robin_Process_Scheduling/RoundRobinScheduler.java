import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RoundRobinScheduler implements Runnable {

    public void run() {
        try {
            // Read input.txt file and save it to temp array
            Scanner inputTextFile = new Scanner(new File("input.txt"));
            List<int[]> tempArray = new ArrayList<>();
            while (inputTextFile.hasNextLine()) {
                int temp[] = new int[2];
                temp[0] = inputTextFile.nextInt();
                temp[1] = inputTextFile.nextInt();
                tempArray.add(temp);
            }
            inputTextFile.close();


            // Print input.txt
            System.out.print("input.txt\n");
            for (int[] ints : tempArray) {
                System.out.print(ints[0] + "\t");
                System.out.print(ints[1] + "\n");
            }
            System.out.print("End of input.txt"+"\n");


            // Create all queues
            FileWriter writer = new FileWriter("output.txt", false);
            writer.write("Sample “output.txt”: "+ "\n");
            writer.write("Simulating Round-Robin Process Scheduling:"+ "\n\n");

            DecimalFormat d0 = new DecimalFormat("0"); // show 0 decimal places
            DecimalFormat d2 = new DecimalFormat("0.00"); // show 2 decimal places

            double time = 1; //set default starting time
            int numOfProcess = tempArray.size();
            List<Process> load = new ArrayList<>();
            List<Process> ready = new ArrayList<>(); // number of process - number of cpu (1)
            List<Process> running = new ArrayList<>(); // single-core, thus 1
            List<Process> finished  = new ArrayList<>(); // number of process terminated

            // Load the processes, in the sample there are three processes
            for (int i = 0; i < numOfProcess; i++) {
                load.add(new Process(tempArray.get(i)[0], tempArray.get(i)[1]));
            }

            while (finished.size() < numOfProcess) { // check if all process executed

                if (load.size() != 0) {
                    int indexST = 0;
                    double startingTime = load.get(indexST).getStartingTime();
                    for (int i = 0; i < load.size(); i++) {
                        double tempStartingTime = load.get(i).getStartingTime();
                        if (tempStartingTime < startingTime) {
                            startingTime = tempStartingTime;
                            indexST = i;
                        }
                    }
                    move(load, running, indexST);

                    // write to output.txt
                    writer.write("[Time " + d2.format(time) + "]\t[Process "
                            + d0.format(running.get(0).getStartingTime()) + "]\t[Started]\t[Remaining "
                            + d2.format(running.get(0).getRemainingTime()) + "]\t\t START \n");
                }


                // Find shortest remaining time through processes in ready queue
                else if (ready.size() != 0) {

                    int indexNextPriority = 0;
                    double starvationConstant = 1;// set starvationConstant reference number
                    double starvationBase = 1.8; // set starvationBase reference number

                    double nextPriority = Math.pow(starvationBase,
                            starvationConstant * ready.get(indexNextPriority).getStarvingTime())
                            + (1 / ready.get(indexNextPriority).getRemainingTime());

                    for (int i = 0; i < ready.size(); i++) {
                        double tempNextPriority = Math.pow(starvationBase,
                                (starvationConstant * ready.get(i).getStarvingTime()))
                                + (1 / ready.get(i).getRemainingTime());
                        if (tempNextPriority > nextPriority) {
                            nextPriority = tempNextPriority;
                            indexNextPriority = i;
                        }
                    }

                    move(ready, running, indexNextPriority);
                }

                // write to output.txt
                writer.write(
                        "[Time " + d2.format(time) + "]\t[Process " + d0.format(running.get(0).getStartingTime())
                                + "]\t[Resumed]\t[Remaining " + d2.format(running.get(0).getRemainingTime())
                                + "]\n");

                // Run current process
                running.get(0).run();

                // Manage time of scheduler and processes
                time = time + running.get(0).getAllowedTime();
                for (int j = 0; j < load.size(); j++) {
                    load.get(j).addWaitingTime(running.get(0).getAllowedTime());
                }
                for (int j = 0; j < ready.size(); j++) {
                    ready.get(j).addWaitingTime(running.get(0).getAllowedTime());
                }

                // Paused process and writing to output.txt
                writer.write(
                        "[Time " + d2.format(time) + "]\t[Process " + d0.format(running.get(0).getStartingTime())
                                + "]\t[Paused]\t[Remaining " + d2.format(running.get(0).getRemainingTime())
                                + "]\t\n");

                // Finished process
                if (running.get(0).isFinished()) {
                    writer.write("[Time " + d2.format(time) + "]\t[Process "
                            + d0.format(running.get(0).getStartingTime()) + "]\t[Finished]\t[Remaining "
                            + d2.format(running.get(0).getRemainingTime()) + "]\t\t END \n");
                    move(running, finished);
                }

                // Interrupted process
                else {
                    move(running, ready);
                }

                // Check running queue size
                if (running.size() != 0) {
                    writer.write("\n");
                }
            }

            // Print output.txt
            writer.write("--------------\n");
            writer.write("Waiting times: \n");
            while (finished.size() != 0) {
                int indexST = 0;
                double startingTime = finished.get(indexST).getStartingTime();
                for (int i = 0; i < finished.size(); i++) {
                    double tempStartingTime = finished.get(i).getStartingTime();
                    if (tempStartingTime < startingTime) {
                        startingTime = tempStartingTime;
                        indexST = i;
                    } }

                writer.write("Process " + finished.get(indexST).getStartingTime() + ": "
                        + finished.get(indexST).getWaitingTime() + "\n");

                finished.remove(indexST);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    // Move front queue to back queue
    public static void move(List<Process> departure, List<Process> destination) {
        destination.add(departure.get(0));
        departure.remove(0);
    }

    // Move at index queue to back queue
    public static void move(List<Process> departure, List<Process> destination, int index) {
        destination.add(departure.get(index));
        departure.remove(index);
    }
}