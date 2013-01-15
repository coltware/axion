#!/bin/sh

JAVA_BIN=`which java`
MAIN_CLASS="com.coltware.axion.main.CLIServer"

BRANE_HOME=$(cd $(dirname $0);cd ..;pwd)

CLZPATH=$BRANE_HOME/classes

for f in `ls -1 libs/*.jar;ls -1 ext/*/*.jar`
do
	JAR_PATH=$BRANE_HOME/$f
	CLZPATH=$CLZPATH:$JAR_PATH
done



$JAVA_BIN -Dcom.sun.management.jmxremote -classpath $CLZPATH $MAIN_CLASS &

