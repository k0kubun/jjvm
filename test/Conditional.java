public class Conditional {
    public static void main(String[] args) {
        intif();
    }

    private static void intif() {
        int a;
        int b = 2;

        if (b < 2) {
            a = 1;
        } else {
            a = 2;
        }
        System.out.println(a);

        if (b <= 2) {
            a = 3;
        } else {
            a = 4;
        }
        System.out.println(a);

        if (b >= 2) {
            a = 5;
        } else {
            a = 6;
        }
        System.out.println(a);

        if (b > 2) {
            a = 7;
        } else {
            a = 8;
        }
        System.out.println(a);

        if (b == 2) {
            a = 9;
        } else {
            a = 10;
        }
        System.out.println(a);

        if (b != 2) {
            a = 11;
        } else {
            a = 12;
        }
        System.out.println(a);
    }
}
