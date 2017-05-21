#!/bin/sh
JAVA_EXEC=java
JAVA_FIX=''
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
  JAVA_FIX='-Dawt.useSystemAAFontSettings=lcd'
  ;;
esac

if [ `uname -m` = 'x86_64' ]; then
  systemarch='/x86_64'
fi

SECO_BIN_DIR=`dirname $0`
cd $SECO_BIN_DIR
export SECO_HOME="`pwd`"
echo "Using Seco home directory '$SECO_HOME'"


SECO_CLASSPATH="$SECO_HOME/seco.jar"

for f in lib/*.jar; do
  SECO_CLASSPATH="$SECO_CLASSPATH$pathsep$f"
done;

if $cygwin; then
  [ -n "$SECO_HOME" ] && SECO_CLASSPATH=`cygpath --absolute --path --windows "$SECO_CLASSPATH"`
fi

echo $SECO_CLASSPATH
exec $JAVA_EXEC $JAVA_FIX -cp $SECO_CLASSPATH seco.boot.StartMeUp "$@"
