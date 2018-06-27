public class WriteOnlyFields {
    public           int     anInt;
    private volatile boolean someFlag;

    void aMethod() {
        int i = anInt;
    }

    public static void main(String[] args) {
        WriteOnlyFields w = new WriteOnlyFields();

        if (w.someFlag) {
            System.out.println("yep, flag");
            w.anInt = 10;
        } else {
            w.anInt = 99;
        }
    }
}
