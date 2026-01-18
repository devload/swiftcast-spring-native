$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.12\bin;$env:PATH"

Write-Host "===================================="
Write-Host "Java Version:"
java -version
Write-Host "===================================="
Write-Host "Maven Version:"
mvn --version
Write-Host "===================================="
Write-Host "Building SwiftCast Spring Native..."
Write-Host "===================================="

cd C:\swiftcast_springNative
mvn clean package -DskipTests
