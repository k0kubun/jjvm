public class Int {
    public static void main(String[] args) {
        plus();
        minus();
        mult();
        div();
        rem();
        neg();
    }

    private static void plus() {
        int num1 = 1;
        int num2 = 2;
        System.out.println(num1 + num2);
    }

    private static void minus() {
        int num1 = -1;
        int num2 = 3;
        int num3 = 4;
        System.out.println(num1 - num2 - num3);
    }

    private static void mult() {
        int num1 = 5;
        int num2 = 3;
        System.out.println(num1 * num2);
    }

    private static void div() {
        int num1 = 10;
        int num2 = 5;
        System.out.println(num1 / num2);
    }

    private static void rem() {
        int num1 = 10;
        int num2 = 3;
        System.out.println(num1 % num2);
    }

    private static void neg() {
        int num1 = 3;
        System.out.println(-num1);
    }
}
