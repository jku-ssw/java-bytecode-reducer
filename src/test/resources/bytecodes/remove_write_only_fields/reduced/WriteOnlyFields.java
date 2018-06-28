public class WriteOnlyFields {
    private volatile boolean someFlag;

    void aMethod(int param) {
    }

    public static void main(String[] args) {
        WriteOnlyFields w = new WriteOnlyFields();

        if (w.someFlag) {
            System.out.println("yep, flag");
        } else {
        }
    }
}
