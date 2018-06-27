public class WriteOnlyFields {
    public               int     anInt;

    void aMethod() {
        int i = anInt;
    }

    public static void main(String[] args) {
        WriteOnlyFields w = new WriteOnlyFields();

        boolean b = false;

        if (b) {
            System.out.println("yep, flag");
            w.anInt = 10;
        } else {
            w.anInt = 99;
        }
    }
}
