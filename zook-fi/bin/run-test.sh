#!/bin/sh

ZKHOME=/home/suda/WORK/PCheck/zook-fi
CPATH=${ZKHOME}/build/classes:${ZKHOME}/lib/log4j-1.2.15.jar

java -cp ${CPATH} election.Election 
