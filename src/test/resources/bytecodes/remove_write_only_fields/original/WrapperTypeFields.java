public class WrapperTypeFields {
    Integer anInteger = 0;
    Double  a;
    Boolean writingOnly;

    Float f;

    public WrapperTypeFields() {
        this.f = 0.789343489023902f;
    }

    private void writing() {
        this.writingOnly = false;
    }

    public static void main(String[] args) {
        WrapperTypeFields w = new WrapperTypeFields();

        System.out.println(w.anInteger);
        System.out.println(w.a);
        w.writingOnly = true;
    }
}
