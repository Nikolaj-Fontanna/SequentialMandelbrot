package Mandelbrot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ColorMandelbrot {

    //SKRYPT GENERUJĄCY OBRAZEK ZBIORU MANDELBROTA

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        double[] Xarr =  {-2.1, 0.6};
        double[] Yarr =  {-1.2, 1.2};

        int size = 8192;

        ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        double start = System.nanoTime();

        //BufferedImage img = Mandelbrot.generateMandelbrotImage(size,size, Xarr, Yarr, 100, null);
        BufferedImage img = Mandelbrot.generateMandelbrotImageParallelThreads(size,size, Xarr, Yarr, 200, null);
        //BufferedImage img = Mandelbrot.generateMandelbrotImageParallelPool(size,size, Xarr, Yarr, 100, null);
        //BufferedImage img = Mandelbrot.generateMandelbrotImageParallelPool3(size,size, Xarr, Yarr, 100, null,ex);
        //BufferedImage img = Mandelbrot.generateMandelbrotImageParallelPoolArray(size,size, Xarr, Yarr, 100, null, 1024);
        //BufferedImage img = Mandelbrot.generateMandelbrotImageParallelPoolArrayNoEx(size,size, Xarr, Yarr, 100, null, 4, ex);
        ex.shutdown();
        ex.awaitTermination(1, TimeUnit.DAYS);

        double finish = System.nanoTime();

        System.out.println((finish-start)/1e9 + " sekund");

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // wyłącz po zamknięicu okna
        frame.setVisible(true);

    }
}