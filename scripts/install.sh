@'
#!/bin/bash
# ProjectManager - Installation Script for Linux/Mac

echo "=== ProjectManager Installer ==="
echo ""

# 1. Verificar JAR
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="$SCRIPT_DIR/../target/projectmanager-1.0.0.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "❌ Error: JAR not found at $JAR_PATH"
    echo "Please run: mvn clean package"
    exit 1
fi

echo "✅ Found: $JAR_PATH"

# 2. Crear directorio bin
BIN_DIR="$HOME/bin"
mkdir -p "$BIN_DIR"
echo "✅ Directory: $BIN_DIR"

# 3. Crear script pm
PM_SCRIPT="$BIN_DIR/pm"
cat > "$PM_SCRIPT" << EOF
#!/bin/bash
java -jar "$JAR_PATH" "\$@"
EOF

chmod +x "$PM_SCRIPT"
echo "✅ Created: $PM_SCRIPT"

# 4. Agregar al PATH si no está
SHELL_RC=""
if [ -f "$HOME/.bashrc" ]; then
    SHELL_RC="$HOME/.bashrc"
elif [ -f "$HOME/.zshrc" ]; then
    SHELL_RC="$HOME/.zshrc"
fi

if [ -n "$SHELL_RC" ]; then
    if ! grep -q "export PATH=\"\$HOME/bin:\$PATH\"" "$SHELL_RC"; then
        echo "" >> "$SHELL_RC"
        echo "# ProjectManager" >> "$SHELL_RC"
        echo "export PATH=\"\$HOME/bin:\$PATH\"" >> "$SHELL_RC"
        echo "✅ Added to PATH in $SHELL_RC"
    else
        echo "ℹ️  Already in PATH"
    fi
fi

# 5. Verificar Java
echo ""
echo "Checking Java..."
if command -v java &> /dev/null; then
    java -version
    echo "✅ Java OK"
else
    echo "⚠️  WARNING: Java not found"
    echo "Install from: https://adoptium.net/"
fi

echo ""
echo "=== Installation Complete ==="
echo ""
echo "Restart your terminal, then run:"
echo "  pm help"
echo ""
'@ | Out-File -FilePath scripts\install.sh -Encoding UTF8 -NoNewline

Write-Host "✅ Creado: scripts\install.sh" -ForegroundColor Green