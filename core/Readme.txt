Welcome to Seco
=================

Seco is a free, open-source scripting environment for the Java platform. 

For more information, please visit http://www.kobrix.com/seco.jsp.

Licensing information may be found in the LicensingInformation file in this
directory. Note that the embedded BerkeleyDB is governed by a different license
- see BERKELEYDB_LICENSE.txt.

Requirements
============

Seco needs Java 6 to run. It has been tested on Windows and Linux. If you are using 
on some other platform, please let us know.


Running on Windows
==================

Edit the run.cmd file to set the JAVA_HOME environment variable. It may point to a JDK installation 
or to a JRE installation. Then run the file from anywhere in your computer.

Running on Linux
================

Edit the run.sh file to set the JAVA_HOME environment variable. It may point to a JDK installation 
or to a JRE installation. Then run the file from anywhere in your computer. If it's not recognized
as a program, you may need to set its execute permissions like this:

chmod +x run.sh

32bit vs. 64bit platforms
=========================

The run.sh on Linux will try and detect whether 64bit native libraries need to
be used. If you are running Windows AMD64, please modify the run.cmd to point to
the 64bit native libraries - they can be found under lib/native/windows/amd64.

HAVE FUN!