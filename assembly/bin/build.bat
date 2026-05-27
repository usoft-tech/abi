@echo off
setlocal enabledelayedexpansion
chcp 65001

set "sbinDir=%~dp0"
call %sbinDir%/common.bat %*

set "service=%~1"

cd %projectDir%
if "%service%"=="" (
    set service=%standalone_service%
)

call mvn help:evaluate -Dexpression=project.version > temp.txt
for /f "delims=" %%i in (temp.txt) do (
    set line=%%i
    if not "!line:~0,1!"=="[" (
        set MVN_VERSION=!line!
    )
)
del temp.txt
cd %baseDir%


if "%service%"=="webapp" (
   call :buildWebapp
   tar xvf webapp.tar.gz
   move /y dist webapp
   move /y webapp %projectDir%\usoft-launcher\target\classes
   goto :EOF
) else (
   call :buildJavaService
   call :buildWebapp
   call :packageRelease
   goto :EOF
)


:buildJavaService
   set "model_name=%service%"
   echo "starting building usoft-bi service"
   call mvn -f %projectDir% clean package -DskipTests -Dspotless.skip=true
   IF ERRORLEVEL 1 (
      ECHO Failed to build backend Java modules.
      ECHO Please check Maven and Java versions are compatible.
      ECHO Current Java: %JAVA_HOME%
      ECHO Current Maven: %MAVEN_HOME%
      EXIT /B 1
   )
   
   REM extract and copy files to deployment directory
   cd %projectDir%\usoft-launcher\target
   if exist "usoft-launcher-%MVN_VERSION%-SNAPSHOT-bin.tar.gz" (
       echo "Extracting usoft-launcher-%MVN_VERSION%-SNAPSHOT-bin.tar.gz..."
       tar -xf "usoft-launcher-%MVN_VERSION%-SNAPSHOT-bin.tar.gz"
       if exist "usoft-launcher-%MVN_VERSION%" (
           echo "Copying files to deployment directory..."
           xcopy /E /Y "usoft-launcher-%MVN_VERSION%\*" "%buildDir%\usoft-bi-%MVN_VERSION%\"
       )
   )
   
   copy /y %projectDir%\usoft-launcher\target\*.tar.gz %buildDir%\
   echo "finished building usoft-bi service"
   cd %baseDir%
   goto :EOF


:buildWebapp
   echo "starting building usoft-bi webapp"
   cd %projectDir%\webapp
   call start-bi-build.bat
   copy /y webapp.tar.gz %buildDir%\
   rem check build result
   IF ERRORLEVEL 1 (
        ECHO Failed to build frontend webapp.
        EXIT /B 1
   )
   echo "finished building usoft-bi webapp"
   goto :EOF


:packageRelease
   set "model_name=%service%"
   set "release_dir=usoft-bi-%MVN_VERSION%"
   set "service_name=usoft-launcher-%MVN_VERSION%"
   echo "starting packaging usoft-bi release"
   cd %buildDir%
   if exist %release_dir% rmdir /s /q %release_dir%
   if exist %release_dir%.zip del %release_dir%.zip
   
   rem check if release directory already exists from buildJavaService
   if exist %release_dir% (
       echo "Release directory already prepared by buildJavaService"
   ) else (
       mkdir %release_dir%
       
       rem package java service
       tar xvf %service_name%-bin.tar.gz 2>nul
       if errorlevel 1 (
           echo "Warning: tar command failed, trying PowerShell extraction..."
           powershell -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%service_name%-bin.tar.gz' -DestinationPath '.' -Force"
       )
       for /d %%D in ("%service_name%\*") do (
           move "%%D" "%release_dir%"
       )
       rmdir /s /q %service_name% 2>nul
   )
   
   rem package webapp
   if exist webapp.tar.gz (
       tar xvf webapp.tar.gz 2>nul
       if errorlevel 1 (
           echo "Warning: tar command failed, trying PowerShell extraction..."
           powershell -ExecutionPolicy Bypass -Command "Expand-Archive -Path 'webapp.tar.gz' -DestinationPath '.' -Force"
       )
       move /y dist webapp
       move /y webapp %release_dir%
       del webapp.tar.gz 2>nul
   )
   
   rem verify deployment structure
   if exist "%release_dir%\lib\usoft-launcher-%MVN_VERSION%.jar" (
       echo "Deployment structure verified successfully"
   ) else (
       echo "Warning: Main jar file not found in deployment structure"
       echo "Expected: %release_dir%\lib\usoft-launcher-%MVN_VERSION%.jar"
   )
   
   rem generate zip file
   powershell -ExecutionPolicy Bypass -Command "Import-Module Microsoft.PowerShell.Archive; Compress-Archive -Path '%release_dir%' -DestinationPath '%release_dir%.zip' -Force"
   if errorlevel 1 (
       echo "Warning: PowerShell compression failed, release directory still available: %release_dir%"
   ) else (
       echo "Successfully created release package: %release_dir%.zip"
   )
   
   del %service_name%-bin.tar.gz 2>nul
   echo "finished packaging usoft-bi release"
   goto :EOF

endlocal