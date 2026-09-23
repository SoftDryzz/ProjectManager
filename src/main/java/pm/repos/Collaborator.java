package pm.repos;

/** A repository collaborator and their role ("admin", "write", ...). */
public record Collaborator(String login, String role) {
}
