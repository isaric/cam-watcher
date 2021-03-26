\$JAVA_HOME/bin/java -jar -Djava.library.path=${project.openCvBuild}/lib pi-cam-threaded-all.jar & disown
echo \$! > camera.pid
