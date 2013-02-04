CP=.:lib/*:conf/*:ControllerTas.jar
DD=-Djava.library.path="lib/cubist/jni"
EXEC=controllerTas.Main.Main
#EXEC=controllerTas.test.MaxThTest
java ${DD} -cp ${CP} ${EXEC}
