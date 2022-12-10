import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Mandelbrot {

    //    funkcja wykonująca iter iteracji w ciągu Mandelbrota
//    zwraca liczbę iteracji po któej ciąg rozbiegł lub max jesli  modul nie przekroczyl 2
//    smoothColoring przeksztalca liczbe iteracji na nieco gladszą, więc kolory kładą się lepiej
    public static int mandelbrot(Complex c, int iter, Boolean smoothColoring){

        Complex zn = new Complex(0,0);
        int i = 0;
        // szybciej niż liczyć moduł I guess? - ale inf-inf=0 (sic!) wychodził taki fascynujący kwadrat z dziurką z tego
        // while ( (Math.abs(zn1.getReal() - zn.getReal()) > epsilon) & (Math.abs(zn1.getImaginary() - zn.getImaginary()) > epsilon) ){

        while(zn.Cmodule() <= 2 & i < iter){
            zn = Complex.Cadd(Complex.Cmultiply(zn,zn),c) ; //zn1 = zn^2 + c
            i++;
        }

        if (smoothColoring) {
            if (i == iter) {
                return iter;
            } else {
                return (int) (i + 1 -Math.log(Math.log(zn.Cmodule())/Math.log(2)));
            }
        }else {
            return i;
        }


    }

    //    funkcja zwaracająca obraz w BufferedImage
//    input:
//      szerokość wysokość w pikselach
//      zakres parametru c - minmax x oraz minmax y w dwóch tablicy
//      liczba iteracji
//      nazwa pliku, jesli ma być utworzony obrazek jpg, lub NULL jesli nie
//
//    przypominajka - metoda statyczna to taka do której nie musi istnieć obiekt, czyli statyczna powinna być każda "wolna" funkcja
//    pole statyczne (zmienna) to taka która należy do klasy a nie do konkretnej instancji - jedna na klasę
    public static BufferedImage generateMandelbrotImage(int width, int height, double[] Xrange, double[] Yrange, int iter, String filename){
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        if (Xrange.length != 2  && Yrange.length != 2){
            throw new IllegalArgumentException("Range must contain 2 doubles");
        }

        // loop over all pixels
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {

                //MAPOWANIE WARTOŚCI
                double cx = (Xrange[1] - Xrange[0]) * x/img.getWidth() + Xrange[0];
                double cy = (Yrange[1] - Yrange[0]) * y/img.getHeight() + Yrange[0];
                Complex c = new Complex(cx,cy);

                //MANDELBROT ITERATION
                float out = mandelbrot(c, iter, false);

                //SETTING COLOR
                // kolor jest ustalany na okręgu HUE w przestrzeni barw HSV/HSB
                // za https://www.codingame.com/playgrounds/2358/how-to-plot-the-mandelbrot-set/adding-some-colors
                float hue = out/iter; //*4 ;// funkcja hsbToRGB bierze mantysę i mnoży przez 360 //mnożąc hue przez 2,3,4... można dostać ciekawy efekt
                float saturation = 1.0F; // float 0-1
                float brightness = 1.0F; //float 0-1
                if (out == iter){brightness = 0.0F;} //dla zbiegających = dochodzi do max iteracji ustaw kolor czarny

                img.setRGB(x,y, java.awt.Color.HSBtoRGB(hue,saturation,brightness));

                //Czarnobiały
//                if(out==iter){
//                    img.setRGB(x,y, new Color(0,0,0).getRGB());
//                }
//                else {
//                    img.setRGB(x,y, new Color(255, 255, 255).getRGB());
//                }

            }

        }
        if (filename != null){
            File outputfile = new File(filename+".jpg");
            try {
                ImageIO.write(img, "jpg", outputfile);
                System.out.println("Image was saved");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return img;
    }

    //Metoda mierząca średni czas generacji obrazka
    public static double TimeMyMandelbrot(int width, int height, double[] Xrange, double[] Yrange, int iter, int iterMean){

        long []times = new long[iterMean];

        for (int i = 0; i < iterMean; i++) {

            long start = System.nanoTime();
            BufferedImage img = Mandelbrot.generateMandelbrotImage(width, height, Xrange, Yrange, iter, null);
            long finish = System.nanoTime();

            times[i] = finish - start;
        }
        long sum = 0;
        for (long time:times) {
            sum+=time;
        }

        return 1.0d*sum/times.length;
    }

}
