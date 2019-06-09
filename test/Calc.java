public class Calc {
    public static void main(String[] args) {
        plus();
        minus();
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
}
