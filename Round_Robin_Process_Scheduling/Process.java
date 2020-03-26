public class Process implements Runnable {

    private Boolean cpuAccess;
    private double startingTime;
    private double remainingTime;
    private double starvingTime;
    private double waitingTime;
    private double allowedTime;

    public Process(double newST, double newRT) {
        this.cpuAccess = false; // default unlock the cpu
        this.startingTime = newST;
        this.remainingTime = newRT;
        this.starvingTime = 0;
        this.waitingTime = 0;   // set default values
        this.allowedTime = this.remainingTime * 0.1; // 10% of remaining time
    }

    public Boolean getCpuAccess() {
        return this.cpuAccess;
    }

    public double getStartingTime() {
        return this.startingTime;
    }

    public double getRemainingTime() {
        return this.remainingTime;
    }

    public double getStarvingTime() {
        return this.starvingTime;
    }

    public double getWaitingTime() {
        return this.waitingTime;
    }

    public double getAllowedTime() {
        return this.allowedTime;
    }

    public Boolean isFinished() {
        if (this.remainingTime > 0) { return false;
        } else { return true; }
    }

    public void addWaitingTime(double time) {
        this.waitingTime += time;
        this.starvingTime += time;
    }

    public void run() {
        this.cpuAccess = true; // Lock cpu access
        this.starvingTime = 0;  // Reset starving time

        if (this.remainingTime < 0.1) {  // Compute remaining time, in order to prevent loop forever, we set the thresh hold into 0.1 and break
            this.allowedTime = this.remainingTime; // Finish this process
        } else { this.allowedTime = this.remainingTime * 0.1; } // 10% of remaining time

        if (this.remainingTime < this.allowedTime) { //error catch
            this.allowedTime = this.remainingTime; }

        this.remainingTime -= this.allowedTime; //Update remaining time

        this.cpuAccess = false; //Unlock cpu access
    }
}