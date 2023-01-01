public class Complex {

    //KLASA Z LICZBAMI ZESPOLONYMI

    private double real;
    private double imaginary;

    public Complex(){
        this(0,0);
    }

    public Complex(double real, double imaginary){
        this.real = real;
        this.imaginary = imaginary;
    }

    public void set(Complex z){
        this.real = z.real;
        this.imaginary = z.imaginary;
    }

    public double getReal() {
        return real;
    }

    public double getImaginary() {
        return imaginary;
    }

    public static Complex Cmultiply(Complex c1, Complex c2){

        Complex c = new Complex((c1.real*c2.real - c1.imaginary* c2.imaginary), (c1.imaginary*c2.real + c1.real*c2.imaginary));
        return c;
    }

    public static Complex Cadd(Complex c1, Complex c2){
        Complex c = new Complex(c1.real+c2.real, c1.imaginary+c2.imaginary);
        return c;
    }

    public double Cmodule(){
        return Math.sqrt(Math.pow(this.real,2) + Math.pow(this.imaginary,2));
    }

    public void Cprint(){
        System.out.println(this.real+" + "+this.imaginary+"i");
    }
}
