public class Array {
    public static void main(String[] args) {
        string();
    }

    private static void string() {
        String[] arr = new String[]{ "hello" };
        System.out.println(arr.length);

        arr = new String[]{ "world", "jjvm" };
        System.out.println(arr.length);
        System.out.println(arr[1]);
    }
}
