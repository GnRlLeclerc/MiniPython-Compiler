package mini_python.exception_handling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Extract lines of code from a source file in order to display better error
 * messages
 */
public class LOCExtractor {
    public static String extractLine(Path filepath, int line) {
        try (Stream<String> lines = Files.lines(filepath)) {
            return lines.skip(line - 1).findFirst().orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
