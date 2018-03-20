java A >java_out.txt 2>&1 &&^
findstr /C:"42" java_out.txt >nul 2>&1
