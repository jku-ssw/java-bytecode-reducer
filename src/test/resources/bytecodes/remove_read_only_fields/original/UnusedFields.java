public class UnusedFields {
    byte aByte;
    volatile  byte aVolatileByte;
    public    byte aPublicByte;
    static    byte aStaticByte;
    final     byte aFinalByte;
    private   byte aPrivateByte;
    protected byte aProtectedByte;
    transient byte aTransientByte;

    short aShort;
    volatile  short aVolatileShort;
    public    short aPublicShort;
    static    short aStaticShort;
    final     short aFinalShort;
    private   short aPrivateShort;
    protected short aProtectedShort;
    transient short aTransientShort;

    int anInt;
    volatile  int aVolatileInt;
    public    int aPublicInt;
    static    int aStaticInt;
    final     int aFinalInt;
    private   int aPrivateInt;
    protected int aProtectedInt;
    transient int aTransientInt;

    long aLong;
    volatile  long aVolatileLong;
    public    long aPublicLong;
    static    long aStaticLong;
    final     long aFinalLong;
    private   long aPrivateLong;
    protected long aProtectedLong;
    transient long aTransientLong;

    float aFloat;
    volatile  float aVolatileFloat;
    public    float aPublicFloat;
    static    float aStaticFloat;
    final     float aFinalFloat;
    private   float aPrivateFloat;
    protected float aProtectedFloat;
    transient float aTransientFloat;

    double aDouble;
    volatile  double aVolatileDouble;
    public    double aPublicDouble;
    static    double aStaticDouble;
    final     double aFinalDouble;
    private   double aPrivateDouble;
    protected double aProtectedDouble;
    transient double aTransientDouble;

    char aChar;
    volatile  char aVolatileChar;
    public    char aPublicChar;
    static    char aStaticChar;
    final     char aFinalChar;
    private   char aPrivateChar;
    protected char aProtectedChar;
    transient char aTransientChar;

    boolean aBoolean;
    volatile  boolean aVolatileBoolean;
    public    boolean aPublicBoolean;
    static    boolean aStaticBoolean;
    final     boolean aFinalBoolean;
    private   boolean aPrivateBoolean;
    protected boolean aProtectedBoolean;
    transient boolean aTransientBoolean;

    String aString;
    volatile  String aVolatileString;
    public    String aPublicString;
    static    String aStaticString;
    final     String aFinalString;
    private   String aPrivateString;
    protected String aProtectedString;
    transient String aTransientString;

    public UnusedFields() {
        this.aFinalByte = (byte) 0;
        this.aFinalShort = (short) 0;
        this.aFinalInt = 0;
        this.aFinalLong = 0L;
        this.aFinalFloat = 0.0F;
        this.aFinalDouble = 0.0;
        this.aFinalChar = '\0';
        this.aFinalBoolean = false;
        this.aFinalString = null;
    }

    public static void main(String[] args) {
    }
}
