@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
set MAVEN_HOME=C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.12
set PATH=%MAVEN_HOME%\bin;%PATH%

echo ====================================
echo Java Version:
java -version
echo ====================================
echo Maven Version:
mvn --version
echo ====================================
echo Building SwiftCast Spring Native...
mvn clean package -DskipTests
