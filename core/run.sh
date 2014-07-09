#!/bin/sh
JAVA_HOME=c:/java6
# OS specific support.  $var _must_ be set to either true or false.
pathsep=':'
systemname='windows'
systemarch=''
cygwin=false
case "`uname`" in
CYGWIN*) 
  cygwin=true
  systemname='windows'
  ;;
*) 
  systemname='linux'
  ;;
esac

if [ `uname -m` = 'x86_64' ]; then
  systemarch='/x86_64'
fi

SECO_BIN_DIR=`dirname $0`
cd $SECO_BIN_DIR
export SECO_HOME="`pwd`"
echo "Using Seco home directory '$SECO_HOME'"

JAVA_EXEC=$JAVA_HOME/bin/java

SECO_CLASSPATH="$SECO_HOME/seco.jar"

for f in lib/*.jar; do
  SECO_CLASSPATH="$SECO_CLASSPATH$pathsep$f"
done;

if $cygwin; then
  [ -n "$SECO_HOME" ] && SECO_CLASSPATH=`cygpath --absolute --path --windows "$SECO_CLASSPATH"`
fi

exec $JAVA_EXEC -cp $SECO_CLASSPATH seco.boot.StartMeUp
