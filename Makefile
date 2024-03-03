
all:
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