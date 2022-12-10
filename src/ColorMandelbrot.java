import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ColorMandelbrot {

    public static void main(String[] args) {

        double[] Xarr =  {-2.1, 0.6};
        double[] Yarr =  {-1.2, 1.2};

        int size = 512;

        BufferedImage img = Mandelbrot.generateMandelbrotImage(size,size, Xarr, Yarr, 100, null);

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // wyłącz po zamknięicu okna
        frame.setVisible(true);

    }
}