public class Array {
    public static void main(String[] args) {
        string();
        integer();
    }

    private static void string() {
        String[] arr = new String[]{ "hello" };
        System.out.println(arr.length);

        arr = new String[]{ "world", "jjvm" };
        System.out.println(arr.length);
        System.out.println(arr[1]);
    }

    private static void integer() {
        int[] arr = new int[]{ 1, 2, 3 };
        System.out.println(arr.length);

        arr = new int[]{ 1, 2 };
        System.out.println(arr.length);
        System.out.println(arr[1]);
    }
}
