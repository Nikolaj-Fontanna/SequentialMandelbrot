import java.io.FileWriter;
import java.io.IOException;

import static java.lang.System.exit;

public class TimeMandelbrot {

    public static void  main(String[] args) {

        int iterMean = 25; //number of iteration to average
        int iter = 200;
        double[] Xarr =  {-2.1, 0.6};
        double[] Yarr =  {-1.2, 1.2};
        int []sizes = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};

            try {
                FileWriter fw = new FileWriter("out.txt");

                for (int size: sizes) {
                    double out = Mandelbrot.TimeMyMandelbrot(size, size, Xarr, Yarr, iter, iterMean);
                    System.out.println("Done for size: " + size);

                    fw.write(size + "," + out + "\n");

                }
                fw.close();
            }
            catch (IOException e) {
                System.out.println("Write to file failed");
                exit(1);

            }

        }
    }

