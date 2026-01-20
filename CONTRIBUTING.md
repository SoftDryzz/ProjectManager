# Contributing to ProjectManager

Thank you for your interest in contributing! 🎉

## 🐛 Reporting Bugs

If you find a bug, please [create a bug report](https://github.com/SoftDryzz/ProjectManager/issues/new/choose).

**Good bug reports include:**
- Clear title describing the issue
- Steps to reproduce the problem
- What you expected to happen
- What actually happened
- Your environment (OS, Java version, ProjectManager version)
- Error messages or logs

## ✨ Suggesting Features

Have an idea? [Create a feature request](https://github.com/SoftDryzz/ProjectManager/issues/new/choose).

**Good feature requests include:**
- Clear description of the feature
- Why it would be useful
- How it should work
- Examples of usage

## 🔧 Contributing Code

1. **Fork the repository**
2. **Create a feature branch:** `git checkout -b feature/amazing-feature`
3. **Make your changes**
4. **Test thoroughly**
5. **Commit:** `git commit -m 'feat: add amazing feature'`
6. **Push:** `git push origin feature/amazing-feature`
7. **Open a Pull Request**

### Code Style

- Follow existing code patterns
- Add JavaDoc comments for public methods
- Write clear commit messages
- Keep changes focused and atomic

### Commit Message Format
```
<type>: <description>

[optional body]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples:**
- `feat: add command aliases support`
- `fix: resolve path issues on Windows`
- `docs: update README with examples`

## 📋 Development Setup
```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/ProjectManager.git
cd ProjectManager

# Compile
mvn clean package

# Run tests
mvn test

# Install locally
.\scripts\install.ps1  # Windows
./scripts/install.sh   # Linux/Mac
```

## 🧪 Testing

Before submitting a PR:

- [ ] Code compiles without errors
- [ ] All existing tests pass
- [ ] New features have tests (when applicable)
- [ ] Documentation is updated
- [ ] Commits follow the format

## 📚 Documentation

When adding features:

- Update README.md (English)
- Update README_ES.md (Spanish)
- Update relevant User Guides if needed
- Add JavaDoc for public methods

## 🌍 Translations

We maintain documentation in English and Spanish. If you update documentation:

- Update both README.md and README_ES.md
- Keep translations consistent
- If unsure about Spanish translation, ask for help

## ❓ Questions?

- Check the [User Guide](docs/USER_GUIDE.md)
- Search [existing issues](https://github.com/SoftDryzz/ProjectManager/issues)
- Open a new issue with the "question" label

## 📜 Code of Conduct

- Be respectful and inclusive
- Welcome newcomers
- Focus on constructive feedback
- Keep discussions on-topic

## 🙏 Recognition

Contributors will be recognized in:
- Release notes
- Project README
- GitHub contributors page

---

**Thank you for contributing to ProjectManager! 🚀**
