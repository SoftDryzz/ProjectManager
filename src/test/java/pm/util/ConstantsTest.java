package pm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Constants")
class ConstantsTest {

    @Test
    @DisplayName("VERSION is not null or blank")
    void versionIsSet() {
        assertNotNull(Constants.VERSION);
        assertFalse(Constants.VERSION.isBlank());
    }

    @Test
    @DisplayName("HOME path is not null")
    void homePathIsSet() {
        assertNotNull(Constants.HOME);
    }

    @Test
    @DisplayName("CONFIG_DIR is inside HOME")
    void configDirInsideHome() {
        assertTrue(Constants.CONFIG_DIR.startsWith(Constants.HOME));
        assertTrue(Constants.CONFIG_DIR.toString().contains(".projectmanager"));
    }

    @Test
    @DisplayName("PROJECTS_FILE is inside CONFIG_DIR")
    void projectsFileInsideConfigDir() {
        assertTrue(Constants.PROJECTS_FILE.startsWith(Constants.CONFIG_DIR));
        assertTrue(Constants.PROJECTS_FILE.toString().endsWith("projects.json"));
    }

    @Test
    @DisplayName("CACHE_DIR is inside CONFIG_DIR")
    void cacheDirInsideConfigDir() {
        assertTrue(Constants.CACHE_DIR.startsWith(Constants.CONFIG_DIR));
    }

    @Test
    @DisplayName("Gradle commands are not blank")
    void gradleCommandsExist() {
        assertFalse(Constants.BUILD_GRADLE.isBlank());
        assertFalse(Constants.RUN_GRADLE.isBlank());
        assertFalse(Constants.TEST_GRADLE.isBlank());
        assertFalse(Constants.CLEAN_GRADLE.isBlank());
    }

    @Test
    @DisplayName("Maven commands are not blank")
    void mavenCommandsExist() {
        assertFalse(Constants.BUILD_MAVEN.isBlank());
        assertFalse(Constants.RUN_MAVEN.isBlank());
        assertFalse(Constants.TEST_MAVEN.isBlank());
        assertFalse(Constants.CLEAN_MAVEN.isBlank());
    }

    @Test
    @DisplayName("Node.js commands are not blank")
    void npmCommandsExist() {
        assertFalse(Constants.BUILD_NPM.isBlank());
        assertFalse(Constants.RUN_NPM.isBlank());
        assertFalse(Constants.TEST_NPM.isBlank());
    }

    @Test
    @DisplayName(".NET commands are not blank")
    void dotnetCommandsExist() {
        assertFalse(Constants.BUILD_DOTNET.isBlank());
        assertFalse(Constants.RUN_DOTNET.isBlank());
        assertFalse(Constants.TEST_DOTNET.isBlank());
    }

    @Test
    @DisplayName("Rust commands are not blank")
    void rustCommandsExist() {
        assertFalse(Constants.BUILD_CARGO.isBlank());
        assertFalse(Constants.RUN_CARGO.isBlank());
        assertFalse(Constants.TEST_CARGO.isBlank());
        assertFalse(Constants.CLEAN_CARGO.isBlank());
    }

    @Test
    @DisplayName("Go commands are not blank")
    void goCommandsExist() {
        assertFalse(Constants.BUILD_GO.isBlank());
        assertFalse(Constants.RUN_GO.isBlank());
        assertFalse(Constants.TEST_GO.isBlank());
        assertFalse(Constants.CLEAN_GO.isBlank());
    }

    @Test
    @DisplayName("pnpm commands are not blank")
    void pnpmCommandsExist() {
        assertFalse(Constants.BUILD_PNPM.isBlank());
        assertFalse(Constants.RUN_PNPM.isBlank());
        assertFalse(Constants.TEST_PNPM.isBlank());
    }

    @Test
    @DisplayName("Bun commands are not blank")
    void bunCommandsExist() {
        assertFalse(Constants.BUILD_BUN.isBlank());
        assertFalse(Constants.RUN_BUN.isBlank());
        assertFalse(Constants.TEST_BUN.isBlank());
    }

    @Test
    @DisplayName("Yarn commands are not blank")
    void yarnCommandsExist() {
        assertFalse(Constants.BUILD_YARN.isBlank());
        assertFalse(Constants.RUN_YARN.isBlank());
        assertFalse(Constants.TEST_YARN.isBlank());
    }

    @Test
    @DisplayName("Flutter commands are not blank")
    void flutterCommandsExist() {
        assertFalse(Constants.BUILD_FLUTTER.isBlank());
        assertFalse(Constants.RUN_FLUTTER.isBlank());
        assertFalse(Constants.TEST_FLUTTER.isBlank());
        assertFalse(Constants.CLEAN_FLUTTER.isBlank());
    }

    @Test
    @DisplayName("Docker commands are not blank")
    void dockerCommandsExist() {
        assertFalse(Constants.BUILD_DOCKER.isBlank());
        assertFalse(Constants.RUN_DOCKER.isBlank());
        assertFalse(Constants.STOP_DOCKER.isBlank());
        assertFalse(Constants.CLEAN_DOCKER.isBlank());
    }

    @Test
    @DisplayName("Detection file names are not blank")
    void detectionFilesExist() {
        assertFalse(Constants.FILE_BUILD_GRADLE.isBlank());
        assertFalse(Constants.FILE_BUILD_GRADLE_KTS.isBlank());
        assertFalse(Constants.FILE_POM_XML.isBlank());
        assertFalse(Constants.FILE_PACKAGE_JSON.isBlank());
        assertFalse(Constants.FILE_CSPROJ.isBlank());
        assertFalse(Constants.FILE_REQUIREMENTS_TXT.isBlank());
        assertFalse(Constants.FILE_CARGO_TOML.isBlank());
        assertFalse(Constants.FILE_GO_MOD.isBlank());
        assertFalse(Constants.FILE_PNPM_LOCK.isBlank());
        assertFalse(Constants.FILE_BUN_LOCKB.isBlank());
        assertFalse(Constants.FILE_BUN_LOCK.isBlank());
        assertFalse(Constants.FILE_YARN_LOCK.isBlank());
        assertFalse(Constants.FILE_DOCKER_COMPOSE_YML.isBlank());
        assertFalse(Constants.FILE_DOCKER_COMPOSE_YAML.isBlank());
    }

    @Test
    @DisplayName("HOOK_TIMEOUT is positive")
    void hookTimeoutIsPositive() {
        assertTrue(Constants.HOOK_TIMEOUT > 0);
    }

    @Test
    @DisplayName("Cannot be instantiated")
    void cannotInstantiate() throws NoSuchMethodException {
        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}
