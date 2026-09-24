package pm.repos;

/**
 * Working-copy state of a clone for the detail view. Each value is null when unknown.
 *
 * @param branch       current branch, or "abc1234 (detached)"
 * @param changedFiles modified, staged or untracked entries
 * @param ahead        commits not pushed to the upstream branch (null without upstream)
 */
public record CloneStatus(String branch, Integer changedFiles, Integer ahead) {
}
