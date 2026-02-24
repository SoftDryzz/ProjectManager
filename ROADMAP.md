# ProjectManager - Roadmap

> Ideas and planned features for future versions.

---

## v1.4.0 — Multi-ecosystem Install & Docker Support

### Install scripts per ecosystem
Create dedicated install scripts/commands so users can install ProjectManager using their preferred package manager:

| Ecosystem | Install Method | Status |
|-----------|---------------|--------|
| npm/npx | `npx projectmanager` or `npm i -g projectmanager` | Planned |
| pnpm | `pnpm add -g projectmanager` | Planned |
| Cargo | `cargo install projectmanager` | Planned |
| Homebrew | `brew install projectmanager` | Planned |
| Scoop (Windows) | `scoop install projectmanager` | Planned |
| Go | `go install github.com/SoftDryzz/ProjectManager@latest` | Planned |
| Bun | `bun add -g projectmanager` | Planned |
| Yarn | `yarn global add projectmanager` | Planned |

> **Note:** Each ecosystem install would wrap the JAR (or compile a native binary via GraalVM).

### Docker support
- Detect `Dockerfile`, `docker-compose.yml`, `docker-compose.yaml` in project root
- New project type: `DOCKER`
- Default commands:
  - `build` → `docker compose build`
  - `run` → `docker compose up`
  - `stop` → `docker compose down`
  - `clean` → `docker compose down -v --rmi all`
- Detect multi-service Docker Compose and list services
- Support `pm run <service>` to start individual services

---

## v1.5.0 — CI/CD & Workflow Awareness

### CI/CD detection
- Detect GitHub Actions (`.github/workflows/`)
- Detect GitLab CI (`.gitlab-ci.yml`)
- Detect Jenkins (`Jenkinsfile`)
- Show CI status in `pm info`
- New command: `pm ci` — open CI dashboard in browser or show last run status

### Linting & formatting integration
- Detect linters per project type:
  - Rust: `cargo fmt`, `cargo clippy`
  - Go: `gofmt`, `golangci-lint`
  - Node.js: `eslint`, `prettier`
  - Python: `ruff`, `black`, `flake8`
  - Java: `checkstyle`, `spotless`
- New command: `pm lint` — run detected linter
- New command: `pm fmt` — run detected formatter

---

## v1.6.0 — Multi-project Workspaces

### Monorepo support
- Detect monorepo structures:
  - Cargo workspaces (`[workspace]` in Cargo.toml)
  - npm/pnpm/yarn workspaces
  - Go multi-module repos
  - Gradle multi-project builds
- `pm list-modules` — show all sub-projects
- `pm build --all` — build all modules
- `pm test --all` — test all modules
- `pm run <module>` — run specific module

### Project templates
- `pm init <type>` — scaffold a new project from templates
- Built-in templates: Java (Maven/Gradle), Node.js, Rust, Go, Python, .NET
- Support custom templates from GitHub repos

---

## v1.7.0 — Environments & Secrets

### Environment management
- Detect `.env`, `.env.local`, `.env.production` files
- `pm env` — show current environment variables (masked secrets)
- `pm env switch <name>` — switch between environment files
- Warn if `.env` is not in `.gitignore`

### Secrets detection
- Scan for common secret patterns (API keys, tokens, passwords)
- Warn on `pm doctor` if secrets are committed
- Integration with Vaultic or similar tools for encrypted env files

---

## v2.0.0 — Dashboard & Analytics

### Interactive TUI dashboard
- Real-time project status dashboard (using a TUI library)
- Show: build status, test results, git status, dependencies
- Navigate between registered projects
- Quick actions (build, test, clean) from dashboard

### Dependency audit
- `pm audit` — check for known vulnerabilities
  - npm: `npm audit`
  - Cargo: `cargo audit`
  - Go: `govulncheck`
  - Python: `pip-audit`
  - Maven: OWASP dependency-check
- Show summary with severity levels
- Suggest fixes where available

### Performance tracking
- Track build times across runs
- `pm stats` — show build/test time trends
- Identify slow builds and suggest optimizations

---

## Ideas from FindMatch (Real-world Multi-stack Project)

These ideas come from analyzing [FindMatch](https://github.com/AXIOM-ZER0/FindMatch), a real-world project with Flutter + Rust + Docker + PostgreSQL + Redis, multiple CI workflows, and a 4-person team.

### Multi-language project detection
- Detect projects that use multiple languages (e.g., Rust backend + Flutter frontend)
- Show all detected types: `pm info` → "Types: RUST, FLUTTER, DOCKER"
- Run commands per component: `pm build backend`, `pm test mobile`

### Database migration awareness
- Detect migration tools: SQLx, Flyway, Liquibase, Prisma, Diesel, Alembic
- `pm migrate` — run pending migrations
- `pm migrate status` — show migration state

### Service orchestration
- For projects with Docker Compose, detect and list services
- `pm services` — show running/stopped services
- `pm logs <service>` — tail logs of a specific service
- Health check integration (`/health`, `/ready` endpoints)

### Team workflow support
- Detect number of contributors from git log
- `pm team` — show active contributors and their recent areas
- Branch naming convention detection and validation
- PR template detection

### Deployment awareness
- Detect deployment configs: `fly.toml`, `vercel.json`, `netlify.toml`, `railway.json`
- `pm deploy` — trigger deployment to detected platform
- `pm deploy status` — show deployment state

### Code generation detection
- Detect build_runner (Flutter/Dart), protobuf, OpenAPI generators
- `pm codegen` — run detected code generation tools
- Warn if generated files are outdated

---

## Contributing

Have an idea? Open an issue at [GitHub Issues](https://github.com/SoftDryzz/ProjectManager/issues) with the `enhancement` label.
