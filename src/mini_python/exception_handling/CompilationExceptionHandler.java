package mini_python.exception_handling;

import java.nio.file.Path;
import java.nio.file.Paths;

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

        // TODO: custom treatment
        System.out.println("Compilation exception:");
        System.out.println(filepath.getFileName() + ":" + e.getMessage());
        System.exit(1);
    }
}
