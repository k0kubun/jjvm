public class Double {
    public static void main(String[] args) {
        double num1 = plus();
        double num2 = minus();
        double num3 = mult();
        double num4 = div();
        double num5 = rem();
        double num6 = neg();
        System.out.println(num1 + num2 + num3 + num4 + num5 + num6);
    }

    private static double plus() {
        double num1 = 1;
        double num2 = 0;
        double result = num1 + num2;
        System.out.println(result);
        return result;
    }

    private static double minus() {
        double num1 = -1;
        double num2 = 2;
        double result = num1 - num2;
        System.out.println(result);
        return result;
    }

    private static double mult() {
        double num1 = 4;
        double num2 = 5;
        double result = num1 * num2;
        System.out.println(result);
        return result;
    }

    private static double div() {
        double num1 = 10;
        double num2 = 5;
        double result = num1 / num2;
        System.out.println(result);
        return result;
    }

    private static double rem() {
        double num1 = 10;
        double num2 = 3;
        double result = num1 % num2;
        System.out.println(result);
        return result;
    }

    private static double neg() {
        double num1 = 3;
        double result = -num1;
        System.out.println(result);
        return result;
    }
}
