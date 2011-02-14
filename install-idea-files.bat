rem complete your IDEA Home folder here
set IDEA_HOME=%HOMEDRIVE%%HOMEPATH%\Progs\JetBrains\IntelliJ IDEA 10.0.2
set IDEA_VERSION=103.72
set MKS_VERSION=8.3.2392
set IDEA_GROUP_ID=com.intellij

call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\openapi.jar" -DartifactId=openapi -Dversion=%IDEA_VERSION%
call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\annotations.jar" -DartifactId=annotations -Dversion=%IDEA_VERSION%
call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\extensions.jar" -DartifactId=extensions -Dversion=%IDEA_VERSION%
call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\util.jar" -DartifactId=util -Dversion=%IDEA_VERSION%
call mvn install:install-file -DgroupId=com.mks -Dpackaging=jar -Dfile="lib\%MKS_VERSION%\mkscmapi.jar" -DartifactId=mkscmapi -Dversion=%MKS_VERSION%
call mvn install:install-file -DgroupId=com.mks -Dpackaging=jar -Dfile="lib\%MKS_VERSION%\mksapi.jar" -DartifactId=mksapi -Dversion=%MKS_VERSION%
