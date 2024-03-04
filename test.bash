#!/bin/bash

shopt -s nullglob

# script de test pour le projet de compilation

option=$1
compilo=$2
score=0
max=0
verbose=0


echo "Testing $2"

echo

# rm -f syntax/bad/testfile-*.py
# rm -f syntax/good/testfile-*.py
# rm -f typing/bad/testfile-*.py
# rm -f typing/good/testfile-*.py
# ocamllex split.mll
# ocaml split.ml syntax/bad/bad.split
# ocaml split.ml syntax/good/good.split
# ocaml split.ml typing/bad/bad.split
# ocaml split.ml typing/good/good.split

compile () {
if [[ $verbose != 0 ]]; then
  echo Compile $1 $2
  $compilo $1 $2;
else
  $compilo $1 $2 > /dev/null 2>&1;
fi;
}


# part 1 : parsing

partie1 () {

score=0
max=0

echo "Part 1 (parsing)"

echo -n "bad "
for f in  tests/syntax/bad/*.py; do
    echo -n ".";
    max=`expr $max + 1`;
    compile --parse-only $f;
    case $? in
	"0")
	echo
	echo "FAIL on "$f" (should fail)";;
	"1") score=`expr $score + 1`;;
	*)
	echo
	echo "FAIL on "$f" (for a bad reason)";;
    esac
done
echo

echo -n "good "
for f in  tests/syntax/good/*.py  tests/typing/bad/*.py  tests/typing/good/*.py  tests/exec/*.py  tests/exec-fail/*.py; do
    echo -n ".";
    max=`expr $max + 1`;
    compile --parse-only $f;
    case $? in
	"1")
	echo
	echo "FAIL on "$f" (should succeed)";;
	"0") score=`expr $score + 1`;;
	*)
	echo
	echo "FAIL on "$f" (for a bad reason)";;
    esac
done
echo

percent=`expr 100 \* $score / $max`;

echo -n "Parsing: $score/$max : $percent%"; }

# part 2 : type checking

partie2 () {
echo
echo "Part 2 (type checking)"

score=0
max=0

echo -n "bad "
for f in  tests/typing/bad/*.py; do
    echo -n ".";
    max=`expr $max + 1`;
    compile --type-only $f;
    case $? in
	"0")
	echo
	echo "FAIL on "$f" (should fail)";;
	"1") score=`expr $score + 1`;;
	*)
	echo
	echo "FAIL on "$f" (pour une mauvaise raison)";;
    esac
done
echo

echo -n "good "
for f in  tests/typing/good/*.py  tests/exec/*.py  tests/exec-fail/*.py; do
    echo -n ".";
    max=`expr $max + 1`;
    compile --type-only $f;
    case $? in
	"1")
	echo
	echo "FAIL on "$f" (should succeed)";;
	"0") score=`expr $score + 1`;;
	*)
	echo
	echo "FAIL on "$f" (for a bad reason)";;
    esac
done
echo

percent=`expr 100 \* $score / $max`;

echo    "Typing: $score/$max : $percent%";
}


# part 3: code generation

partie3 () {

score_comp=0
score_out=0
score_test=0
max=0

echo "Part 3 (code generation)"
echo "Execution with no runtime error"
echo "-------------------------------"

# timeout="why3-cpulimit 30 0 -h"

########################### ADDED ###########################
# Compile the extended libc
make libc

compile_and_link () {
    # Compile the assembly into an intermediary object file
    gcc -O3 -c $1 -o bin/test.o

    # Link the object file with the extended libc
    gcc -O3 -no-pie bin/test.o -L./bin/libc_extended -llibc_extended
}
########################### ADDED ###########################


for f in tests/exec/*.py; do
    echo -n "."
    asm=tests/exec/`basename $f .py`.s
    rm -f $asm
    expected=tests/exec/`basename $f .py`.out
    max=`expr $max + 1`;
    if compile $f; then
	rm -f out
	score_comp=`expr $score_comp + 1`;
	if compile_and_link $asm && ./a.out > out; then
	    score_out=`expr $score_out + 1`;
	    if cmp --quiet out $expected; then
		score_test=`expr $score_test + 1`;
	    else
		echo
		echo "FAIL: bad output for $f"
	    fi
	else
		echo
		echo "FAIL: generated code for $f fails"
	fi
    else
	echo
	echo "FAIL: $f does not compile"
    fi
done
echo

echo "Execution with a runtime error"
echo "------------------------------"

for f in  tests/exec-fail/*.py; do
    echo -n "."
    asm=tests/exec-fail/`basename $f .py`.s
    rm -f $asm
    max=`expr $max + 1`;
    if compile $f && compile_and_link $asm; then
	score_comp=`expr $score_comp + 1`;
	if { ./a.out > out; } >& /dev/null; then
	    echo
	    echo "FAIL: code for $f should fail at runtime"
	else
	    score_test=`expr $score_test + 1`;
	    score_out=`expr $score_out + 1`;
	fi
    else
	echo
	echo "FAIL: $f does not compile"
    fi
done

echo
percent=`expr 100 \* $score / $max`;

echo "Compilation:";
percent=`expr 100 \* $score_comp / $max`;
echo "Compilation: $score_comp/$max : $percent%";
percent=`expr 100 \* $score_out / $max`;
echo "Code behavior: $score_out/$max : $percent%";
percent=`expr 100 \* $score_test / $max`;
echo "Expected output: $score_test/$max : $percent%";}

# partie 3i : tests d'exécution avec interprète

partie3i () {

score_out=0
score_test=0
max=0

echo
echo "Part 3i (interpreter)"

echo "Execution with no runtime error"
echo "-------------------------------"

# timeout="why3-cpulimit 30 0 -h"

for f in  tests/exec/*.py; do
    echo -n "."
    expected=exec/`basename $f .py`.out
    max=`expr $max + 1`;
    rm -f out
    if $compilo $f > out; then
	score_test=`expr $score_test + 1`;
	if cmp --quiet out $expected; then
	    score_out=`expr $score_out + 1`;
	else
	    echo
	    echo "ECHEC : mauvaise sortie pour $f"
	fi
    else
	echo
	echo "ECHEC de l'interprétation sur $f (devrait réussir)"
    fi
done
echo

echo "Execution with a runtime error"
echo "------------------------------"

for f in  tests/exec-fail/*.py; do
    echo -n "."
    max=`expr $max + 1`;
    if $compilo $f > out 2>&1; then
	echo
	echo "ECHEC : l'interprétation sur $f devrait échouer"
    else
	score_test=`expr $score_test + 1`;
	score_out=`expr $score_out + 1`;
    fi
done

echo
echo "Interpretation:";
percent=`expr 100 \* $score_test / $max`;
echo "Behavior: $score_test/$max : $percent%";
percent=`expr 100 \* $score_out / $max`;
echo "Expected output: $score_out/$max : $percent%";}

case $option in
    "-1" )
        partie1;;
    "-2" )
        partie2;;
    "-3" )
        partie3;;
    "-i" )
        partie3i;;
    "-v1" )
	verbose=1;
	partie1;;
    "-v2" )
    	verbose=1;
        partie2;;
    "-v3" )
    	verbose=1;
        partie3;;
    "-all" )
    	partie1;
    	partie2;
    	partie3;;
    * )
        echo "usage : $0 <option> <compilo>"
        echo "spécifier une option parmi : "
        echo "-1      : tester l'analyse syntaxique"
        echo "-2      : tester le typage"
        echo "-3      : tester la compilation"
        echo "-v1     : tester l'analyse syntaxique (verbeusement)"
        echo "-v2     : tester le typage (verbeusement)"
        echo "-v3     : tester la compilation (verbeusement)"
        echo "-all    : tout tester";;

esac
echo
