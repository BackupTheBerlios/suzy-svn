#!/bin/sh

ulimit -t 5

nice -n 19 java -classpath execPlugin/ -Djava.security.manager -Djava.security.policy=execPlugin/exec.policy -Xmx64M Exec
rm execPlugin/*.class