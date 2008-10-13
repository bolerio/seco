#!/bin/sh

# OS specific support.  $var _must_ be set to either true or false.
pathsep=':'
systemname='windows'
cygwin=false
os400=false
case "`uname`" in
CYGWIN*) 
  cygwin=true
  systemname='windows'
  ;;
*) 
  systemname='linux'
  ;;
esac


SCRIBA_BIN_DIR=`dirname $0`
cd $SCRIBA_BIN_DIR
export SCRIBA_HOME="`pwd`"
echo "Using Scriba home directory '$SCRIBA_HOME'"

JAVA_EXEC="$JAVA_HOME/bin/java"

SCRIBA_CLASSPATH="$SCRIBA_HOME/scriba.jar"

for f in lib/*.jar; do
  SCRIBA_CLASSPATH="$SCRIBA_CLASSPATH$pathsep$f"
done;

SCRIBA_NATIVE=$SCRIBA_HOME/lib/native/$systemname

if $cygwin; then
  [ -n "$SCRIBA_HOME" ] && SCRIBA_CLASSPATH=`cygpath --absolute --path --windows "$SCRIBA_CLASSPATH"`
  [ -n "$SCRIBA_NATIVE" ] && SCRIBA_NATIVE=`cygpath --path --windows "$SCRIBA_NATIVE"`
fi

PATH=$SCRIBA_NATIVE:$PATH

exec $JAVA_EXEC -cp $SCRIBA_CLASSPATH -Djava.library.path=$SCRIBA_NATIVE com.kobrix.scriba.boot.StartMeUp
