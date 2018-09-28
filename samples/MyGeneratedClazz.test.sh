#!/bin/sh

ulimit -t 2 # limit execution time to 2 seconds

java MyGeneratedClazz > java_out.txt 2>&1 &&\
grep '#############   GLOBAL HASH: 1211148565  #############' java_out.txt > /dev/null 2>&1
