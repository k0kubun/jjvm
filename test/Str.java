public class Str {
    public static void main(String[] args) {
        retNull();
        variables();
        builder();
        startsWith();
        comparison();
    }

    private static String retNull() {
        String a = null;
        return a;
    }

    private static void variables() {
        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d);
    }

    private static void builder() {
        String hello = "hello";
        String world = "world";
        System.out.println(hello + world);
    }

    private static void startsWith() {
        String str = "str";
        System.out.println(str.startsWith("s"));
    }

    private static void comparison() {
        String a = "foo";
        String b = a;
        String c = new String("foo");
        String d = "bar";
        System.out.println(a == b ? 1 : 0);
        System.out.println(a == c ? 1 : 0);
        System.out.println(a == d ? 1 : 0);
        System.out.println(a != b ? 1 : 0);
        System.out.println(a != c ? 1 : 0);
        System.out.println(a != d ? 1 : 0);
    }
}
