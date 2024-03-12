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

        // Print error header
        System.out
                .println(String.format("  %s Compiling %s v0.1.0 (%s)", Color.BOLD_GREEN,
                        Color.RESET + filepath.getFileName().toString(),
                        filepath.toAbsolutePath()));

        System.out.println(String.format("%serror%s%s: %s", Color.BOLD_RED, Color.RESET, Color.BOLD, e.getMessage()));

        System.out.println(String.format("  %s--> %s:%s:%s", Color.BOLD_BLUE, Color.RESET + filepath.toString(),
                ce.location.line, ce.location.column));

        // Print source code snippet
        String genericPrefix = String.format("%s%s| ", " ".repeat(leftOffset), Color.BOLD_BLUE);
        System.out.println(genericPrefix);
        String code = LOCExtractor.extractLine(filepath, ce.location.line);
        System.out.println(String.format("%s | %s%s", ce.location.line, Color.RESET, code));

        // Print error message with indications
        Tuple<Integer, Integer> span = ce.getIndicatorSpan();
        System.out
                .println(
                        new StringBuilder().append(genericPrefix).append(Color.BOLD_RED)
                                .append(" ".repeat(span.x)).append("^".repeat(span.y - span.x)).append(" ")
                                .append(ce.getErrorHelper()).append(Color.RESET)
                                .toString());

        System.exit(1);
    }
}
