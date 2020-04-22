import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Scanner;

public class vmm {
    private String [] memStoreCommand = {"store"};
    private String [] memFreeCommand = { "release"};
    private String [] memLookupCommand = {"lookup" };
    private int time = 0;
    private Object[][] mainMemory;      // 2D Array which assign 1,2,3 respect to variableId, value, and last access time

    vmm(int size) {
        mainMemory = new Object[size][3];
    }

    public synchronized void runCommand(String command, String name, int time)
    {
        setTime(time);
        try {
            String[] commandSplit = command.split("\\s");
            if (Arrays.asList(memStoreCommand).contains(commandSplit[0].toLowerCase()))
            {
                scheduler.addToOutputString("Clock: " + time + ", " + name + ", ");
                scheduler.addToOutputString("Store: Variable " + commandSplit[1] + ", Value: " + commandSplit[2] + "\n");
                int value = Integer.parseInt(commandSplit[1]);
                memStore(commandSplit[1],value);
            }
            else if (Arrays.asList(memFreeCommand).contains(commandSplit[0].toLowerCase()))
            {
                scheduler.addToOutputString("Clock: " + time +
                        ", " + name + ", ");
                scheduler.addToOutputString("Release: Variable " + commandSplit[1] +
                        "\n");
                memFree(commandSplit[1]);
            }
            else if (Arrays.asList(memLookupCommand).contains(commandSplit[0].toLowerCase()))
            {
                int value = memLookup(commandSplit[1]);
                scheduler.addToOutputString("Clock: " + time +
                        ", " + name + ", ");
                scheduler.addToOutputString("Lookup: Variable " +
                        commandSplit[1] + ", Value " + value + "\n");
            }

        }
        catch (Exception e)
        {
            System.out.println(e);
            System.out.println("Errors in vmm");
        }
    }

    // Store variable variableId and value into memory or disk
    public void memStore(String variableId, int value) {
        boolean stored = false;

        for (int i = 0; i < mainMemory.length; i++ )
        {
            if (mainMemory[i][0] == null) {
                mainMemory[i][0] = variableId;
                mainMemory[i][1] = value;
                mainMemory[i][2] = time;
                stored = true;
            }
        }

        // If not stored in main memory, then store in disk
        if(!stored)
        {
            String toText = "\n" + variableId + " "+ value + " " + time;
            appendToDisk(toText);
        }
    }

    // Free variable from memory or disk space
    public void memFree(String variableId) throws FileNotFoundException {
        boolean deleted = false;

        for (int i = 0; i < mainMemory.length; i++ )
        {
            if (mainMemory[i][0].equals(variableId))
            {
                mainMemory[i][0] = null;
                mainMemory[i][1] = null;
                mainMemory[i][2] = null;
                deleted = true;
            }
        }

        // If not found in main memory, then remove from disk.
        if (!deleted)
        {
            removeVariableFromDisk(variableId);
        }
    }

    // Get value from memory or disk space
    // Swap the variable from disk to memory
    public int memLookup(String variableId) throws FileNotFoundException {
        int returnValue = -1;
        int mainMemoryHasSpace = -1;
        int smallestLastTime = (int) mainMemory[0][2];
        int smallestLastTimeIndex = 0;

        for (int i = 0; i < mainMemory.length; i++ )
        {
            // If the variableId exists in the main memory it returns its value.
            if (mainMemory[i][0].equals(variableId)){
                returnValue = (int)mainMemory[i][1];
                break;
            }

            else if (mainMemory[i][0] == null){
                mainMemoryHasSpace = i;
                break;
            }

            else if ((int)mainMemory[i][2] < smallestLastTime){
                smallestLastTime = (int)mainMemory[i][2];
                smallestLastTimeIndex = i;
            }
        }

        // If variableId not found in main memory, checking disk space
        // Swap if found in disk space
        if (returnValue == -1){

            File file = new File("vm.txt");
            Scanner sc = new Scanner (file);
            Object[] variableInDisk = null;
            do {
                String[] line = sc.nextLine().split("\\s");
                if (line[0].equals(variableId))
                {
                    variableInDisk = new Object[3];
                    variableInDisk[0] = line[0];
                    variableInDisk[1] = Integer.parseInt(line[1]);
                    variableInDisk[2] = Integer.parseInt(line[2]);
                    break;
                }
            }
            while(sc.hasNextLine());
            sc.close();

            // If variableId was not exist in disk, return -1
            if (variableInDisk == null){
                return returnValue;
            }

            // If main memory has space, add to main memory
            else if (mainMemoryHasSpace != -1){
                mainMemory[mainMemoryHasSpace] = variableInDisk;
                removeVariableFromDisk(variableId);
                return (int)variableInDisk[2];
            }

            // Swapping in from disk space and swapping out from memory
            scheduler.addToOutputString("Clock: " + time + ", Memory Manager, SWAP: " + "Variable " +
                    variableId + " with " + mainMemory[smallestLastTimeIndex][0] + "\n");
            removeVariableFromDisk(variableId); //removing variable from disk space

            // Append swapped out variable to disk space
            Object [] swappedVar= mainMemory[smallestLastTimeIndex];
            String toText = swappedVar[0] + " "+ swappedVar[1] + " " + swappedVar[2];
            appendToDisk(toText);

            // Swap in variable to memory
            mainMemory[smallestLastTimeIndex] = variableInDisk;
            returnValue = (int)mainMemory[smallestLastTimeIndex][1];
        }

        return returnValue;
    }

    // Adding variable to disk space
    private void appendToDisk (String toText)
    {
        try {
            Files.write(Paths.get("vm.txt"), toText.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            System.out.println ("Error appending to vm.txt");
        }
    }

    // Deleting variable from disk space
    private void removeVariableFromDisk (String variableId) throws FileNotFoundException {
        // Scan file to see if variableId is in the disk space and rewrite the vm.txt file
        File file = new File("vm.txt");
        Scanner sc = new Scanner (file);
        String outText = "";
        do {
            String line = sc.nextLine();
            String[] lineSplit = line.split("\\s");
            if (!lineSplit[0].equals(variableId) && !line.isEmpty()){
                outText += line + "\n";
            }
        }
        while(sc.hasNextLine());
        sc.close();

        //Write new disk text file.
        try (PrintWriter out = new PrintWriter("vm.txt")) {
            out.println(outText);
        }
    }

    public void setTime(int time){
        this.time = time;
    }

}


