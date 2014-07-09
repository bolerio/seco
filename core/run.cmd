@echo off
set JAVA_HOME=C:\java7
set SECO_HOME=%CD%

::Uncomment this and change the path if you have profile name  with accent or if you wish to specify a different location for Seco repository
::set SECO_CLASS_REPOSITORY_HOME = C:/temp/.secoRepository

set SECO_CLASSPATH="%SECO_HOME%/seco.jar"
set JAVA_EXEC="%JAVA_HOME%/bin/java"

set LIB_JARS=
echo set LIB_JARS=%%~1;%%LIB_JARS%%>append.bat
dir /s/b lib\*.jar > tmpList.txt
FOR /F "usebackq tokens=1* delims=" %%i IN (tmpList.txt) do (call append.bat "%%i")
del append.bat
del tmpList.txt
set SECO_CLASSPATH=%LIB_JARS%;%SECO_CLASSPATH%

%JAVA_EXEC% -cp %SECO_CLASSPATH% seco.boot.StartMeUp