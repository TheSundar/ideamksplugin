before building, it is necessary to run the following commands

mvn install:install-file -DgroupId=com.mks -Dpackaging=jar -Dfile=lib\8.3.2392\mkscmapi.jar -DartifactId=mkscmapi -Dversion=8.3.2392

rem complete your IDEA Home folder here
set IDEA_HOME=H:\developpement\IntelliJ IDEA 7.0M1a

rem the following lines are for IDEA 7.0M1a (which is build 6827)
mvn install:install-file -DgroupId=com.intellij.idea -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\openapi.jar" -DartifactId=openapi -Dversion=7.0.6827
mvn install:install-file -DgroupId=com.intellij.idea -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\annotations.jar" -DartifactId=annotations -Dversion=7.0.6827
mvn install:install-file -DgroupId=com.intellij.idea -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\extensions.jar" -DartifactId=extensions -Dversion=7.0.6827
