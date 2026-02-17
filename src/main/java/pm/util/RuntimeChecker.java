package pm.util;

import pm.cli.OutputFormatter;
import pm.detector.ProjectType;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Verifies that the required runtime is installed before executing commands.
 *
 * <p>Checks for each project type:
 * <ul>
 * <li>Gradle: java</li>
 * <li>Maven: java, mvn</li>
 * <li>Node.js: node, npm</li>
 * <li>.NET: dotnet</li>
 * <li>Python: python or python3</li>
 * </ul>
 *
 * <p>If the runtime is not found, displays a friendly error with install instructions.
 *
 * @author SoftDryzz
 * @version 1.1.0
 * @since 1.1.0
 */
public final class RuntimeChecker {

    private RuntimeChecker() {
        throw new AssertionError("RuntimeChecker cannot be instantiated");
    }

    /**
     * Checks if the required runtime for the project type is available.
     * If not found, prints a friendly error and exits.
     *
     * @param type the project type to check
     */
    public static void checkRuntime(ProjectType type) {
        if (type == null || type == ProjectType.UNKNOWN) {
            return;
        }

        switch (type) {
            case GRADLE -> {
                if (!isCommandAvailable("java", "-version")) {
                    printMissing("Java",
                            "This Gradle project requires Java to build/run.",
                            "winget install Microsoft.OpenJDK.17",
                            "https://adoptium.net/temurin/releases/");
                    System.exit(1);
                }
            }
            case MAVEN -> {
                if (!isCommandAvailable("java", "-version")) {
                    printMissing("Java",
                            "This Maven project requires Java to build/run.",
                            "winget install Microsoft.OpenJDK.17",
                            "https://adoptium.net/temurin/releases/");
                    System.exit(1);
                }
            }
            case NODEJS -> {
                if (!isCommandAvailable("node", "--version")) {
                    printMissing("Node.js",
                            "This Node.js project requires Node and npm.",
                            "winget install OpenJS.NodeJS.LTS",
                            "https://nodejs.org/en/download/");
                    System.exit(1);
                }
                if (!isCommandAvailable("npm", "--version")) {
                    printMissing("npm",
                            "npm is required but was not found. It usually comes with Node.js.",
                            "winget install OpenJS.NodeJS.LTS",
                            "https://nodejs.org/en/download/");
                    System.exit(1);
                }
            }
            case DOTNET -> {
                if (!isCommandAvailable("dotnet", "--version")) {
                    printMissing(".NET SDK",
                            "This .NET project requires the .NET SDK.",
                            "winget install Microsoft.DotNet.SDK.8",
                            "https://dotnet.microsoft.com/en-us/download");
                    System.exit(1);
                }
            }
            case PYTHON -> {
                if (!isCommandAvailable("python", "--version") &&
                        !isCommandAvailable("python3", "--version")) {
                    printMissing("Python",
                            "This Python project requires Python 3.",
                            "winget install Python.Python.3.12",
                            "https://www.python.org/downloads/");
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Checks if the required runtime for a project type is available.
     * Does NOT print anything or exit. Use this for diagnostic purposes (pm doctor).
     *
     * @param type the project type to check
     * @return true if the runtime is available (or no runtime is needed)
     */
    public static boolean isRuntimeAvailable(ProjectType type) {
        if (type == null || type == ProjectType.UNKNOWN) {
            return true;
        }

        return switch (type) {
            case GRADLE, MAVEN -> isCommandAvailable("java", "-version");
            case NODEJS -> isCommandAvailable("node", "--version") &&
                    isCommandAvailable("npm", "--version");
            case DOTNET -> isCommandAvailable("dotnet", "--version");
            case PYTHON -> isCommandAvailable("python", "--version") ||
                    isCommandAvailable("python3", "--version");
            default -> true;
        };
    }

    /**
     * Gets the version string of a command.
     * Returns null if the command is not available.
     *
     * @param command the command to check (e.g., "java", "node")
     * @param versionFlag the flag to get version (e.g., "-version", "--version")
     * @return version output string, or null if not available
     */
    public static String getVersion(String command, String versionFlag) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String[] cmd;
            if (os.contains("windows")) {
                cmd = new String[]{"cmd.exe", "/c", command + " " + versionFlag};
            } else {
                cmd = new String[]{"sh", "-c", command + " " + versionFlag};
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                    break; // Only need the first line
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 && !output.isEmpty()) {
                return output.toString().trim();
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a command is available in the system PATH.
     *
     * @param command the command to check
     * @param versionFlag the flag to test with
     * @return true if the command executes successfully
     */
    static boolean isCommandAvailable(String command, String versionFlag) {
        return getVersion(command, versionFlag) != null;
    }

    /**
     * Prints a friendly "runtime not found" error message with install instructions.
     */
    private static void printMissing(String runtime, String reason,
                                     String wingetCommand, String downloadUrl) {
        System.out.println();
        OutputFormatter.error("Runtime not found: " + runtime);
        System.out.println();
        System.out.println("  " + reason);
        System.out.println();
        System.out.println("  Install:");
        System.out.println("    Windows:  " + wingetCommand);
        System.out.println("    Download: " + downloadUrl);
        System.out.println();
    }
}
