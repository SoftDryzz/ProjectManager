package pm.export;

import java.util.List;

/**
 * Result of an import operation.
 *
 * @param imported  number of projects successfully saved to the store
 * @param skipped   names of projects skipped because they already exist
 * @param warnings  list of warning messages (e.g., missing paths, invalid data)
 *
 * @author SoftDryzz
 * @version 1.6.4
 * @since 1.6.4
 */
public record ImportResult(
        int imported,
        List<String> skipped,
        List<String> warnings
) {}
