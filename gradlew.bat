@echo off
set APP_HOME=%~dp0
if defined JAVA_HOME (set JAVACMD=%JAVA_HOME%\bin\java.exe) else (set JAVACMD=java.exe)
"%JAVACMD%" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper-main.jar;%APP_HOME%gradle\wrapper\gradle-wrapper-shared.jar;%APP_HOME%gradle\wrapper\gradle-cli.jar;%APP_HOME%gradle\wrapper\gradle-files.jar" org.gradle.wrapper.GradleWrapperMain %*
