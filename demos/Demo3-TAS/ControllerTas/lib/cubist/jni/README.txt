What you need
1. gcc
2. jni.h headers in the folder of the JDK
3. A compiled and executable version of Cubist, to create the model. Use the make all to have one
4. The source file of the module that allows to use the built model. It is available here and on the Cubist webpage

1. Put the native declaration of the method in the class you want to use them
2. Compile the class (even through an IDE)
3. Go in the root path of the binary of your project
4. Execute javah full.package.class
5. In the cubistJNI.c, update the header to be included. Also, update the name of the methods!!
6. Create the lib with something like gcc -fPIC  -I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -shared -O3 -o libcubistJNI.dylib cubistJNI.c -lm -lpthread
7. Put the lib in a folder. This folder has to be passed as parameter -Djava.library.path="full_path_to_lib"
8. Before using the stuff, remember to so a System.loadLibrary with the name you gave to the library
9. When invoking, be sure to pass a valid reference to the *.names