# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.3.x   | Yes       |
| < 1.3.0 | No        |

Only the latest release receives security updates. We recommend always using the latest version via `pm update`.

---

## Reporting a Vulnerability

If you discover a security vulnerability, please report it **privately**:

1. **Do NOT open a public issue** — this could expose the vulnerability before a fix is available
2. Contact via email: **[security@softdryzz.com](mailto:security@softdryzz.com)**
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Affected version(s)
   - Potential impact

You will receive a response within **72 hours**. If the vulnerability is confirmed, a fix will be released as soon as possible and you will be credited in the release notes (unless you prefer to remain anonymous).

---

## Security Model

### What ProjectManager does

ProjectManager is a **local CLI tool** that manages project metadata and executes shell commands on behalf of the user. It does **not** run a server, listen on any port, or accept remote connections.

### Data storage

- All data is stored locally in `~/.projectmanager/projects.json`
- **Atomic writes** (since v1.3.7) — data is written to a temp file first, then renamed. No partial writes can corrupt your data.
- **Automatic backup** (since v1.3.7) — `projects.json.bak` is created before every write. If the main file becomes corrupted, it is automatically restored from backup on the next command.
- No data is sent to external servers (except the GitHub API for update checks)
- No telemetry, analytics, or tracking of any kind

### Command execution

ProjectManager executes shell commands configured by the user. These commands run with the **same permissions as the user** who invokes `pm`.

**Current limitations (being addressed in v1.3.8):**
- Project paths are not escaped before being passed to shell commands. Paths containing shell metacharacters (`&`, `|`, `;`, etc.) could lead to unintended command execution. Avoid registering projects with special characters in their paths until v1.3.8.

### Network access

ProjectManager only connects to the internet for **two purposes**:

1. **Update check** — On each run, it checks `https://api.github.com/repos/SoftDryzz/ProjectManager/releases/latest` for new versions
2. **Auto-update download** — When `pm update` is used, it downloads the JAR from GitHub Releases

Both connections use HTTPS. No authentication tokens or personal data are transmitted.

**Current limitations (being addressed in v1.3.9):**
- Downloaded JAR integrity is validated only by a minimum size check (> 1KB). A more robust validation (expected file size from API response) is planned.
- Redirect loops from the GitHub API are not capped, which could cause a hang.

### File system access

ProjectManager reads and writes:
- `~/.projectmanager/projects.json` — project registry
- `~/.projectmanager/projectmanager.jar` — the application itself (during updates)
- Project directories — only to detect project types (reads `pom.xml`, `package.json`, etc.). It **never modifies** project files.

### Dependencies

ProjectManager is built as a fat JAR with these dependencies:
- **Gson** (Google) — JSON serialization
- **Maven Shade Plugin** — build-time only, for creating the fat JAR

No runtime dependencies are pulled from the network. The application is fully self-contained.

---

## Known Security Considerations

### Shell command injection via project paths
- **Status:** Known, fix planned for v1.3.8
- **Risk:** Low — requires the user to manually register a project with a malicious path
- **Mitigation:** Do not register projects whose paths contain shell metacharacters (`&`, `|`, `;`, `` ` ``, `$`, etc.)

### Update mechanism integrity
- **Status:** Known, improvements planned for v1.3.9
- **Risk:** Low — downloads only from GitHub Releases over HTTPS
- **Mitigation:** Verify the downloaded JAR manually if concerned (`sha256sum` comparison with the release page)
- **Installation:** For detailed installation and verification steps, see the [Installation Guide](scripts/INSTALL.md)

### Local file permissions
- **Status:** Acceptable
- **Risk:** Low — `projects.json` has default user-only permissions. However, on shared systems, other users with access to your home directory could read or modify it.
- **Mitigation:** Ensure `~/.projectmanager/` has appropriate permissions (`chmod 700` on Linux/Mac)

---

## Security Roadmap

| Version | Security Improvement |
|---------|---------------------|
| v1.3.7 ✅ | Atomic file writes, automatic backup, corrupted data recovery, field validation on load, user-friendly error messages (no stack traces) |
| v1.3.8  | Escape shell metacharacters in project paths; validate directories before execution |
| v1.3.9  | Validate download integrity; cap redirect loops; distinguish network error types |
| v1.5.2  | `pm secure` command — filesystem security scan for project best practices |

---

## Best Practices for Users

1. **Keep ProjectManager updated** — Run `pm update` regularly
2. **Follow the installation guide** — See [INSTALL.md](scripts/INSTALL.md) for complete setup instructions and troubleshooting
3. **Use descriptive project names** — Avoid names that match PM commands (`build`, `run`, `list`)
4. **Avoid special characters in project paths** — Until v1.3.8, paths with `&`, `|`, `;` may cause issues
5. **Review custom commands** — Commands set with `pm commands set` execute as your user. Review them before running.
6. **Protect your home directory** — On shared systems, ensure `~/.projectmanager/` is not world-readable
