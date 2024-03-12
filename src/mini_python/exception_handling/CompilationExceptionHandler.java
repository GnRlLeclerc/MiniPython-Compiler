package mini_python.exception_handling;

import java.nio.file.Path;
import java.nio.file.Paths;

import mini_python.exception_handling.exceptions.WrongArgCountException;
import mini_python.exception_handling.terminal.Color;
import mini_python.syntax.Ident;
import mini_python.syntax.Location;
import mini_python.syntax.Span;

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

        System.out.printf("%serror%s%s: %s\n", Color.BOLD_RED, Color.RESET, Color.BOLD, e.getMessage());

        System.out.printf("  %s--> %s:%s:%s\n", Color.BOLD_BLUE, Color.RESET + filepath.toString(),
                ce.location.line, ce.location.column);

        // Print source code snippet
        String genericPrefix = String.format("%s%s| ", " ".repeat(leftOffset), Color.BOLD_BLUE);
        System.out.println(genericPrefix);
        String code = LOCExtractor.extractLine(filepath, ce.location.line);
        System.out.printf("%s | %s%s\n", ce.location.line, Color.RESET, code);

        // Print error message with indications
        Span span = ce.getIndicatorSpan();
        System.out
                .println(
                        new StringBuilder().append(genericPrefix).append(Color.BOLD_RED)
                                .append(" ".repeat(span.start.column))
                                .append("^".repeat(span.length))
                                .append(ce.getErrorHelper()).append(Color.RESET)
                                .toString());

        // Additional special handling for function call exceptions
        if (e instanceof WrongArgCountException) {
            WrongArgCountException wace = (WrongArgCountException) e;
            Location defLoc = wace.definition.f.loc;
            strLine = String.valueOf(defLoc.line);
            leftOffset = 1 + strLine.length();
            genericPrefix = String.format("%s%s| ", " ".repeat(leftOffset), Color.BOLD_BLUE);

            System.out.println(genericPrefix);
            System.out.printf("%snote%s: function defined here\n", Color.BOLD_GREEN, Color.RESET);
            System.out.printf("  %s--> %s:%s:%s\n", Color.BOLD_BLUE, Color.RESET + filepath.toString(),
                    defLoc.line, defLoc.column);
            System.out.println(genericPrefix);

            // Print the source line
            code = LOCExtractor.extractLine(filepath, defLoc.line);
            System.out.println(String.format("%s | %s%s", defLoc.line, Color.RESET, code));

            System.out.print(new StringBuilder().append(genericPrefix).append(" ".repeat(defLoc.column))
                    .append(Color.BOLD_GREEN)
                    .append("^".repeat(wace.definition.f.id.length()))
                    .toString());

            // Underline all existing arguments
            System.out.print(Color.BOLD_BLUE);
            int idEnd = defLoc.column + wace.definition.f.id.length();
            for (Ident id : wace.definition.l) {
                int idStart = id.loc.column;
                System.out.print(" ".repeat(idStart - idEnd));
                System.out.print("-".repeat(id.id.length()));
                idEnd = idStart + id.id.length();
            }
            System.out.println(Color.RESET);
        }
        System.out.println();

        System.exit(1);
    }
}
