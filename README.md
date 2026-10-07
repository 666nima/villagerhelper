# 村民助手 / Villager Helper 1.0.7

Minecraft **1.20.1** · Forge **47.4.6–47.x** · Java **17** · modId `villagerhelper`

## 中文
- 在原版交易列表提前生成并持久化各等级的真实交易，不是候选池；未解锁交易有等级锁与锁图标，服务端保护普通购买和 Shift 购买。升级解锁原有交易，不重复随机生成。
- 从未交易的村民可刷新：历史交易标记、经验或已用次数任一表明交易过即拒绝。刷新同步重建未来交易，不是补货。
- 快速补货仅重置 offer uses，不改商品、需求、价格、等级与经验，不清等级锁；服务器验证交易者、菜单、存活、8格距离与10 tick请求冷却。补货快捷键默认未绑定，在按键设置自行绑定，仅交易界面有效。
- 交易界面保持打开时允许原版约40 tick升级流程继续，并同步等级与锁态。
- **1.0.7恢复原版每日最多两次自动补货门槛**；工作站、工作AI及间隔沿用原版。手动快速补货独立于该自动次数门槛。
- **不再修改声望、折扣、需求项或受伤粒子**。这些可选玩法移至独立 `villagerreputation` 1.0.0；只装助手就是原版声望/价格规则。最低一个原付款物品由原版 getCostA 下限保留。
- 单人装于客户端实例（含集成服务器）；多人客户端和服务器装匹配助手版本。移除旧助手重复JAR，不要与1.0.6或更早助手同装。
- 捕获、释放村民、28槽贸易台、商品名/拼音搜索与聚合显示属于 **villagertradehub**，不是本模组功能。贸易台附近发现为扩展16格AABB，最多200条真实offer；聚合仅显示，不合并库存。

### 旧存档与可选声望模组
卸载旧助手或声望模组不会自动撤销已写入村民NBT的正面 gossip、需求、特殊价格与交易数据。新的声望事件和价格计算恢复原版，但已有正面声望仍可造成折扣；并非立即恢复原价或自动回滚。操作前备份存档，不自动清空 gossip 或改写玩家世界。保留助手可继续读取其未来交易/等级锁NBT；完全卸载助手后的预览锁语义不再受本模组保护。

## English
- Generates and persists actual offers for every level in the vanilla list. Future offers are server-locked, including normal and shift purchases, and unlock without generating duplicates.
- Rerolls only never-traded villagers: recorded trading history, XP or used offers reject rerolls. Quick restock resets uses only, preserving price/demand/items/XP/level/locks. Requests validate menu, trader, alive state, an 8-block distance and a 10-tick cooldown.
- The restock key is **unbound by default**, configurable in Controls and active only in the trading screen. Vanilla delayed promotion continues while the preview menu stays open.
- **1.0.7 restores vanilla's two automatic restocks per day** with vanilla work AI, workstation and timing. Manual quick restock is separate.
- Reputation, discount, demand and hurt-particle changes are removed and available separately in **villagerreputation 1.0.0**, which does not require this mod. Vanilla's minimum-one payment clamp is retained.
- Singleplayer includes the integrated server; multiplayer requires matching client/server helper installations. Remove old helper JARs; never co-install helper 1.0.6 or earlier.
- Capture/release, 28-slot storage, name/pinyin search and display-only offer grouping belong to **villagertradehub**, not this mod. Its nearby discovery uses a 16-block inflated AABB and a 200-offer cap; grouping does not merge inventories.
- Removing older versions or reputation does **not** erase persisted positive gossip, demand, special prices or offers. Vanilla calculations resume, but saved positive reputation may still yield discounts. Back up your save; there is no automatic rollback or gossip reset.

## Build / 构建
JDK17 + Python3 available as `python`; network required for initial dependencies. No sibling project or private launcher required.

Windows: `gradlew.bat --no-daemon clean build reobfJar`

Linux/macOS: `sh gradlew --no-daemon clean build reobfJar`

Output `build/libs/villagerhelper-1.0.7.jar`. `check` executes `policyCheck` request-policy assertions. `tools/convert_srg.py` is required for Mixin production mappings. Retained GameTests are isolated in `src/gameTest`; `compileGameTestJava` checks compilation only, not runtime execution.

## Verification boundaries / 验证边界
Production compilation/reobfuscation, request-policy tests and artifact/refmap checks are separate from gameplay. Isolated production mapping probes, when recorded, exit during mod construction before a client GUI/world is opened and prove target transformation only. No formal instance installation, client GUI validation, multiplayer acceptance or gameplay/save round-trip is claimed. Final in-game acceptance is left to the author.

## License / 许可
Original work Copyright (c) 2026 **666nima**, **BSD-3-Clause**. Gradle wrapper and Forge MDK notices retain their own copyrights/licenses; see `LICENSE`, `THIRD_PARTY_NOTICES.md`, `LICENSES`.
