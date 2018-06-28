public class RecursiveCalls {

    private void aMethod() {
        aMethod();
    }

    private void anotherMethod() {
        aMethod();
    }

    static void aThirdMethod() {
    }

    synchronized void aFourthMethod(int x) {
        if (x > 0) {
            aFourthMethod(x - 1);
            return;
        }
        if (x < 0) {
            aFourthMethod(x + 1);
            return;
        }
    }

    public static void main(String[] args) {
        aThirdMethod();
    }
}
