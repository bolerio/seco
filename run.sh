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

JAVA_EXEC=java

#SCRIBA_CLASSPATH="/usr/local/classpath/share/classpath:$SCRIBA_HOME/bin"
SCRIBA_CLASSPATH="$SCRIBA_HOME/bin"

for f in lib/*.jar; do
  SCRIBA_CLASSPATH="$SCRIBA_CLASSPATH$pathsep$f"
done;

SCRIBA_NATIVE=$SCRIBA_HOME/lib/native/$systemname

if $cygwin; then
  [ -n "$SCRIBA_HOME" ] && SCRIBA_CLASSPATH=`cygpath --absolute --path --windows "$SCRIBA_CLASSPATH"`
  [ -n "$SCRIBA_NATIVE" ] && SCRIBA_NATIVE=`cygpath --path --windows "$SCRIBA_NATIVE"`
fi

#export LD_LIBRARY_PATH=/usr/local/classpath/lib/classpath:/usr/local/BerkeleyDB.4.7/lib:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH=/usr/local/BerkeleyDB.4.7/lib:$LD_LIBRARY_PATH
#NATIVE=/usr/local/classpath/lib/classpath:/usr/local/BerkeleyDB.4.7/lib

exec $JAVA_EXEC -cp $SCRIBA_CLASSPATH  seco.boot.StartMeUp
