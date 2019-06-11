public class Long {
    public static void main(String[] args) {
        long num1 = plus();
        long num2 = minus();
        long num3 = mult();
        long num4 = div();
        long num5 = rem();
        long num6 = neg();
        System.out.println(num1 + num2 + num3 + num4 + num5 + num6);
    }

    private static long plus() {
        long num1 = 1;
        long num2 = 0;
        long result = num1 + num2;
        System.out.println(result);
        return result;
    }

    private static long minus() {
        long num1 = -1;
        long num2 = 3;
        long result = num1 - num2;
        System.out.println(result);
        return result;
    }

    private static long mult() {
        long num1 = 4;
        long num2 = 5;
        long result = num1 * num2;
        System.out.println(result);
        return result;
    }

    private static long div() {
        long num1 = 10;
        long num2 = 5;
        long result = num1 / num2;
        System.out.println(result);
        return result;
    }

    private static long rem() {
        long num1 = 10;
        long num2 = 3;
        long result = num1 % num2;
        System.out.println(result);
        return result;
    }

    private static long neg() {
        long num1 = 3;
        long result = -num1;
        System.out.println(result);
        return result;
    }
}
