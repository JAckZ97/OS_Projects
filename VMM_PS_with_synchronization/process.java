import java.io.File;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;


public class process implements Runnable {
	private Semaphore hasCPU; 			// Use to pause and resume the access to CPU
	private String name; 				// Process name
	private int arrivalTime; 			// Time of arrival
	private int processTime; 			// Time of process
	private int quantum; 				// Time needed for process
	private int waitingTime; 			// Time spent in queue
	private int elapsedTime; 			// Elapsed runtime
	private int remainingTime; 			// Remaining time to completion
	private boolean isStarted; 			// If process has been started
	private boolean isFinished; 		// If process has been completed
	private static int currentTime =0; 	// Static member tracking the system time
	private static int nameIterator =1;	// Tracks the name of the process
	private static Scanner sc = null;
	private static vmm vmmObject = null;

	process(int arrivalT, int processT, Semaphore hasCPU) {
		setArrivalTime(arrivalT * 1000);
		setProcessTime(processT * 1000);
		setRemainingTime(processT * 1000);
		setWaitingTime(0);
		setStarted(false);
		quantum = (int) ((0.1 * processT) * 1000);
		if (quantum < 1000) {
			quantum = 1000;
		}
		this.hasCPU = hasCPU;
		name = "Process " + nameIterator; // Names the process based on arriving order.
		nameIterator += 1;
		if (sc == null)
		{
			try{
				sc = new Scanner(new File("commands.txt")); // Get the commands
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
		if (vmmObject == null)
		{
			try {
				Scanner memConfigSc = new Scanner(new File("memconfig.txt"));
				int memConfigSize = memConfigSc.nextInt();
				vmmObject = new vmm(memConfigSize);
				memConfigSc.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}

	@Override
	public void run() {
		try {
			hasCPU.tryAcquire(); 				// Get permission to access CPU
			if (!isStarted) { 					// Checks to see if the process has started
				scheduler.addToOutputString("Clock: " + currentTime + ", " + name + ", " + "Started" + "\n");
				setStarted(true);
			}
			scheduler.addToOutputString("Clock: " + currentTime + ", " + name + ", " + "Resumed" + "\n");

			// Running command for the process
			int commandTime= 0;
			while ((commandTime += new Random().nextInt(100)*10) < this.quantum) {
				String thisCommand = getNextCommand();
				if (thisCommand != "ENDofLine") {
					vmmObject.runCommand(thisCommand, name,currentTime + commandTime);
				}
			}

			elapsedTime += quantum; 			//Incrementing elapsed time from a quantum
			remainingTime -= quantum; 			//Decrementing remaining time from a quantum
			if(elapsedTime >= processTime){ 	//Checking for process completion
				currentTime += processTime - (elapsedTime - quantum);
				remainingTime = 0;
				elapsedTime = processTime;
				setFinished(true);
				setWaitingTime(currentTime - arrivalTime - processTime);
			} else{
				currentTime += quantum;
			}

			scheduler.addToOutputString("Clock: " + currentTime + ", " + name + ", " + "Paused" + "\n");
			if (isFinished) {
				scheduler.addToOutputString("Clock: " + currentTime + ", " + name + ", " + "Finished" + "\n");
			}
		} finally{
			hasCPU.release();  					//Releases permission to access CPU
		}

	}

	private String getNextCommand(){
		if (sc.hasNextLine())
		{
			return sc.nextLine();
		}
		else
		{
			return "ENDofLine";
		}
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getProcessTime() {
		return processTime;
	}

	public void setProcessTime(int processTime) {
		this.processTime = processTime;
	}

	public int getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public int getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(int i) {
		currentTime = i;
	}

	public int getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		this.remainingTime = remainingTime;
	}

	public String getName() {
		return name;
	}

}
