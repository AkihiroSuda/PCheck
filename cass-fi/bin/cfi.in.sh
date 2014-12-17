#!/bin/sh



CASSANDRA_INCLUDE=/home/suda/WORK/PCheck/cass-fi/bin/cfi.in.sh
export CASSANDRA_INCLUDE
cd /home/suda/WORK/PCheck/cass-fi


. $CASSANDRA_INCLUDE

echo ""
echo "$JVM_OPTS"
echo ""


JAVA=`which java`
exec $JAVA $JVM_OPTS -cp $CLASSPATH org.fi.FMDriver > /tmp/fi/logs/fi.out  & 

