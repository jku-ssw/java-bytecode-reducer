java A >java_out.txt 2>&1 &&^
findstr /R /F java_out.txt '42' >/dev/null 2>&1 &&\
