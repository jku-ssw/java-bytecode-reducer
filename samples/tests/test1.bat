java TestClazz1 >java_out.txt 2>&1 &&^
findstr /C:"#############   GLOBAL HASH: 2  #############" java_out.txt >nul 2>&1
