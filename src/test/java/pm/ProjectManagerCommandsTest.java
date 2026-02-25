package pm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pm.core.Project;
import pm.detector.ProjectType;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectManager - Custom Commands")
class ProjectManagerCommandsTest {

    // ============================================================
    // PROJECT-LEVEL COMMAND MANAGEMENT
    // ============================================================

    @Test
    @DisplayName("addCommand creates a new custom command")
    void addCommandCreatesNew() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        project.addCommand("tunnel", "npx expo start --tunnel");

        assertTrue(project.hasCommand("tunnel"));
        assertEquals("npx expo start --tunnel", project.getCommand("tunnel"));
    }

    @Test
    @DisplayName("addCommand can add multiple custom commands")
    void addMultipleCustomCommands() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        project.addCommand("build", "npm run build");
        project.addCommand("lint", "npm run lint");
        project.addCommand("deploy", "docker compose up -d");
        project.addCommand("tunnel", "npx expo start --tunnel");

        assertEquals(4, project.commandCount());
        assertEquals("npm run lint", project.getCommand("lint"));
        assertEquals("docker compose up -d", project.getCommand("deploy"));
        assertEquals("npx expo start --tunnel", project.getCommand("tunnel"));
    }

    @Test
    @DisplayName("addCommand overwrites existing command with same name")
    void addCommandOverwritesExisting() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        project.addCommand("run", "npm start");
        project.addCommand("run", "npx expo start --tunnel");

        assertEquals("npx expo start --tunnel", project.getCommand("run"));
        assertEquals(1, project.commandCount());
    }

    @Test
    @DisplayName("removeCommand removes a custom command")
    void removeCommandWorks() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        project.addCommand("tunnel", "npx expo start --tunnel");
        project.addCommand("lint", "npm run lint");

        project.removeCommand("tunnel");

        assertFalse(project.hasCommand("tunnel"));
        assertTrue(project.hasCommand("lint"));
        assertEquals(1, project.commandCount());
    }

    @Test
    @DisplayName("removeCommand does nothing for non-existent command")
    void removeNonExistentCommandDoesNothing() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        project.addCommand("build", "npm run build");
        int countBefore = project.commandCount();

        project.removeCommand("nonexistent");

        assertEquals(countBefore, project.commandCount());
        assertTrue(project.hasCommand("build"));
    }

    @Test
    @DisplayName("hasCommand returns false for non-existent command")
    void hasCommandReturnsFalse() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        assertFalse(project.hasCommand("tunnel"));
    }

    @Test
    @DisplayName("Custom commands coexist with default commands")
    void customCommandsCoexistWithDefaults() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        // Simulate defaults
        project.addCommand("build", "npm run build");
        project.addCommand("run", "npm start");
        project.addCommand("test", "npm test");

        // Add custom commands
        project.addCommand("tunnel", "npx expo start --tunnel");
        project.addCommand("lint", "npm run lint");
        project.addCommand("deploy", "docker compose up -d");

        assertEquals(6, project.commandCount());
        // Defaults still intact
        assertEquals("npm run build", project.getCommand("build"));
        assertEquals("npm start", project.getCommand("run"));
        assertEquals("npm test", project.getCommand("test"));
        // Custom commands available
        assertEquals("npx expo start --tunnel", project.getCommand("tunnel"));
        assertEquals("npm run lint", project.getCommand("lint"));
        assertEquals("docker compose up -d", project.getCommand("deploy"));
    }

    @Test
    @DisplayName("Custom command with complex shell syntax is stored correctly")
    void complexCommandStored() {
        Project project = new Project("api", Paths.get("/tmp/api"), ProjectType.MAVEN);
        project.addCommand("start-db", "docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secret postgres:15");

        assertEquals("docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secret postgres:15",
                project.getCommand("start-db"));
    }

    @Test
    @DisplayName("Custom command with pipe characters is stored correctly")
    void commandWithPipesStored() {
        Project project = new Project("api", Paths.get("/tmp/api"), ProjectType.GRADLE);
        project.addCommand("check-port", "lsof -i :8080 | grep LISTEN");

        assertEquals("lsof -i :8080 | grep LISTEN", project.getCommand("check-port"));
    }

    // ============================================================
    // COMMAND PERSISTENCE (via commands map)
    // ============================================================

    @Test
    @DisplayName("commands() returns all commands including custom ones")
    void commandsIncludesCustom() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        project.addCommand("build", "npm run build");
        project.addCommand("tunnel", "npx expo start --tunnel");

        var commands = project.commands();

        assertEquals(2, commands.size());
        assertTrue(commands.containsKey("build"));
        assertTrue(commands.containsKey("tunnel"));
    }

    @Test
    @DisplayName("clearCommands removes all commands including custom ones")
    void clearCommandsRemovesAll() {
        Project project = new Project("my-app", Paths.get("/tmp/my-app"), ProjectType.NODEJS);
        project.addCommand("build", "npm run build");
        project.addCommand("tunnel", "npx expo start --tunnel");

        project.clearCommands();

        assertEquals(0, project.commandCount());
        assertFalse(project.hasCommand("build"));
        assertFalse(project.hasCommand("tunnel"));
    }
}
