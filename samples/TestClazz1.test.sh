#!/bin/sh

ulimit -t 2 # limit execution time to 2 seconds

java TestClazz1 > java_out.txt 2>&1 &&\
grep '#############   GLOBAL HASH: 2  #############' java_out.txt > /dev/null 2>&1
