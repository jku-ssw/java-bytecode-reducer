public class RecursiveCalls {

    private void aMethod() {
        aMethod();
    }

    static void aThirdMethod() {
    }

    public static void main(String[] args) {
        aThirdMethod();
    }
}
