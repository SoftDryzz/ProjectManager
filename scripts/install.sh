#!/bin/bash
# ProjectManager - Installation Script for Linux/Mac
# Works both from source (mvn clean package) and from GitHub Release download

JAR_NAME="projectmanager-1.4.0.jar"

echo "=== ProjectManager Installer ==="
echo ""

# 1. Search for JAR in multiple locations
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

JAR_PATH=""
SEARCH_PATHS=(
    "$SCRIPT_DIR/../target/$JAR_NAME"       # Built from source (mvn clean package)
    "$SCRIPT_DIR/../$JAR_NAME"              # JAR placed in project root
    "$SCRIPT_DIR/$JAR_NAME"                 # JAR placed next to script
    "$SCRIPT_DIR/../../$JAR_NAME"           # One level up (nested ZIP extraction)
)

for path in "${SEARCH_PATHS[@]}"; do
    if [ -f "$path" ]; then
        JAR_PATH="$(cd "$(dirname "$path")" && pwd)/$(basename "$path")"
        break
    fi
done

# Also search by pattern in case version differs
if [ -z "$JAR_PATH" ]; then
    for dir in "$SCRIPT_DIR/../target" "$SCRIPT_DIR/.." "$SCRIPT_DIR"; do
        if [ -d "$dir" ]; then
            found=$(find "$dir" -maxdepth 1 -name "projectmanager-*.jar" ! -name "*-javadoc*" ! -name "original-*" 2>/dev/null | head -1)
            if [ -n "$found" ]; then
                JAR_PATH="$(cd "$(dirname "$found")" && pwd)/$(basename "$found")"
                break
            fi
        fi
    done
fi

if [ -z "$JAR_PATH" ]; then
    echo "❌ Error: JAR not found ($JAR_NAME)"
    echo ""
    echo "Searched in:"
    for path in "${SEARCH_PATHS[@]}"; do
        echo "  - $path"
    done
    echo ""
    echo "Options:"
    echo "  1. Download the JAR from the GitHub Release page and place it next to this script"
    echo "     https://github.com/SoftDryzz/ProjectManager/releases/latest"
    echo "  2. Build from source: mvn clean package"
    exit 1
fi

echo "✅ Found: $JAR_PATH"

# 2. Copy JAR to a permanent location
INSTALL_DIR="$HOME/.projectmanager"
mkdir -p "$INSTALL_DIR"

INSTALLED_JAR="$INSTALL_DIR/projectmanager.jar"
cp "$JAR_PATH" "$INSTALLED_JAR"
echo "✅ Installed: $INSTALLED_JAR"

# 3. Create bin directory
BIN_DIR="$HOME/bin"
mkdir -p "$BIN_DIR"
echo "✅ Directory: $BIN_DIR"

# 4. Create pm script pointing to permanent location (with auto-update swap)
PM_SCRIPT="$BIN_DIR/pm"
cat > "$PM_SCRIPT" << EOF
#!/bin/bash
PM_JAR="$INSTALLED_JAR"
PM_NEW="\${PM_JAR}.new"
if [ -f "\$PM_NEW" ]; then
    mv -f "\$PM_NEW" "\$PM_JAR"
fi
java -jar "\$PM_JAR" "\$@"
EOF

chmod +x "$PM_SCRIPT"
echo "✅ Created: $PM_SCRIPT"

# 5. Add to PATH if not already there
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

# 6. Verify Java
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
