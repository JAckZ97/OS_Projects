class FindDefective extends Thread{

    static void findDefective(int[] bulbPosition, int[] bulbStatusArray, int position, counter counter1, counter counter2) {

        // Print out sorting array
        // For every time staring the new thread, there will be an empty line, which help us to declare easier
        System.out.print(" \n");
        System.out.print("The bulb status array is: \n");
        for (int value : bulbStatusArray) {
            System.out.println(value);
        }

        // Check if the current sorting array have defective bulb
        int Defective = 1;
        for (int value : bulbStatusArray) {
            //if any defective bulb status of 0, it will cause the Defective parameter change from 1 to 0
            Defective *= value;
        }

        // No defective bulb
        if (Defective == 1) {
            System.out.println("No Defective bulb in current sorting array");
        }

        // Locate the defective bulb when no more sub-array and it is defective bulb
        else if (bulbStatusArray.length == 1 && Defective == 0) {
            //counter2 is count the defective bulb into bulbPosition array
            bulbPosition[counter2.getCount()] = position;
            counter2.increment();
            System.out.println("Find defective bulb located in position: " + (position+1));

        }
        // Use pivot to divide the array into two
        else {
            int pivot = bulbStatusArray.length / 2;
            System.out.println("# "+ pivot + " bulb is used at pivot point now. " );

            // Recursion on the left side.
            int[] leftArray = new int[pivot];
            // Copy the sub-array as new bulbStatusArray array
            System.arraycopy(bulbStatusArray, 0, leftArray, 0, leftArray.length);

            // Do threading on left side
            Thread leftThread = new Thread(() -> {
                //counting the number of thread
                counter1.increment();
                findDefective(bulbPosition, leftArray, position, counter1, counter2);
            });
            leftThread.start();
            System.out.println("Starting left sub-array");

            // Recursion on the right side.
            int[] rightArray = new int[bulbStatusArray.length - leftArray.length];
            System.arraycopy(bulbStatusArray, pivot, rightArray, 0, rightArray.length);

            // Do threading on right side
            Thread rightThread = new Thread(() -> {
                //counting the number of thread
                counter1.increment();
                findDefective(bulbPosition, rightArray, position + pivot, counter1, counter2);
            });
            rightThread.start();
            System.out.println("Starting right sub-array");
            try {
                rightThread.join();
                System.out.println("End right sub-array");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                leftThread.join();
                System.out.println("End left sub-array");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
