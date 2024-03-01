# MiniPython compiler

Compiler for a subset of Python made with Java.

Run `make` to build the compiler.

Compile a minipython file to assembly with

```bash
./minipython file.py
```

Compile the `C` code with

```bash
make libc
```

You can then compile and link with the `C` code, and then execute the generated assembly code using an assembler.

```bash
make compile
./test
```

## Project Structure

```bash
├── .vscode   # VSCode configuration
│
├── lib       # Library dependencies
│
├── src       # Main application folder
│   ├── libc_extended    # C helper functions
│   │
│   └── mini_python      # Main package
│       │
│       ├── Main.java      # Main class (CLI program)
│       │
│       ├── Compile.java   # Compilation entrypoint: TFile -> X86_64
│       ├── Compiler.java  # Compiler logic: implements TVisitor
│       ├── X86_64.java    # X86_64 symbols utilities
│       │
│       ├── Typing.java    # Type checking entrypoint: File -> TFile
│       ├── Typer.java     # Type Checking logic: implements Visitor
│       ├── Syntax.java    # Syntax & TSyntax definitions
│       │
│       ├── MyLexer.java   # Java Lexer
│       ├── sym.java       # Lexer Symbol definitions
│       ├── Lexer.flex     # JFlex Lexer configuration
│       ├── Lexer.java     # Autogenerated Java Lexer
│       │
│       ├── MyParser.java  # Java Parser
│       ├── Parser.cup     # Parser configuration
│       └── Parser.java    # Autogenerated Java Parser
│
├── tests     # test configuration
│   │
│   ├── exec       # Supposed to execute successfully
│   ├── exec-fail  # Supposed to fail at runtime
│   │
│   ├── syntax     # Syntax tests
│   │   ├── bad      # Supposed to fail
│   │   └── good     # Supposed to succeed
│   │
│   └── typing     # Type Checking tests
│       ├── bad      # Supposed to fail
│       └── good     # Supposed to succeed
│
├── minipython  # Compiler bash entrypoint
│
└── test.bash   # Shell script to run the tests
```

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

Test the compiler with the `test.bash` script:

```bash
./test.bash -1 "java -cp lib/java-cup-11a-runtime.jar:bin mini_python.Main"  # Parsing
./test.bash -2 "java -cp lib/java-cup-11a-runtime.jar:bin mini_python.Main"  # Type Checking
./test.bash -3 "java -cp lib/java-cup-11a-runtime.jar:bin mini_python.Main"  # Code Generation
```

For the type checking tests, your compiler is called with command
line option `--type-only` and the filename, and the exit code is used
to figure out the behavior of your compiler.

The script does the following:

* call the compiler on a test file
* compile the generated assembly code with gcc
* run the executable
