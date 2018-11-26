package cll;

import java.util.Random;

public class CLDriver {
    static ConcurrentList l;
    static final long baseTime = System.nanoTime();

    /**
     * Class to run operations on a shared linked list
     */
    protected static class CLThread extends Thread {
        int id, ops, maxItem;
        Random r;

        public CLThread(int id, int ops, int maxItemValue) {
            this.id = id;
            this.ops = ops;
            this.maxItem = maxItemValue;
            r = new Random();
        }

        /**
         * Generate a random integer
         * Code from https://stackoverflow.com/questions/363681/how-to-generate-random-integers-within-a-specific-range-in-java/363692#363692
         *
         * @param min Min value
         * @param max Max Value
         * @return Integer from range [min, max]
         */
        public int randInt(int min, int max) {
            return r.nextInt((max - min) + 1) + min;
        }

        @Override
        public void run() {
            int opChoice, itemChoice, item2Choice;
            for (int i = 0; i < ops; i++) {
                opChoice = randInt(0, 3);
                itemChoice = randInt(1, maxItem);
                switch (opChoice) {
                    case 0:
                        this.contains(itemChoice);
                        break;
                    case 1:
                        this.insert(itemChoice);
                        break;
                    case 2:
                        this.delete(itemChoice);
                        break;
                    case 3:
                        item2Choice = randInt(1, maxItem);
                        this.replace(itemChoice, item2Choice);
                        break;
                    default:
                        break;
                }
            }
        }

        void contains(int item) {
            System.out.println(String.format("%d,%d,contains,started,%d,,", System.nanoTime() - baseTime, id, item));
            boolean res = l.contains(item);
            System.out.println(String.format("%d,%d,contains,finished,%d,,%s", System.nanoTime() - baseTime, id, item, res));
        }

        void insert(int item) {
            System.out.println(String.format("%d,%d,insert,started,%d,,", System.nanoTime() - baseTime, id, item));
            boolean res = l.insert(item);
            System.out.println(String.format("%d,%d,insert,finished,%d,,%s", System.nanoTime() - baseTime, id, item, res));
        }

        void delete(int item) {
            System.out.println(String.format("%d,%d,delete,started,%d,,", System.nanoTime() - baseTime, id, item));
            boolean res = l.delete(item);
            System.out.println(String.format("%d,%d,delete,finished,%d,,%s", System.nanoTime() - baseTime, id, item, res));
        }

        void replace(int oldItem, int newItem) {
            System.out.println(String.format("%d,%d,replace,started,%d,%d,", System.nanoTime() - baseTime, id, oldItem, newItem));
            boolean res = l.replace(oldItem, newItem);
            System.out.println(String.format("%d,%d,replace,finished,%d,%d,%s", System.nanoTime() - baseTime, id, oldItem, newItem, res));
        }
    }

    public static void main(String[] args) {
        // arg 1: Upper limit for range of numbers to select from default [1, 10]
        // arg 2: Number of threads to use, default 4
        // arg 3: Operations to perform per thread
        // arg 4: type of workload - read heavy or write heavy
        int upperLimit = 10;
        if (args.length > 0) {
            upperLimit = Integer.valueOf(args[0]);
        }

        int numThreads = 4;
        if (args.length > 1) {
            numThreads = Integer.valueOf(args[1]);
        }

        int opsPerThread = 100;
        if (args.length > 2) {
            opsPerThread = Integer.valueOf(args[2]);
        }

        boolean readHeavy = true;
        if (args.length > 3) {
            readHeavy = Boolean.valueOf(args[3]);
        }

        CLThread[] threads = new CLThread[numThreads];
        l = new ConcurrentList();

        System.out.println("Timestamp,Thread,Operation,Status,Item 1,Item 2,Result");

        // initialize the threads
        for (int i = 1; i <= numThreads; i++) {
            threads[i - 1] = new CLThread(i, opsPerThread, upperLimit);
        }

        // start threads
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        // wait till all threads have stopped
        boolean threadsRunning = true;
        while (threadsRunning) {
            threadsRunning = false;
            for (int i = 0; i < numThreads; i++) {
                if (threads[i].isAlive()) {
                    threadsRunning = true;
                }
            }
        }
    }
}
