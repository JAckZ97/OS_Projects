import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.io.*;

public class scheduler implements Runnable {

    private static String output = "";
    private static List<process> processList = new ArrayList<>();
    private static List<process> completeList = new ArrayList<>();
    private static Semaphore hasCPU = new Semaphore(2);
    //hasCPU is the semaphore, and when we set it to 2,
    //it allow us to run 2 threads at the same time


    public static void main(String args[]) {

        try{
            PrintWriter writer = new PrintWriter(new File("vm.txt"));
            writer.print("");
            writer.close(); //Remove all text from vm file

            Scanner sc = new Scanner (new File("processes.txt"));
            sc.nextLine();
            do {
                int[] processInput;
                String line = sc.nextLine();
                String[] splitString = line.split("\\s");

                processInput = new int[2];
                for (int n = 0; n < 2; n++)
                {
                    processInput[n] = Integer.parseInt(splitString[n]);
                }
                process p1 = new process (processInput[0],processInput[1], hasCPU);

                //Add new Process to the list
                processList.add(p1);
            } while (sc.hasNextLine());
            sc.close();

            //Initialize the new scheduler and runnable object
            scheduler Scheduler = new scheduler();
            Scheduler.run();

            output += ("______________________________ \n");
            output += ("Waiting Time for processes: \n");
            for (int i = 0; i < completeList.size(); i++) { // Printing waiting times
                process processI = completeList.get(i);
                output += (processI.getName() + ": " + processI.getWaitingTime() + "\n" );
            }

            System.out.println(output);
            try (PrintWriter out = new PrintWriter("output.txt")) {
                out.println(output);
            }

        }
        catch(Exception e)
        {
            System.out.println("Errors");
            e.printStackTrace();
        }
    }

    public static void addToOutputString(String addString)
    {
        output += addString;
    }

    @Override
    public void run() {
        while (processList.size() > 0) {
            try {
                hasCPU.tryAcquire();                            // Get permission to access CPU
                process next = processList.get(0);              // Prepare next process to run
                for (int i = 0; i < processList.size(); i++) {  // Check the shortest started process
                    process process = processList.get(i);
                    if (process.getRemainingTime() < next.getRemainingTime() && process.isStarted()) {
                        next = process;
                    }
                }
                for (int i = 0; i < processList.size(); i++) {  // Check for newly method
                    process process = processList.get(i);
                    if (process.getCurrentTime() >= process.getArrivalTime() && !process.isStarted()) {
                        next = process;
                        break;
                    }
                }
                if (next.getCurrentTime() < next.getArrivalTime()) { //Check to see if a process is being started before it arrives
                    next.setCurrentTime(next.getCurrentTime() + 1);
                    next = null;
                }
                if (next != null) {
                    next.run();
                    if (next.isFinished()){
                        completeList.add(next);
                        processList.remove(next);
                    }
                }
            } finally {
                hasCPU.release();                               //Releases permission to access CPU
            }
        }
    }

}
