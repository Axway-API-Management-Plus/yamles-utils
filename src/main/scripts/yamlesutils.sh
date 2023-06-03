#!/bin/sh
CMD_HOME=`dirname $0`

DIR_LIB=`realpath $CMD_HOME/../lib`
DIR_PLUGINS=`realpath $CMD_HOME/../plugins`

CLASSMAIN="com.axway.yamles.utils.YamlEsUtils"
CLASSPATH="$DIR_LIB/*:$DIR_PLUGINS/*"

export CLASSPATH

java ${JVM_OPTS:-} "$CLASSMAIN" $*
