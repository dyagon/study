# OpenJDK 构建流程（简略）

## 前置条件

- **Boot JDK**：N-1 原则，编译 JDK 21 需 JDK 20 或 21
- **编译工具链**：完整 Xcode（非仅 Command Line Tools）
- **构建工具**：autoconf、make、zip、unzip、freetype、libpng
- **磁盘**：建议 SSD，至少 6GB
- **内存**：aarch64 建议 8GB+，x86 建议 4GB+

## 构建步骤

```bash
cd jdk

# macOS 推荐配置（含常见规避项）
bash configure \
    --with-debug-level=slowdebug \
    --with-freetype=system \
    --with-freetype-include=$(brew --prefix freetype)/include/freetype2 \
    --with-freetype-lib=$(brew --prefix freetype)/lib \
    --with-boot-jdk="$JAVA_HOME" \
    --with-zlib=system \
    --disable-warnings-as-errors

make images
```

## 输出位置

`build/<os>-<arch>-<variant>/images/jdk/`，如 `build/macosx-aarch64-server-slowdebug/images/jdk/bin/java`

## 常见问题与解决

| 问题 | 解决方法 |
|------|----------|
| **Metal 找不到** `XCode tool 'metal' neither found` | 需完整 Xcode；`sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`；或 `xcodebuild -downloadComponent metalToolchain` 下载缺失组件 |
| **Boot JDK 找不到**（用 SDKMAN 时） | 用 `--with-boot-jdk="$JAVA_HOME"`，不要用 `java_home -v 21`；变量用双引号让 zsh 展开 |
| **bash configure 拿不到 zsh 环境** | 在命令前加 `DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer"`，或先 `export` 再执行 |
| **Freetype：Only bundled can be specified** | 修改 `make/autoconf/lib-freetype.m4`，注释掉 Mac/Windows 的 bundled 检查，再用 `--with-freetype=system` + brew 路径 |
| **权限错误** `Permission denied` / `xattr` / `jmxremote.password` | `chmod -R u+w .` |
| **OS_CODE 宏重定义**（zlib） | 加 `--disable-warnings-as-errors` |
| **fp.h not found**（libpng） | 注释 `pngpriv.h` 中 `#include <fp.h>`，并添加 `#include <math.h>` |
| **frexp undeclared**（删 fp.h 后） | 在 `pngpriv.h` 添加 `#include <math.h>` |
| **VLA / 变长数组等 Clang 警告** | 加 `--disable-warnings-as-errors` |

## 参考

详细说明见 `jdk/doc/building.md`
