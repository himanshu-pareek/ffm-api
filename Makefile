run: FFMDemo.class util/libutil.dylib
	~/dev/tools/jdk-25.0.5+8/Contents/Home/bin/java --enable-native-access=ALL-UNNAMED -Djava.library.path=util FFMDemo

FFMDemo.class: FFMDemo.java
	~/dev/tools/jdk-25.0.5+8/Contents/Home/bin/javac FFMDemo.java

util/libutil.dylib: util/util.o
	gcc -dynamiclib -o util/libutil.dylib util/util.o

util/util.o: util/util.h util/util.c
	gcc -c util/util.c -o util/util.o

shell:
	~/dev/tools/jdk-25.0.5+8/Contents/Home/bin/jshell

