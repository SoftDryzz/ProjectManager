package pm.migration;

import pm.util.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects database migration tools by the presence of their configuration files.
 *
 * @author SoftDryzz
 * @version 1.7.1
 * @since 1.7.1
 */
public final class MigrationDetector {

    private MigrationDetector() {}

    /**
     * Detects all migration tools present in a project directory.
     *
     * @param projectRoot project root path
     * @return list of detected tools, empty if none found or path is invalid
     */
    public static List<MigrationTool> detect(Path projectRoot) {
        if (projectRoot == null || !Files.isDirectory(projectRoot)) {
            return Collections.emptyList();
        }

        List<MigrationTool> tools = new ArrayList<>();

        // Prisma: prisma/schema.prisma
        if (Files.exists(projectRoot.resolve("prisma").resolve("schema.prisma"))) {
            tools.add(MigrationTool.PRISMA);
        }

        // Alembic: alembic.ini or alembic/ directory
        if (Files.exists(projectRoot.resolve(Constants.FILE_ALEMBIC_INI))
                || Files.isDirectory(projectRoot.resolve("alembic"))) {
            tools.add(MigrationTool.ALEMBIC);
        }

        // Diesel: diesel.toml
        if (Files.exists(projectRoot.resolve(Constants.FILE_DIESEL_TOML))) {
            tools.add(MigrationTool.DIESEL);
        }

        // Flyway: flyway.conf or flyway.toml
        if (Files.exists(projectRoot.resolve(Constants.FILE_FLYWAY_CONF))
                || Files.exists(projectRoot.resolve(Constants.FILE_FLYWAY_TOML))) {
            tools.add(MigrationTool.FLYWAY);
        }

        // Liquibase: liquibase.properties
        if (Files.exists(projectRoot.resolve(Constants.FILE_LIQUIBASE_PROPERTIES))) {
            tools.add(MigrationTool.LIQUIBASE);
        }

        // SQLx: .sqlx/ directory
        if (Files.isDirectory(projectRoot.resolve(".sqlx"))) {
            tools.add(MigrationTool.SQLX);
        }

        return tools;
    }
}
