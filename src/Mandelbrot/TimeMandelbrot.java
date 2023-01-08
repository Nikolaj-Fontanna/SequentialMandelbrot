package Mandelbrot;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static java.lang.System.exit;

public class TimeMandelbrot {

    //SKRYPT LICZĄCY ŚREDNI CZAS DLA SEKWENCYJNEGO GENEROWANIA MANDELBROTA
    // flag==0 - sekwencyjnie
    // flag==1 - wielowątkowo, pojedyncze wątki
    // flag==2 - pula wątków, wszystko robi pula wątków (zdefinuj blockSize w jobie)
    // flag==3 - pula wątków, ale tworzymy jedną pulę i używamy jej wielokrotnie, czyli nie wliczamy do średniego czasu, czasu potrzebnego na stworzenie puli wątków

    public static void  main(String[] args) {

        int iterMean = 3; //number of iteration to average
        int iter = 200;
        double[] Xarr =  {-2.1, 0.6};
        double[] Yarr =  {-1.2, 1.2};
        int []sizes = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
        int []blockSizes = {4,8,16,32,64,128};
        int flag = 1;
        double out = 0;

            try {
                FileWriter fw = new FileWriter("out_threads04.txt");

                for (int size: sizes) {
                    // dla flag 2 i 4, należy podać blockSize - rozmiar bloku pikseli podawany jako job do puli wątków
                    if (flag ==2 || flag==3){
                        for (int blockSize: blockSizes) {
                            out = Mandelbrot.TimeMyMandelbrot(size, size, Xarr, Yarr, iter, iterMean, flag, blockSize);
                            System.out.println("Done for size: " + size + ", block size: "+ blockSize);
                            fw.write(size*size + "," + out + "," + blockSize + "\n"); // number of pixels and avg. time
                        }
                    }else {
                        out = Mandelbrot.TimeMyMandelbrot(size, size, Xarr, Yarr, iter, iterMean, flag, 0);
                        System.out.println("Done for size: " + size);
                        fw.write(size*size + "," + out + "\n"); // number of pixels and avg. time
                    }
                }
                fw.close();
            }
            catch (IOException | InterruptedException | ExecutionException e) {
                System.out.println("Write to file failed");
                exit(1);

            }

        }
    }

