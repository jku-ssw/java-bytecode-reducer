java TestClazz2 >java_out.txt 2>&1 &&^
findstr /C:"#############   GLOBAL HASH: 1690996283  #############" java_out.txt >nul 2>&1
