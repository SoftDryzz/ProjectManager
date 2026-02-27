package pm.migration;

/**
 * Supported database migration tools with their CLI commands.
 *
 * @author SoftDryzz
 * @version 1.7.1
 * @since 1.7.1
 */
public enum MigrationTool {

    PRISMA("Prisma", "npx prisma migrate deploy", "npx prisma migrate status", "npx"),
    ALEMBIC("Alembic", "alembic upgrade head", "alembic current", "alembic"),
    DIESEL("Diesel", "diesel migration run", "diesel migration list", "diesel"),
    FLYWAY("Flyway", "flyway migrate", "flyway info", "flyway"),
    LIQUIBASE("Liquibase", "liquibase update", "liquibase status", "liquibase"),
    SQLX("SQLx", "sqlx migrate run", "sqlx migrate info", "sqlx");

    private final String displayName;
    private final String migrateCommand;
    private final String statusCommand;
    private final String binary;

    MigrationTool(String displayName, String migrateCommand, String statusCommand, String binary) {
        this.displayName = displayName;
        this.migrateCommand = migrateCommand;
        this.statusCommand = statusCommand;
        this.binary = binary;
    }

    public String displayName() {
        return displayName;
    }

    public String migrateCommand() {
        return migrateCommand;
    }

    public String statusCommand() {
        return statusCommand;
    }

    public String binary() {
        return binary;
    }
}
