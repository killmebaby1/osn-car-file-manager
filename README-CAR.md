# 车机文件管理（Material Files 车机改造版）

基于开源项目 [Material Files](https://github.com/zhanghai/MaterialFiles)（GPL-3.0）改造，面向领克等车机 Android 系统的文件管理器。

## 与原版的差异（车机改造）

1. **车机模式设置组**（设置 → 车机模式）
   - 车机模式总开关
   - 强制全屏：隐藏状态栏/导航栏（sticky 沉浸式，滑动可短暂唤出）
   - Dock 避让高度（0~200dp）：界面底部整体上移，避开常驻 Dock，FAB/底部操作栏/列表/抽屉全部生效，实时调整
2. **街机 ROM 快捷入口**：抽屉顶部「街机 ROM」直达（设置 → 车机模式 → 街机 ROM 目录可改），长按入口可打开设置
3. **触控优化**：触控目标 48→56dp，抽屉项加高至 56dp
4. **局域网传文件**：沿用内置 FTP 服务器，抽屉菜单「局域网传文件 (FTP)」入口
   - 用法：车机与手机连同一 WiFi → 车机打开「局域网传文件 (FTP)」→ 启动服务器，界面显示地址如 `ftp://192.168.x.x:2121` → 手机/电脑文件管理器访问即可传文件
5. **品牌**：应用名「车机文件管理」，包名 `com.carcast.carfilemanager`（可与原版共存）
6. **移除**：Firebase/崩溃统计等非自由组件已剔除

## 构建

GitHub Actions 自动构建：push 到 master/main 后，release `latest` 会更新为最新 debug 包，可直接下载：

```
https://github.com/<owner>/<repo>/releases/download/latest/app-debug.apk
```

本地构建需要 Android SDK（compileSdk 37 + NDK 30 + CMake），命令：`./gradlew assembleDebug`
