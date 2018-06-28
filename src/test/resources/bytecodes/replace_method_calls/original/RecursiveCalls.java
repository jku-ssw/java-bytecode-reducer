public class RecursiveCalls {

    private void aMethod() {
        aMethod();
    }

    private void anotherMethod() {
        aMethod();
    }

    static void aThirdMethod() {
    }

    synchronized int aFourthMethod(int x) {
        if (x > 0)
            return aFourthMethod(x - 1);
        if (x < 0)
            return aFourthMethod(x + 1);
        return 42;
    }

    public static void main(String[] args) {
        aThirdMethod();
    }
}
