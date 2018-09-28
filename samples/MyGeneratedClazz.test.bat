java MyGeneratedClazz >java_out.txt 2>&1 &&^
findstr /C:"#############   GLOBAL HASH: 1211148565  #############" java_out.txt >nul 2>&1
