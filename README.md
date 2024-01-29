# MiniPython compiler

## Tests

Tests can be run using the [`test.bash`](./test.bash) file.
They are organized in various categories, inside the `tests/`:

    syntax/bad/    lexing or parsing must fail
    typing/bad/    type checking must fail
    typing/good/   type checking must pass
    exec-fail/     compiles successfully but fails at runtime
    exec/          compiles successfully, executes successfully,
                   and output conforms to file .out

Tests are cumulative, i.e.,

- files in `typing/bad/`, `exec-fail/`, and `exec/` can be used for the
  category `syntax/good/`

- files in `exec-fail/` and `exec/` can be used for the category
  `typing/good/`

### Testing the compiler

A script `test` is provided to run your compiler on those tests.
Use

    ./test.bash -2 "java -cp lib/java-cup-11a-runtime.jar:bin mini_python.Main"

to run the type checking tests. Your compiler is called with command
line option `--type-only` and the filename, and the exit code is used
to figure out the behavior of your compiler.

Use

    ./test.bash -2 "java -cp lib/java-cup-11a-runtime.jar:bin mini_python.Main"

to run the code generation tests. Your compiler is called with the
filename, the generated code is then compiled with `gcc`, the
executable is run, and the standard output is compared to the expected
output.
