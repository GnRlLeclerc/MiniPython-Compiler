package mini_python.exception_handling;

import java.nio.file.Path;
import java.nio.file.Paths;

import mini_python.exception_handling.terminal.Color;

public class CompilationExceptionHandler {

    protected Path filepath;

    public CompilationExceptionHandler(String filename) {
        this.filepath = Paths.get(filename);
    }

    public void handle(Exception e) {
        if (!(e instanceof CompilationException)) {
            System.out.println("Unknown exception:");
            System.out.println(filepath.getFileName() + ":" + e.getMessage());
            System.exit(1);
        }

        // Compute a left monospaced offset to leave room for the line number
        CompilationException ce = (CompilationException) e;
        String strLine = String.valueOf(ce.location.line);
        int leftOffset = 1 + strLine.length();

        System.out
                .println(String.format("  %s Compiling %s v0.1.0 (%s)", Color.BOLD_GREEN,
                        Color.RESET + filepath.getFileName().toString(),
                        filepath.toAbsolutePath()));

        System.out.println(String.format("%serror%s%s: %s", Color.BOLD_RED, Color.RESET, Color.BOLD, e.getMessage()));

        System.out.println(String.format("  %s--> %s:%s:%s", Color.BOLD_BLUE, Color.RESET + filepath.toString(),
                ce.location.line, ce.location.column));
        System.exit(1);
    }
}
