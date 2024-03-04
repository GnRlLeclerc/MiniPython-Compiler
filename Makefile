
all: java release

java:
	mkdir -p bin
	javac -cp lib/java-cup-11a-runtime.jar -d bin src/mini_python/**/*.java src/mini_python/*.java
	java -cp lib/java-cup-11a-runtime.jar:bin mini_python.Main --debug test.py

# Compile our "libc extension" and create a library file
libc:
	mkdir -p bin/libc_extended
	gcc -O3 -c src/libc_extended/libc_extended.c -o bin/libc_extended/libc_extended.o
	ar rcs bin/libc_extended/liblibc_extended.a bin/libc_extended/libc_extended.o

# Compile our output assembly and link it together with our "libc extension"
compile:
	gcc -O3 -c test.s -o bin/test.o
	gcc -O3 -no-pie -o test bin/test.o -L./bin/libc_extended -llibc_extended

release: libc compile

# Compile our output assembly and link it together with our "libc extension" with no optimizations and a debug flag
debug:
	# Compile the libc in debug mode
	mkdir -p bin/libc_extended
	gcc -g -c src/libc_extended/libc_extended.c -o bin/libc_extended/libc_extended.o
	ar rcs bin/libc_extended/liblibc_extended.a bin/libc_extended/libc_extended.o

	# Compile our code in debug mode
	gcc -g -c test.s -o bin/test.o
	gcc -g -no-pie -o test bin/test.o -L./bin/libc_extended -llibc_extended
