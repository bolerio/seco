@echo off
set JAVA_HOME=c:/java6
set SCRIBA_HOME=%CD%

set SCRIBA_CLASSPATH="%SCRIBA_HOME%/seco.jar"
set SCRIBA_NATIVE=%SCRIBA_HOME%/lib/native/windows
set JAVA_EXEC="%JAVA_HOME%/bin/java"

set PATH=%SCRIBA_NATIVE%;%PATH%

set LIB_JARS=
echo set LIB_JARS=%%~1;%%LIB_JARS%%>append.bat
dir /s/b lib\*.jar > tmpList.txt
FOR /F "usebackq tokens=1* delims=" %%i IN (tmpList.txt) do (call append.bat "%%i")
del append.bat
del tmpList.txt
set SCRIBA_CLASSPATH=%LIB_JARS%;%SCRIBA_CLASSPATH%

%JAVA_EXEC% -cp %SCRIBA_CLASSPATH% -Djava.library.path=%SCRIBA_NATIVE% seco.boot.StartMeUp