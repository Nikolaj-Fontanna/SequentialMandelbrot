import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Mandelbrot {

    //KLASA Z FUNKCJAMI DO GENEROWANIA ZBIORÓW MANDELBROTA


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

    //Mandelbrot 2 - więcej operacji wewnątrz funkcji
    public static void mandelbrot2(int iter, Boolean smoothColoring, double []Xrange, double []Yrange, int x, int y, BufferedImage img){

        //MAPOWANIE WARTOŚCI
        double cx = (Xrange[1] - Xrange[0]) * x/img.getWidth() + Xrange[0];
        double cy = (Yrange[1] - Yrange[0]) * y/img.getHeight() + Yrange[0];
        Complex c = new Complex(cx,cy);

        Complex zn = new Complex(0,0);
        int i = 0;
        while(zn.Cmodule() <= 2 & i < iter){
            zn = Complex.Cadd(Complex.Cmultiply(zn,zn),c) ; //zn1 = zn^2 + c
            i++;
        }

        float out=0;

        if (smoothColoring) {
            if (i == iter) {
                //return iter;
                out = iter;
            } else {
                //return (int) (i + 1 -Math.log(Math.log(zn.Cmodule())/Math.log(2)));
                out = (int) (i + 1 -Math.log(Math.log(zn.Cmodule())/Math.log(2)));
            }
        }else {
            //return i;
            out = i;
        }

        //SETTING COLOR
        // kolor jest ustalany na okręgu HUE w przestrzeni barw HSV/HSB
        // za https://www.codingame.com/playgrounds/2358/how-to-plot-the-mandelbrot-set/adding-some-colors
        float hue = out/iter; //*4 ;// funkcja hsbToRGB bierze mantysę i mnoży przez 360 //mnożąc hue przez 2,3,4... można dostać ciekawy efekt
        float saturation = 1.0F; // float 0-1
        float brightness = 1.0F; //float 0-1
        if (out == iter){brightness = 0.0F;} //dla zbiegających = dochodzi do max iteracji ustaw kolor czarny

        img.setRGB(x,y, java.awt.Color.HSBtoRGB(hue,saturation,brightness));
    }
//    public static float mandelbrot2noImg(int iter, Boolean smoothColoring, double []Xrange, double []Yrange, int x, int y, int width, int height){
//
//        //MAPOWANIE WARTOŚCI
//        double cx = (Xrange[1] - Xrange[0]) * x/width + Xrange[0];
//        double cy = (Yrange[1] - Yrange[0]) * y/height + Yrange[0];
//        Complex c = new Complex(cx,cy);
//
//        Complex zn = new Complex(0,0);
//        int i = 0;
//        while(zn.Cmodule() <= 2 & i < iter){
//            zn = Complex.Cadd(Complex.Cmultiply(zn,zn),c) ; //zn1 = zn^2 + c
//            i++;
//        }
//
//        float out=0;
//
//        if (smoothColoring) {
//            if (i == iter) {
//                //return iter;
//                out = iter;
//            } else {
//                //return (int) (i + 1 -Math.log(Math.log(zn.Cmodule())/Math.log(2)));
//                out = (int) (i + 1 -Math.log(Math.log(zn.Cmodule())/Math.log(2)));
//            }
//        }else {
//            //return i;
//            out = i;
//        }
//        return out;
//    }
//
//    //klasa IntPair - żeby ułatwić przekazywanie par współrzędnych do funkcji
//    public static class IntPair{
//        private int x;
//        private int y;
//
//        public IntPair(){
//            this.x = 0;
//            this.y = 0;
//        }
//
//        public IntPair(int x, int y){
//            this.x = x;
//            this.y = y;
//        }
//
//        public int getX() {
//            return x;
//        }
//
//        public int getY() {
//            return y;
//        }
//
//        public void setX(int x) {
//            this.x = x;
//        }
//
//        public void setY(int y) {
//            this.y = y;
//        }
//    }

    //Mandelbrot 3 - więcej operacji wewnątrz funkcji oraz przekazujemy większe kawałki obrazka do przetworzenia, na potrzeby jobów - dawanie im pojedynczych pikseli działa słabo
    public static void mandelbrotArray(int iter, Boolean smoothColoring, double []Xrange, double []Yrange, int blockNumber, int blockSize, int width, int height, BufferedImage img) {

        // OBLICZAMY PIERWSZY PIKSEL
        int x = blockNumber * blockSize % width;
        int y = blockNumber * blockSize / width;

        //PĘTLA PO KOLEJNYCH PIKSELACH OD PIERWSZEGO DO OSTATNIEGO Z BLOKU
        for (int i = 0; i < blockSize; i++) {
            //System.out.println(x +" " + y);
            x++;

            //KONIEC RZĘDU
            if (x >= width) {
                x = 0;
                y++;
            }
            //KONIEC OBRAZKA - ostatni blok może być nie pełny
            if (y >= height) {
                break;
            }
            //PRZEKAZUJEMY WARTOŚCI DO FUNKCJI DLA POJEDYNCZEGO PIKSELA
            mandelbrot2(iter, smoothColoring, Xrange, Yrange, x, y, img);
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

    // klasa definiująca pojedynczy wątek - te wątki są potem tworzone w metodzie generateMandelbrotImageParallelThreads
    // wątek otrzymuje tablicę pikseli do przeliczenia a nasepnie zwraca je poprzez metode getValues
    public static class MandelbrotThread01 extends Thread{

        private final int iter;
        private final double[] Xrange;
        private final double[] Yrange;
        private BufferedImage img;
        private final int orderNumber; // liczbą porządkowa - co który piksel ma liczyćten wątek
        public MandelbrotThread01(BufferedImage img, int i, double[] Xrange, double[] Yrange, int iter) {
            this.img = img;
            this.orderNumber = i;
            this.Xrange = Xrange;
            this.Yrange = Yrange;
            this.iter = iter;
        }

        @Override
        public void run() {

            // loop over all pixels
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = orderNumber; y < img.getHeight(); y+=4) {

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

                }

            }

        }
    }

    //    funkcja zwaracająca obraz w BufferedImage, jak wyżej tylko równolegle poprzez tworzenie wątków:
        // czyli: jest klasa wątku który robi zadanie, ta metoda tworzy te wątki i je uruchamia, każdy wątek ma dostęp do tego samego obrazka w którym modyfikuje piksele

    public static BufferedImage generateMandelbrotImageParallelThreads(int width, int height, double[] Xrange, double[] Yrange, int iter, String filename) throws InterruptedException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        if (Xrange.length != 2  && Yrange.length != 2){
            throw new IllegalArgumentException("Range must contain 2 doubles");
        }

        // TWORZYMY WĄTKI (tyle wątków ile jest dostępnych rdzeni na maszynie)
        //MandelbrotThread01[] threads = new MandelbrotThread01[Runtime.getRuntime().availableProcessors()];
        MandelbrotThread01[] threads = new MandelbrotThread01[4];

        //WYSYŁAMY KAŻDEMY WĄTKOWI OBRAZEK
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MandelbrotThread01(img, i+1, Xrange, Yrange, iter); //orderNumber = i+1, bo liczymy mod orderNumber, a nie może być mod 0 (bo to jest dzielenie przez 0)
        }
        // WŁĄCZAMY WSZYSTKIE WĄTKI
        for (int i = 0; i < threads.length; i++) {
                threads[i].start();
        }

        // CZEKAMY NA WSZYSTKIE WĄTKI
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        //Zapisywanie obrazka
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

//    //    funkcja zwaracająca obraz w BufferedImage, jak wyżej tylko równolegle poprzez tworzenie puli wątków
//    // pula wątków wykonuje jednoczęnie iteracje w funkcji mandelbrot, Obilczanie i kolorowanie odbywa się sekwencyjnie
//    public static BufferedImage generateMandelbrotImageParallelPool(int width, int height, double[] Xrange, double[] Yrange, int iter, String filename) throws ExecutionException, InterruptedException {
//        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//        if (Xrange.length != 2  && Yrange.length != 2){
//            throw new IllegalArgumentException("Range must contain 2 doubles");
//        }
//
//        //TWORZYMY PULĘ WĄTKÓW - tyle ile jest rdzeni
//        ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//        // PĘTLA PO PIKSELACH
//        for (int x = 0; x < img.getWidth(); x++) {
//            for (int y = 0; y < img.getHeight(); y++) {
//
//                //MAPOWANIE WARTOŚCI
//                double cx = (Xrange[1] - Xrange[0]) * x/img.getWidth() + Xrange[0];
//                double cy = (Yrange[1] - Yrange[0]) * y/img.getHeight() + Yrange[0];
//                Complex c = new Complex(cx,cy);
//
//                //MANDELBROT ITERATION - THREAD POOL
//                //float out = mandelbrot(c, iter, false);
//                float out = ex.submit(()->mandelbrot(c, iter, false)).get();
//
//                //SETTING COLOR
//                // kolor jest ustalany na okręgu HUE w przestrzeni barw HSV/HSB
//                // za https://www.codingame.com/playgrounds/2358/how-to-plot-the-mandelbrot-set/adding-some-colors
//                float hue = out/iter; //*4 ;// funkcja hsbToRGB bierze mantysę i mnoży przez 360 //mnożąc hue przez 2,3,4... można dostać ciekawy efekt
//                float saturation = 1.0F; // float 0-1
//                float brightness = 1.0F; //float 0-1
//                if (out == iter){brightness = 0.0F;} //dla zbiegających = dochodzi do max iteracji ustaw kolor czarny
//
//                img.setRGB(x,y, java.awt.Color.HSBtoRGB(hue,saturation,brightness));
//
//            }
//
//        }
//        //NISZCZENIE PULI WĄTKÓW
//        ex.shutdown();
//        ex.awaitTermination(1, TimeUnit.DAYS);
//
//        if (filename != null){
//            File outputfile = new File(filename+".jpg");
//            try {
//                ImageIO.write(img, "jpg", outputfile);
//                System.out.println("Image was saved");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        return img;
//    }
//
//
//    //    funkcja zwaracająca obraz w BufferedImage, jak wyżej tylko równolegle poprzez tworzenie puli wątków
//    // druga wersja - więcej operacji dzieje się sekwencyjnie
//    public static BufferedImage generateMandelbrotImageParallelPool2(int width, int height, double[] Xrange, double[] Yrange, int iter, String filename) throws  InterruptedException {
//        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//        if (Xrange.length != 2  && Yrange.length != 2){
//            throw new IllegalArgumentException("Range must contain 2 doubles");
//        }
//
//        //TWORZYMY PULĘ WĄTKÓW - tyle ile jest rdzeni
//        ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//        // PĘTLA PO PIKSELACH
//        for (int x = 0; x < img.getWidth(); x++) {
//            for (int y = 0; y < img.getHeight(); y++) {
//
//                //MANDELBROT ITERATION - THREAD POOL
//                int ix = x; // musi być final
//                int iy = y;
//                ex.execute(()->mandelbrot2(iter, false, Xrange, Yrange, ix, iy, img));
//            }
//
//        }
//        //NISZCZENIE PULI WĄTKÓW
//        ex.shutdown();
//        ex.awaitTermination(1, TimeUnit.DAYS);
//
//        if (filename != null){
//            File outputfile = new File(filename+".jpg");
//            try {
//                ImageIO.write(img, "jpg", outputfile);
//                System.out.println("Image was saved");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        return img;
//    }
//    //    funkcja zwaracająca obraz w BufferedImage, jak wyżej tylko równolegle poprzez tworzenie puli wątków
//    // trzecia wersja - pulę wątków podajemy spoza funkcji
//    public static BufferedImage generateMandelbrotImageParallelPool3(int width, int height, double[] Xrange, double[] Yrange, int iter, String filename, ExecutorService ex) throws  InterruptedException {
//        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//        if (Xrange.length != 2  && Yrange.length != 2){
//            throw new IllegalArgumentException("Range must contain 2 doubles");
//        }
//
//        // PĘTLA PO PIKSELACH
//        for (int x = 0; x < img.getWidth(); x++) {
//            for (int y = 0; y < img.getHeight(); y++) {
//
//                //MANDELBROT ITERATION - THREAD POOL
//                int ix = x; // musi być final
//                int iy = y;
//                ex.execute(()->mandelbrot2(iter, false, Xrange, Yrange, ix, iy, img));
//            }
//
//        }
//        if (filename != null){
//            File outputfile = new File(filename+".jpg");
//            try {
//                ImageIO.write(img, "jpg", outputfile);
//                System.out.println("Image was saved");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        return img;
//    }

    //  funkcja zwaracająca obraz w BufferedImage, jak wyżej tylko równolegle poprzez tworzenie puli wątków
    // druga wersja - więcej operacji dzieje się sekwencyjnie
    // joby są podawane przez rozmiar bloku oraz numer bloku, znając rozmiar obrazka każdy wątek może sobie wyliczyć które piksele ma obliczać
    // ta funkcja jest owocem 10h (sic!) próbaowania różnych rzeczy - problemy których nie warto powatarzać:
    // 1.JVM ma ograniczoną pamięć 256MB, więc stworzenie tablicy 8192*8192*4 bajtów ją rozwalało - dlatego tutaj nie podaję konkretnie referencji do pikseli
    // 2.najłatwiej jest wysłać każdemu wątkowi referencję do obrazka i niech on sobie zmienia w nim piksele - odbieranie wartości i bawienie się z dostępem zawiodło
    // 3. Pula wątków musi dostawać jakieś bloki pixeli - wysyłanie pojedynczych pikseli trwało wieki (aż mi sięnie chciało czekać żeby sprawdzić jak długo) - zostawiam tamte rozwiązania, ale zakomentowane
    // 4. Niby z pulą wątków łatwiej, ale kod dla wątków napisałem w 15 minut - rozważ czy nie warto używać tamtego podejścia
    public static BufferedImage generateMandelbrotImageParallelPoolArray(int width, int height, double[] Xrange, double[] Yrange, int iter, String filename, int blockSize) throws  InterruptedException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        if (Xrange.length != 2  && Yrange.length != 2){
            throw new IllegalArgumentException("Range must contain 2 doubles");
        }

        //TWORZYMY PULĘ WĄTKÓW - tyle ile jest rdzeni
        ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int counter = 0;

        //TWORZENIE BLOKÓW
        int nBlocks = (int) Math.ceil(1.0*width*height/blockSize);

        for (int block = 0; block < nBlocks; block++) {
            int ii = block;
            //System.out.println("wysyłam blok "+ ii );
            ex.execute(()->mandelbrotArray(iter, false, Xrange, Yrange, ii, blockSize, img.getWidth(), img.getHeight(), img));
        }

        //NISZCZENIE PULI WĄTKÓW
        ex.shutdown();
        ex.awaitTermination(1, TimeUnit.DAYS);

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

    //wersja z zewnętrzną pulą wątków na potrzeby mierzenia czasu
    public static BufferedImage generateMandelbrotImageParallelPoolArrayNoEx(int width, int height, double[] Xrange, double[] Yrange, int iter, String filename, int blockSize, ExecutorService ex) throws  InterruptedException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        if (Xrange.length != 2  && Yrange.length != 2){
            throw new IllegalArgumentException("Range must contain 2 doubles");
        }
        int counter = 0;

        //TWORZENIE BLOKÓW
        int nBlocks = (int) Math.ceil(1.0*width*height/blockSize);

        for (int block = 0; block < nBlocks; block++) {
            int ii = block;
            //System.out.println("wysyłam blok "+ ii );
            ex.execute(()->mandelbrotArray(iter, false, Xrange, Yrange, ii, blockSize, img.getWidth(), img.getHeight(), img));
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
    // flag==0 - sekwencyjnie
    // flag==1 - wielowątkowo, pojedyncze wątki
    // flag==2 - pula wątków, wszystko robi pula wątków (zdefinuj blockSize w jobie)
    // flag==3 -  pula wątków, ale tworzymy jedną pulę i używamy jej wielokrotnie, czyli nie wliczamy do średniego czasu, czasu potrzebnego na stworzenie puli wątków
    public static double TimeMyMandelbrot(int width, int height, double[] Xrange, double[] Yrange, int iter, int iterMean, int flag, int blockSize) throws InterruptedException, ExecutionException {

        long []times = new long[iterMean];

        for (int i = 0; i < iterMean; i++) {

            long start = System.nanoTime();

            if (flag == 0){
                BufferedImage img = Mandelbrot.generateMandelbrotImage(width, height, Xrange, Yrange, iter, null);
            } else if (flag == 1) {
                BufferedImage img = Mandelbrot.generateMandelbrotImageParallelThreads(width, height, Xrange, Yrange, iter, null);
            } else if (flag == 2) {
                BufferedImage img = Mandelbrot.generateMandelbrotImageParallelPoolArray(width, height, Xrange, Yrange, iter, null, blockSize);
            }

            long finish = System.nanoTime();

            times[i] = finish - start;
        }

        //PULA WĄTKÓW bez tworzenie ex, flag==3
        if (flag==3){
            ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (int i = 0; i < iterMean; i++) {
                long start = System.nanoTime();
                BufferedImage img = Mandelbrot.generateMandelbrotImageParallelPoolArrayNoEx(width, height, Xrange, Yrange, iter, null, blockSize, ex);
                long finish = System.nanoTime();

                times[i] = finish - start;
            }

            ex.shutdown();
            ex.awaitTermination(1, TimeUnit.DAYS);
        }

        long sum = 0;
        for (long time:times) {
            sum+=time;
        }

        return 1.0d*sum/times.length;
    }

}
