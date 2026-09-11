#!/bin/sh
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAVACMD=${JAVA_HOME:+$JAVA_HOME/bin/java}
if [ -z "$JAVACMD" ]; then JAVACMD=java; fi
exec "$JAVACMD" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper-main.jar:$APP_HOME/gradle/wrapper/gradle-wrapper-shared.jar:$APP_HOME/gradle/wrapper/gradle-cli.jar:$APP_HOME/gradle/wrapper/gradle-files.jar" org.gradle.wrapper.GradleWrapperMain "$@"
