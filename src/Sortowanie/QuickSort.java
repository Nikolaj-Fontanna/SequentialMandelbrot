package Sortowanie;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

import static java.lang.System.exit;

// klasa mierząca czas dla różnych realizacji quicksort: sekwensyjnie, forkjoin z Task, frokJoin z Action, dla różnych thresholdów
// myślałem żeby jeszce zrobić gołe wątki, dzilić tablicę i wrzucać na początku każdemu kawałek tablicy do posortowania

public class QuickSort {

    public static void printArray(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    // funkcja generująca tablicę losowych intów
    public static int[] generateRandomArray(int size) {
        Random rand = new Random();
        int[] arr = new int[size];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = rand.nextInt();
        }
        return arr;
    }

    // funkcja sprawdzająca posortowanie min-to-max
    public static boolean isSorted(int[] arr){
        for (int i = 0; i < arr.length-1; i++) {
            if (arr[i] > arr[i+1]){
                return false;
            }
        }
        return true;
    }

    //funkcja zamieniająca elementy o danych indeksach miejscami
    public static void swap(int[] arr, int left, int right){
        int tmp = arr[left];
        arr[left]=arr[right];
        arr[right]=tmp;
    }

    //funkcja dzieląca tablicę
    public static int partition(int[] arr, int left0, int right0, int flag){

        int left,right,mid,pivot;

        switch (flag){
            // podział z wymianą lewy na prawy
            case 0:
                // w Javie nie ma wskaźników, więc korzystamy z indeksów w tablicy
                left = left0;
                right = right0-1;

                // wybór osi - tu jako ostatni element
                pivot = arr[right0];

                // dopóki wskaźniki się nie miną
                while(left <= right){
                    //znajdź element większy niż pivot z lewej
                    while(left <= right && arr[left] <= pivot){
                        left++;
                    }
                    //znajdź element mniejszy niż pivot z prawej
                    while(left <= right && arr[right] >= pivot){
                        right--;
                    }
                    //zamień mniejszą i większą wartość
                    if (left<right){
                        swap(arr, left, right);
                    }
                }
                //zamień lewy z indeksem pivota
                swap(arr, left, right0);

                return left;

            //drugi sposób
            case 1:
                // w Javie nie ma wskaźników, więc korzystamy z indeksów w tablicy
                left = left0;
                right = right0;

                // wybór osi - element środkowy
                //System.out.println((left+right)/2);
                pivot = arr[(left+right)/2];

                while(left<=right){
                    while(arr[left]<pivot){left++;}
                    while(arr[right]>pivot){right--;}

                    if (left<=right){
                        swap(arr,left,right);
                        left++;
                        right--;
                    }
                }

                //trzeci sposób - blokowy - ponoć super szybki
                // TO DO

                return left;
            default:
                System.out.println("Wrong flag!");
        }

        return 1;
    }

    //funkcja sortująca tablicę intów
    public static void quickSortSequential(int[] arr, int left, int right){

        if (left<right){
            int mid = partition(arr, left, right, 0);
            quickSortSequential(arr,left,mid-1);
            quickSortSequential(arr,mid,right);

        }
    }

    //bazowane na: https://www.geeksforgeeks.org/quick-sort-using-multi-threading/
    // Dodałem threshold wielowątkowości (ale po co robili RecursiveTask a nie Action?)
    //może się wydawać że nie jest szybciej, ale dla większych jest
    /*
        ForkJoinPool to pula dobra do zadań rekurencyjnych. Fork nazywamy podział zadania, a join zwrot zadania (czyli zakończenie rekurencji w gałęzi)
        należy stworzyć zadanie które daje się podzielić - RecursiveTask (lub Action jeśli ma być void) z metodą compute
        schemat
            Result compute(Problem problem) {
            if (problem is small) {
                directly solve problem
            } else {
                split problem into independent parts
                fork new subtasks to solve each part
                join all subtasks
                compose result from subresults
            }
           }
            fork() - dzieli pracę
            compute() - wylicza wynik
            join() - czeka na wynik operacji
     */
    public static class quickSortParallelTask extends RecursiveTask<Integer> {
        int left0, right0;
        int[] arr;

        public quickSortParallelTask(int[] arr, int left0, int right0){
            this.arr = arr;
            this.left0 = left0;
            this.right0 = right0;
        }

        @Override
        protected Integer compute(){
            if (left0<right0){
                int mid = partition(arr, left0, right0, 0);
                quickSortParallelTask left = new quickSortParallelTask(arr,left0,mid-1);
                quickSortParallelTask right = new quickSortParallelTask(arr,mid,right0);

                left.fork();
                right.compute();

                left.join();

            }
            return null;
        }

    }

    public static class quickSortParallelAction extends RecursiveAction {
        int left0, right0;
        int[] arr;

        int THRESHOLD;

        public quickSortParallelAction(int[] arr, int left0, int right0, int THRESHOLD){
            this.arr = arr;
            this.left0 = left0;
            this.right0 = right0;
            this.THRESHOLD = THRESHOLD;
        }

        @Override
        protected void compute(){
            if ((right0 - left0)<= THRESHOLD){
                quickSortSequential(arr, left0, right0);
            }
            else{
                int mid = partition(arr, left0, right0, 0);
                quickSortParallelAction left = new quickSortParallelAction(arr,left0,mid-1, THRESHOLD);
                quickSortParallelAction right = new quickSortParallelAction(arr,mid,right0,  THRESHOLD);

                left.fork();
                right.compute();

                left.join();

            }
        }

    }

//    // po co się męczyć z pulą wątków?
//    //klasa wątku - rekurencyjnie wywołuje klasę sortującą
//    static class QuickSortTask implements Runnable{
//
//        int[] arr;
//        int left, right;
//
//        public QuickSortTask(int[] arr, int left, int right){
//            this.arr = arr;
//            this.left = left;
//            this.right = right;
//        }
//        @Override
//        public void run()
//        {
//           quickSortParallel2.sort(arr, left, right);
//        }
//    }
//    public static class quickSortParallel2{
//
//        private static int maxSize = 1000; //rozmiar tablicy przy której rezygujemy z wielowątkowści
//        private int finishedThreads=0;
//        private int finishedCount=0;
//        private int taskCount=0;
//        private static int nThreads=0;
//
//        static ExecutorService pool = Executors.newFixedThreadPool(nThreads);
//        //pool.shutdown()
//
//        public static void sort(int[] arr, int left0, int right0){
//
//            //sekwencyjny quicksort
//            if (right0-left0+1 < maxSize){
//                quickSortSequential(arr,left0, right0);
//            }
//            else {
//                int mid = partition(arr, left0, right0, 0);
//                pool.execute(new QuickSortTask( arr, mid+1, right0));
//
//                // Recurse to assign the lower half to its thread
//                sort(arr, left0, mid - 1);
//
//            }
//        }
//    }


    public static void main(String[] args) {

        int iterMean = 10; //number of iteration to average
        long []times = new long[iterMean];

        int[] sizes = { 10,(int)1e2,(int)1e3,(int)1e4, (int) 1e5, (int)1e6, (int) 1e7, (int) 1e8}; // rozmiary tablic, 1e9 nie udźwignie moja Java ogr. heap space

        // flagi: 0-sekwencyjnie, 1-frokjoinTask, 2-forkjoinAction, 3-1 ale z czasem tworzenia puli, 4-analogincznie dla 2
        // threshold biorę 1, czyli całość tablicy wielowątkowo
        int flag = 3;
        FileWriter fw = null;

        //Ogólne porównanie
//        try {
//            fw = new FileWriter("out_fjptask2.txt");
//
//            for (int size :sizes) {
//                for (int i = 0; i < iterMean; i++) {
//                    int[] arr = generateRandomArray(size); //1e8 ok
//
//                    if(flag==0){
//                        long start = System.nanoTime();
//                        quickSortSequential(arr,0,arr.length-1);
//                        long finish = System.nanoTime();
//                        times[i] = finish - start;
//                    }
//                    if(flag==1){
//                        ForkJoinPool pool = new ForkJoinPool(4);
//                        long start = System.nanoTime();
//                        pool.invoke(new quickSortParallelTask(arr,0,arr.length-1));
//                        long finish = System.nanoTime();
//                        pool.shutdown();
//                        times[i] = finish - start;
//                    }
//                    if(flag==2){
//                        ForkJoinPool pool = new ForkJoinPool(4);
//                        long start = System.nanoTime();
//                        pool.invoke(new quickSortParallelAction(arr,0,arr.length-1, 1));
//                        long finish = System.nanoTime();
//                        pool.shutdown();
//                        times[i] = finish - start;
//                    }
//                    if(flag==3){
//                        long start = System.nanoTime();
//                        ForkJoinPool pool = new ForkJoinPool(4);
//                        pool.invoke(new quickSortParallelTask(arr,0,arr.length-1));
//                        pool.shutdown();
//                        long finish = System.nanoTime();
//                        times[i] = finish - start;
//                    }
//                    if(flag==4){
//                        long start = System.nanoTime();
//                        ForkJoinPool pool = new ForkJoinPool(4);
//                        pool.invoke(new quickSortParallelAction(arr,0,arr.length-1,1));
//                        pool.shutdown();
//                        long finish = System.nanoTime();
//                        times[i] = finish - start;
//                    }
//                }
//                System.out.println("Done with size: "+size);
//                fw.write(size + "," + Arrays.stream(times).average().orElse(0) + "\n");
//            }
//            fw.close();
//
//        } catch (IOException e) {
//            System.out.println("Write to file failed");
//            exit(1);
//        }

        //test różnych thresholdów w FJP

        int[] thresholds = {1,10,100,1000,(int)1e4, (int)1e5, (int)1e6, (int)1e7, (int)1e8};

        try {
            fw = new FileWriter("out_thrseholds.txt");
            for (int thr:thresholds) {

                for (int i = 0; i < iterMean; i++) {
                    int[] arr = generateRandomArray((int)1e8); //1e8 ok

                    ForkJoinPool pool = new ForkJoinPool(4);
                    long start = System.nanoTime();
                    pool.invoke(new quickSortParallelAction(arr,0,arr.length-1, thr));
                    long finish = System.nanoTime();
                    pool.shutdown();
                    times[i] = finish - start;
                }
                System.out.println("Done with thr: "+thr);
                fw.write(thr + "," + Arrays.stream(times).average().orElse(0) + "\n");
            }
            fw.close();
        }catch (IOException e) {
                System.out.println("Write to file failed");
                exit(1);
            }




        //testy
//        ForkJoinPool pool = new ForkJoinPool(4);// liczba wątków, lub ForkJoinPool.commonPool(); co daje wszystkie obecne -1
//        float start = System.nanoTime();
//        //quickSortSequential(arr,0,arr.length-1);
//        //pool.invoke(new quickSortParallelTask(arr,0,arr.length-1));
//        //pool.invoke(new quickSortParallelAction(arr,0,arr.length-1, 100));
//        float finish = System.nanoTime();
//
//        double time = (double) ((finish-start)/1e9);
//        printArray(arr);
//        System.out.println(isSorted(arr)+" in just "+time+" sec");

    }

}
