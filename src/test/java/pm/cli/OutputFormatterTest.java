package pm.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pm.core.Project;
import pm.detector.ProjectType;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OutputFormatter")
class OutputFormatterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void redirectOutput() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    private String getOutput() {
        return outContent.toString();
    }

    // ============================================================
    // MESSAGE METHODS
    // ============================================================

    @Test
    @DisplayName("success prints green message")
    void successPrintsGreen() {
        OutputFormatter.success("Operation completed");
        String output = getOutput();

        assertTrue(output.contains("Operation completed"));
        assertTrue(output.contains(OutputFormatter.GREEN));
        assertTrue(output.contains(OutputFormatter.RESET));
    }

    @Test
    @DisplayName("error prints red message")
    void errorPrintsRed() {
        OutputFormatter.error("Something went wrong");
        String output = getOutput();

        assertTrue(output.contains("Something went wrong"));
        assertTrue(output.contains(OutputFormatter.RED));
    }

    @Test
    @DisplayName("warning prints yellow message")
    void warningPrintsYellow() {
        OutputFormatter.warning("Be careful");
        String output = getOutput();

        assertTrue(output.contains("Be careful"));
        assertTrue(output.contains(OutputFormatter.YELLOW));
    }

    @Test
    @DisplayName("info prints blue message")
    void infoPrintsBlue() {
        OutputFormatter.info("Processing...");
        String output = getOutput();

        assertTrue(output.contains("Processing..."));
        assertTrue(output.contains(OutputFormatter.BLUE));
    }

    @Test
    @DisplayName("section prints bold cyan header")
    void sectionPrintsHeader() {
        OutputFormatter.section("My Section");
        String output = getOutput();

        assertTrue(output.contains("My Section"));
        assertTrue(output.contains(OutputFormatter.BOLD));
        assertTrue(output.contains(OutputFormatter.CYAN));
    }

    // ============================================================
    // PROJECT LIST
    // ============================================================

    @Test
    @DisplayName("printProjectList shows empty message when no projects")
    void printProjectListEmpty() {
        OutputFormatter.printProjectList(new HashMap<>());
        String output = getOutput();

        assertTrue(output.contains("No projects registered yet"));
        assertTrue(output.contains("pm add"));
    }

    @Test
    @DisplayName("printProjectList shows project count")
    void printProjectListWithProjects() {
        Map<String, Project> projects = new HashMap<>();
        projects.put("api", new Project("api", Paths.get("/tmp/api"), ProjectType.MAVEN));

        OutputFormatter.printProjectList(projects);
        String output = getOutput();

        assertTrue(output.contains("Registered Projects (1)"));
        assertTrue(output.contains("api"));
    }

    // ============================================================
    // COMMANDS
    // ============================================================

    @Test
    @DisplayName("printCommands shows empty message when no commands")
    void printCommandsEmpty() {
        Project project = new Project("test", Paths.get("/tmp/test"), ProjectType.UNKNOWN);
        OutputFormatter.printCommands(project);
        String output = getOutput();

        assertTrue(output.contains("No commands configured"));
    }

    @Test
    @DisplayName("printCommands shows command list")
    void printCommandsWithCommands() {
        Project project = new Project("api", Paths.get("/tmp/api"), ProjectType.GRADLE);
        project.addCommand("build", "gradle build");
        project.addCommand("test", "gradle test");

        OutputFormatter.printCommands(project);
        String output = getOutput();

        assertTrue(output.contains("Commands for"));
        assertTrue(output.contains("build"));
        assertTrue(output.contains("gradle build"));
    }

    // ============================================================
    // ALL COMMANDS (--all)
    // ============================================================

    @Test
    @DisplayName("printAllCommands shows all projects with commands")
    void printAllCommandsShowsAll() {
        Map<String, Project> projects = new HashMap<>();

        Project api = new Project("api", Paths.get("/tmp/api"), ProjectType.MAVEN);
        api.addCommand("build", "mvn package");
        api.addCommand("deploy", "docker compose up");
        projects.put("api", api);

        Project web = new Project("web", Paths.get("/tmp/web"), ProjectType.NODEJS);
        web.addCommand("build", "npm run build");
        web.addCommand("tunnel", "npx expo start --tunnel");
        projects.put("web", web);

        OutputFormatter.printAllCommands(projects);
        String output = getOutput();

        assertTrue(output.contains("Commands for All Projects (2)"));
        assertTrue(output.contains("api"));
        assertTrue(output.contains("mvn package"));
        assertTrue(output.contains("web"));
        assertTrue(output.contains("npx expo start --tunnel"));
    }

    @Test
    @DisplayName("printAllCommands shows 'no commands' for empty project")
    void printAllCommandsShowsEmpty() {
        Map<String, Project> projects = new HashMap<>();
        projects.put("empty", new Project("empty", Paths.get("/tmp/empty"), ProjectType.UNKNOWN));

        OutputFormatter.printAllCommands(projects);
        String output = getOutput();

        assertTrue(output.contains("empty"));
        assertTrue(output.contains("No commands configured"));
    }

    @Test
    @DisplayName("printAllCommands shows project type in parentheses")
    void printAllCommandsShowsType() {
        Map<String, Project> projects = new HashMap<>();
        Project p = new Project("rust-app", Paths.get("/tmp/rust"), ProjectType.RUST);
        p.addCommand("build", "cargo build");
        projects.put("rust-app", p);

        OutputFormatter.printAllCommands(projects);
        String output = getOutput();

        assertTrue(output.contains("rust-app"));
        assertTrue(output.contains("Rust"));
    }

    // ============================================================
    // DEFAULT / CUSTOM COMMAND SECTIONS
    // ============================================================

    @Test
    @DisplayName("printCommands separates default and custom commands")
    void printCommandsSeparatesDefaultAndCustom() {
        Project project = new Project("myapp", Paths.get("/tmp/myapp"), ProjectType.GRADLE);
        project.addCommand("build", "gradle build");
        project.addCommand("run", "gradle run");
        project.addCommand("tunnel", "npx expo start --tunnel");
        project.addCommand("lint", "npm run lint");

        OutputFormatter.printCommands(project);
        String output = getOutput();

        assertTrue(output.contains("Default"), "Should have a 'Default' section header");
        assertTrue(output.contains("Custom"), "Should have a 'Custom' section header");
        // Default commands appear under Default header, custom under Custom
        int defaultIdx = output.indexOf("Default");
        int customIdx = output.indexOf("Custom");
        int buildIdx = output.indexOf("build");
        int tunnelIdx = output.indexOf("tunnel");
        assertTrue(buildIdx > defaultIdx && buildIdx < customIdx,
                "build should appear between Default and Custom headers");
        assertTrue(tunnelIdx > customIdx,
                "tunnel should appear after Custom header");
    }

    @Test
    @DisplayName("printCommands shows only Default section when no custom commands")
    void printCommandsOnlyDefault() {
        Project project = new Project("myapp", Paths.get("/tmp/myapp"), ProjectType.MAVEN);
        project.addCommand("build", "mvn package");
        project.addCommand("test", "mvn test");

        OutputFormatter.printCommands(project);
        String output = getOutput();

        assertTrue(output.contains("Default"), "Should have Default section");
        assertFalse(output.contains("Custom"), "Should NOT have Custom section");
    }

    @Test
    @DisplayName("printCommands shows only Custom section when no default commands")
    void printCommandsOnlyCustom() {
        Project project = new Project("myapp", Paths.get("/tmp/myapp"), ProjectType.UNKNOWN);
        project.addCommand("deploy", "docker compose up");
        project.addCommand("lint", "eslint .");

        OutputFormatter.printCommands(project);
        String output = getOutput();

        assertFalse(output.contains("Default"), "Should NOT have Default section");
        assertTrue(output.contains("Custom"), "Should have Custom section");
    }

    @Test
    @DisplayName("printCommands classifies 'stop' as a default command")
    void printCommandsStopIsDefault() {
        Project project = new Project("docker-app", Paths.get("/tmp/docker"), ProjectType.DOCKER);
        project.addCommand("build", "docker compose build");
        project.addCommand("run", "docker compose up");
        project.addCommand("stop", "docker compose down");
        project.addCommand("deploy", "kubectl apply -f k8s/");

        OutputFormatter.printCommands(project);
        String output = getOutput();

        assertTrue(output.contains("Default"), "Should have Default section");
        assertTrue(output.contains("Custom"), "Should have Custom section");
        int defaultIdx = output.indexOf("Default");
        int customIdx = output.indexOf("Custom");
        int stopIdx = output.indexOf("stop");
        int deployIdx = output.indexOf("deploy");
        assertTrue(stopIdx > defaultIdx && stopIdx < customIdx,
                "stop should appear in Default section");
        assertTrue(deployIdx > customIdx,
                "deploy should appear in Custom section");
    }

    // ============================================================
    // ENVIRONMENT VARIABLES
    // ============================================================

    @Test
    @DisplayName("printEnvVars does nothing when no vars")
    void printEnvVarsEmpty() {
        Project project = new Project("test", Paths.get("/tmp/test"), ProjectType.UNKNOWN);
        OutputFormatter.printEnvVars(project);
        String output = getOutput();

        assertTrue(output.isEmpty());
    }

    @Test
    @DisplayName("printEnvVars shows variables")
    void printEnvVarsWithVars() {
        Project project = new Project("api", Paths.get("/tmp/api"), ProjectType.MAVEN);
        project.addEnvVar("PORT", "8080");
        project.addEnvVar("DEBUG", "true");

        OutputFormatter.printEnvVars(project);
        String output = getOutput();

        assertTrue(output.contains("Environment Variables"));
        assertTrue(output.contains("PORT"));
        assertTrue(output.contains("8080"));
    }

    // ============================================================
    // GIT FEEDBACK
    // ============================================================

    @Test
    @DisplayName("printProject shows 'not a repository' for non-git project")
    void printProjectShowsNotARepo() {
        // Project with path that is not a git repo
        Project project = new Project("test", Paths.get(System.getProperty("java.io.tmpdir")), ProjectType.NODEJS);
        OutputFormatter.printProject(project);
        String output = getOutput();

        assertTrue(output.contains("not a repository"));
    }

    // ============================================================
    // ANSI CONSTANTS
    // ============================================================

    @Test
    @DisplayName("ANSI color constants are not null")
    void ansiConstantsExist() {
        assertNotNull(OutputFormatter.GREEN);
        assertNotNull(OutputFormatter.RED);
        assertNotNull(OutputFormatter.YELLOW);
        assertNotNull(OutputFormatter.BLUE);
        assertNotNull(OutputFormatter.CYAN);
        assertNotNull(OutputFormatter.GRAY);
        assertNotNull(OutputFormatter.BOLD);
        assertNotNull(OutputFormatter.RESET);
    }
}
