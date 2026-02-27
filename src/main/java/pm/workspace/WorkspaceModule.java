package pm.workspace;

import pm.detector.ProjectType;

/**
 * Represents a module within a workspace/monorepo.
 *
 * @param name         module name (e.g., "app", "lib-core")
 * @param relativePath relative path from the project root (e.g., "packages/app")
 * @param type         detected project type of the module
 */
public record WorkspaceModule(
        String name,
        String relativePath,
        ProjectType type
) {}
