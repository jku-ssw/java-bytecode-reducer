public class WrapperTypeFields {
    Integer anInteger = 0;
    Double  a;

    public WrapperTypeFields() {
    }

    private void writing() {

    }

    public static void main(String[] args) {
        WrapperTypeFields w = new WrapperTypeFields();

        System.out.println(w.anInteger);
        System.out.println(w.a);
    }
}
