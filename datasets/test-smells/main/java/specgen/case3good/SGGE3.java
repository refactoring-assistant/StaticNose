package specgen.case3good;

interface GeometricObject {
    public double calculateArea();

    public void calculatePerimeter();
}

class Circle implements GeometricObject  {
    double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * Math.pow(this.radius, 2);
    }

    @Override
    public void calculatePerimeter() {
        System.out.println("Circumference: " + 2 * Math.PI * this.radius);
    }

}
