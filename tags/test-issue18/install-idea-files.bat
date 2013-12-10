rem complete your IDEA Home folder here
set IDEA_HOME=C:\Documents and Settings\A6253567.HBEU\Progs\JetBrains\IntelliJ IDEA 7.0M2
set IDEA_VERSION=7.0.7123
set MKS_VERSION=8.3.2392

call mvn install:install-file -DgroupId=com.intellij.idea -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\openapi.jar" -DartifactId=openapi -Dversion=%IDEA_VERSION%
call mvn install:install-file -DgroupId=com.intellij.idea -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\annotations.jar" -DartifactId=annotations -Dversion=%IDEA_VERSION%
call mvn install:install-file -DgroupId=com.intellij.idea -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\extensions.jar" -DartifactId=extensions -Dversion=%IDEA_VERSION%
call mvn install:install-file -DgroupId=com.mks -Dpackaging=jar -Dfile="lib\%MKS_VERSION%\mkscmapi.jar" -DartifactId=mkscmapi -Dversion=%MKS_VERSION%
