public class Float {
    public static void main(String[] args) {
        float num1 = plus();
        float num2 = minus();
        float num3 = mult();
        float num4 = div();
        float num5 = rem();
        float num6 = neg();
        System.out.println(num1 + num2 + num3 + num4 + num5 + num6);
    }

    private static float plus() {
        float num1 = 1;
        float num2 = 0;
        float result = num1 + num2;
        System.out.println(result);
        return result;
    }

    private static float minus() {
        float num1 = -1;
        float num2 = 2;
        float result = num1 - num2;
        System.out.println(result);
        return result;
    }

    private static float mult() {
        float num1 = 4;
        float num2 = 5;
        float result = num1 * num2;
        System.out.println(result);
        return result;
    }

    private static float div() {
        float num1 = 10;
        float num2 = 5;
        float result = num1 / num2;
        System.out.println(result);
        return result;
    }

    private static float rem() {
        float num1 = 10;
        float num2 = 3;
        float result = num1 % num2;
        System.out.println(result);
        return result;
    }

    private static float neg() {
        float num1 = 3;
        float result = -num1;
        System.out.println(result);
        return result;
    }
}
