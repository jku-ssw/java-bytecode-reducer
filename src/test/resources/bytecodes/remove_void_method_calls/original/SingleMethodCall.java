public class SingleMethodCall {

    static int anInt() {
        return 0;
    }

    static void aVoid() {
    }

    public static void main(String[] args) {
        int a = anInt();
        aVoid();
    }
}
