package pm.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MigrationDetector")
class MigrationDetectorTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("detect")
    class Detection {

        @Test
        @DisplayName("returns empty list for empty directory")
        void emptyDir() {
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("detects Prisma when prisma/schema.prisma exists")
        void detectsPrisma() throws IOException {
            Files.createDirectories(tempDir.resolve("prisma"));
            Files.createFile(tempDir.resolve("prisma").resolve("schema.prisma"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.PRISMA, tools.get(0));
        }

        @Test
        @DisplayName("detects Alembic when alembic.ini exists")
        void detectsAlembicIni() throws IOException {
            Files.createFile(tempDir.resolve("alembic.ini"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.ALEMBIC, tools.get(0));
        }

        @Test
        @DisplayName("detects Alembic when alembic/ directory exists")
        void detectsAlembicDir() throws IOException {
            Files.createDirectory(tempDir.resolve("alembic"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.ALEMBIC, tools.get(0));
        }

        @Test
        @DisplayName("detects Diesel when diesel.toml exists")
        void detectsDiesel() throws IOException {
            Files.createFile(tempDir.resolve("diesel.toml"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.DIESEL, tools.get(0));
        }

        @Test
        @DisplayName("detects Flyway when flyway.conf exists")
        void detectsFlywayConf() throws IOException {
            Files.createFile(tempDir.resolve("flyway.conf"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.FLYWAY, tools.get(0));
        }

        @Test
        @DisplayName("detects Flyway when flyway.toml exists")
        void detectsFlywayToml() throws IOException {
            Files.createFile(tempDir.resolve("flyway.toml"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.FLYWAY, tools.get(0));
        }

        @Test
        @DisplayName("detects Liquibase when liquibase.properties exists")
        void detectsLiquibase() throws IOException {
            Files.createFile(tempDir.resolve("liquibase.properties"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.LIQUIBASE, tools.get(0));
        }

        @Test
        @DisplayName("detects SQLx when .sqlx/ directory exists")
        void detectsSqlx() throws IOException {
            Files.createDirectory(tempDir.resolve(".sqlx"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(1, tools.size());
            assertEquals(MigrationTool.SQLX, tools.get(0));
        }

        @Test
        @DisplayName("detects multiple tools simultaneously")
        void detectsMultiple() throws IOException {
            Files.createDirectories(tempDir.resolve("prisma"));
            Files.createFile(tempDir.resolve("prisma").resolve("schema.prisma"));
            Files.createFile(tempDir.resolve("diesel.toml"));
            Files.createDirectory(tempDir.resolve(".sqlx"));
            List<MigrationTool> tools = MigrationDetector.detect(tempDir);
            assertEquals(3, tools.size());
            assertTrue(tools.contains(MigrationTool.PRISMA));
            assertTrue(tools.contains(MigrationTool.DIESEL));
            assertTrue(tools.contains(MigrationTool.SQLX));
        }

        @Test
        @DisplayName("returns empty for null path")
        void nullPath() {
            assertTrue(MigrationDetector.detect(null).isEmpty());
        }

        @Test
        @DisplayName("returns empty for nonexistent path")
        void nonexistentPath() {
            assertTrue(MigrationDetector.detect(tempDir.resolve("nope")).isEmpty());
        }
    }

    @Nested
    @DisplayName("MigrationTool enum")
    class ToolEnum {

        @Test
        @DisplayName("all tools have display names")
        void allHaveDisplayNames() {
            for (MigrationTool tool : MigrationTool.values()) {
                assertNotNull(tool.displayName());
                assertFalse(tool.displayName().isEmpty());
            }
        }

        @Test
        @DisplayName("all tools have migrate commands")
        void allHaveMigrateCommands() {
            for (MigrationTool tool : MigrationTool.values()) {
                assertNotNull(tool.migrateCommand());
                assertFalse(tool.migrateCommand().isEmpty());
            }
        }

        @Test
        @DisplayName("all tools have status commands")
        void allHaveStatusCommands() {
            for (MigrationTool tool : MigrationTool.values()) {
                assertNotNull(tool.statusCommand());
                assertFalse(tool.statusCommand().isEmpty());
            }
        }

        @Test
        @DisplayName("all tools have binary names")
        void allHaveBinaries() {
            for (MigrationTool tool : MigrationTool.values()) {
                assertNotNull(tool.binary());
                assertFalse(tool.binary().isEmpty());
            }
        }
    }
}
