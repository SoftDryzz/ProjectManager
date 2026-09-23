package pm.repos;

import java.nio.file.Path;
import java.util.List;

/**
 * Result of scanning the disk for clones.
 *
 * @param clones       clones found, sorted by path
 * @param missingRoots configured roots that do not exist
 */
public record ScanResult(List<LocalClone> clones, List<Path> missingRoots) {
}
