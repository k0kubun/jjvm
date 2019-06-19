public class Str {
    public static void main(String[] args) {
        retNull();
        variables();
        // TODO: test String concatenation by StringBuilder
        startsWith();
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

    private static void startsWith() {
        String str = "str";
        System.out.println(str.startsWith("s"));
    }
}
