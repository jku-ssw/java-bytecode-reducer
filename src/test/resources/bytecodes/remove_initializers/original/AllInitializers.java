public class AllInitializers {

    static int a;

    protected char x;

    private String str;

    static {
        a = 10;
    }

    {
        x = '\0';
    }

    public AllInitializers(String str) {
        this.str = str;
    }

    AllInitializers() {
        this("asdf");
    }

    public static void main(String[] args) {
    }
}
