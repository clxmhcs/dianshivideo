# 电视视频（重建精简版）

这是根据现有 APK 可观察到的产品形态重新搭建的 Android TV 工程。它不是反编译源码回填，也不包含原 APK 的品牌、图片、接口、动态插件、广告、账号、支付或统计代码。

## 已重建能力

- 电视首页：分区标题 + 横向内容卡片；
- 遥控器焦点：方向键移动、确认键进入播放；
- 全屏播放器：播放/暂停、从头播放、返回首页；
- 本地播放记录：自动保存并恢复单条媒体的进度；
- 本地目录：`app/src/main/assets/catalog.json`；
- 仅允许 HTTPS 媒体地址；目录内不接受 HTTP 地址；
- Android TV 启动入口，横屏显示，无触控硬件要求。

## 刻意删除的非必要模块

- 动态插件、热更新和运行时下载可执行代码；
- 广告、会员、支付、账户、扫码、社交、投屏、语音与埋点；
- 悬浮窗、安装应用、修改系统设置、开机广播、读写外部存储等权限；
- 多进程、远程服务、开放 ContentProvider 与导出 Service。

## 构建

1. 用 Android Studio 打开项目根目录；
2. 安装 Android SDK Platform 35；
3. 让 Android Studio 下载 Gradle 与 Android Gradle Plugin；
4. 运行 `app` 模块到 Android TV / Google TV / 电视盒子。

初版使用包名：`com.clxmhcs.dianshivideo`。

> 若旧 APK 的签名密钥已丢失，新包无法覆盖安装旧包。测试或迁移时需先卸载旧版，或在拥有原签名密钥时恢复原 applicationId。

## 正式接入内容前

- 替换 `catalog.json` 内的示例 URL，只保留你拥有分发权的 HTTPS 媒体；
- 将 `CatalogRepository` 改为受鉴权、受签名校验的 HTTPS 数据源；
- 为媒体播放加入超时、重试、埋点（仅必要且获得用户同意）与适配测试；
- 如需 DASH/HLS、多音轨、字幕、清晰度切换，建议再接入 AndroidX Media3，而不是恢复 APK 中的旧插件体系。
