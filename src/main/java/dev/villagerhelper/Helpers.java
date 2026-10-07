package dev.villagerhelper;
import net.minecraft.world.entity.npc.Villager;
public final class Helpers { public static boolean refresh(Villager v) { if(v.getPersistentData().getBoolean("VHEverTraded") || v.getVillagerXp()!=0 || v.getOffers().stream().anyMatch(o -> o.getUses()>0))return false; Preview.clear(v);v.getOffers().clear();((VillagerAccess)v).vh$updateTrades();Preview.ensure(v);return true; } public static void restock(Villager v) { v.getOffers().forEach(o -> o.resetUses()); } }
