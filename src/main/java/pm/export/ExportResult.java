package pm.export;

import java.nio.file.Path;
import java.util.List;

/**
 * Result of an export operation.
 *
 * @param exported   number of projects successfully written to the file
 * @param outputFile the path of the file that was written
 * @param notFound   names that were requested but do not exist in the store
 *
 * @author SoftDryzz
 * @version 1.6.4
 * @since 1.6.4
 */
public record ExportResult(
        int exported,
        Path outputFile,
        List<String> notFound
) {}
