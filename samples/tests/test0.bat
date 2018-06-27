java TestClazz0 >java_out.txt 2>&1 &&^
findstr /C:"#############   GLOBAL HASH: 60439  #############" java_out.txt >nul 2>&1
