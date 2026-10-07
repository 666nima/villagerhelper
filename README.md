# 村民助手 / Villager Helper 1.0.6

Minecraft **1.20.1**, Forge **47.4.6–47.x**, Java **17**. 原创代码 / Original code: **666nima**, **BSD-3-Clause**; third-party notices remain separate.

## 中文
- 在原版交易列表提前生成并持久化全等级真实交易；未解锁交易灰显并受服务端等级锁保护，升级后解锁。
- 未交易过的村民可刷新交易；快速补货恢复库存。补货快捷键默认未绑定，可在按键设置中绑定，仅交易界面生效。
- 低血量受伤相关好感与折扣调整；付款至少保留一个原付款物品。取消自动补货的每日次数门槛，但保留原版间隔和工作 AI。
- 单人游戏装在客户端（集成服务器也运行模组）；多人需服务器和客户端均装相同版本，不是纯客户端辅助。
- 将发行 JAR 放入对应实例的 `mods`，移除本模组旧版重复 JAR。不要把源码包当成模组安装。

## English
- Generates and persists actual trades for all villager levels in the vanilla list. Future trades remain greyed out and server-locked until their level is unlocked.
- Rerolls only villagers that have never traded; quick restock resets trade uses. The restock key is unbound by default and works only in the trading screen.
- Adjusts low-health reputation/discount behavior, keeps at least one original payment item, and removes the daily automatic-restock count limit while retaining vanilla timing/work AI.
- Install on the client for single-player (including the integrated server); multiplayer requires the same version on both client and server. Place the release JAR in `mods` and remove duplicate old versions.

## Build / 构建
Requires a JDK 17 on PATH (or JAVA_HOME), Python 3 available as `python`, and network access to Gradle/Forge/Maven dependencies. No sibling project, bundled JDK, private launcher, or local deployment script is required.

Windows: `gradlew.bat clean build reobfJar`

Linux/macOS: `sh gradlew clean build reobfJar`

Output: `build/libs/villagerhelper-1.0.6.jar`. `build` runs the retained request-policy assertions via `policyCheck`; `tools/convert_srg.py` is required for production Mixin mappings. GameTests and their template are isolated under `src/gameTest` and can be compiled separately with `gradlew.bat compileGameTestJava`; they are not packaged.

## Verification scope / 未测范围
Public-staging checks cover clean compilation, reobfuscation, request-policy checks and static release integrity. No game or GameTest was launched for this preparation. These checks do not prove final in-game UI, multiplayer transactions, persistence or compatibility with arbitrary modpacks/third-party professions. Those runtime areas require separate testing.
