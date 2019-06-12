public class Int {
    public static void main(String[] args) {
        int num1 = plus();
        int num2 = minus();
        int num3 = mult();
        int num4 = div();
        int num5 = rem();
        int num6 = neg();
        int num7 = shift();
        System.out.println(num1 + num2 + num3 + num4 + num5 + num6 + num7);
    }

    private static int plus() {
        int num1 = 1;
        int num2 = 2;
        int result = num1 + num2;
        System.out.println(result);
        return result;
    }

    private static int minus() {
        int num1 = -1;
        int num2 = 3;
        int num3 = 0;
        int result = num1 - num2 - num3;
        System.out.println(result);
        return result;
    }

    private static int mult() {
        int num1 = 4;
        int num2 = 5;
        int result = num1 * num2;
        System.out.println(result);
        return result;
    }

    private static int div() {
        int num1 = 10;
        int num2 = 5;
        int result = num1 / num2;
        System.out.println(result);
        return result;
    }

    private static int rem() {
        int num1 = 10;
        int num2 = 3;
        int result = num1 % num2;
        System.out.println(result);
        return result;
    }

    private static int neg() {
        int num1 = 3;
        int result = -num1;
        System.out.println(result);
        return result;
    }

    private static int shift() {
        int num1 = 1;
        int num2 = 4;
        int num3 = -1;
        int result = num1 << 1 + num2 >> 1 + num3 >>> 2;
        System.out.println(result);
        return result;
    }
}
