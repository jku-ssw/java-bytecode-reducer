class A {


    int a;
    static int b;
    final int c = 0;
    private int d;
    protected int e;
    public int f;
    transient int g;
    volatile int h;

    A() {
    }

    void h() {
    }

    static void j() {
    }

    private void k() {
    }

    protected void l() {
    }

    public void m() {
    }

    final void n() {
        System.out.println(h);
    }

    synchronized void o() {
    }

    public static void main(String[] var0) {
        System.out.println("b=" + A.b);
        System.out.println("42");
    }
}
