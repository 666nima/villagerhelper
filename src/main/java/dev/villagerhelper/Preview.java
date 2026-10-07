package dev.villagerhelper;
import net.minecraft.world.entity.npc.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.trading.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
/** All actual offers live in the vanilla list; no separate preview model. */
public final class Preview {
 public static final String KEY="VillagerHelperAllLevels";
 public static final Set<UUID> GENERATING=new HashSet<>();
 public static CompoundTag data(Villager v){return v.getPersistentData().getCompound(KEY);}
 public static void clear(Villager v){v.getPersistentData().remove(KEY);v.getPersistentData().remove("VillagerHelperFuture");}
 public static void unlock(Villager v){if(v.level().isClientSide)return;for(MerchantOffer o:v.getOffers())((OfferAccess)o).vh$locked(((OfferAccess)o).vh$level()>v.getVillagerData().getLevel());}
 public static boolean consume(Villager v){if(GENERATING.contains(v.getUUID()) || !data(v).getBoolean("Complete"))return false;String profession=String.valueOf(ForgeRegistries.VILLAGER_PROFESSIONS.getKey(v.getVillagerData().getProfession()));if(!data(v).getString("Profession").equals(profession)){clear(v);return false;}unlock(v);return true;}
 public static void ensure(Villager v){
  if(v.level().isClientSide || GENERATING.contains(v.getUUID()))return;
  String profession=String.valueOf(ForgeRegistries.VILLAGER_PROFESSIONS.getKey(v.getVillagerData().getProfession()));
  if(data(v).getBoolean("Complete") && data(v).getString("Profession").equals(profession)){unlock(v);return;}
  VillagerData original=v.getVillagerData();MerchantOffers current=v.getOffers();
  // Existing offers (including trades in old saves) are never re-rolled or replaced.
  CompoundTag legacy=v.getPersistentData().getCompound("VillagerHelperFuture");
  GENERATING.add(v.getUUID());
  try{
   for(int level=original.getLevel()+1;level<=5;level++){
    MerchantOffers generated;
    if(legacy.getString("Profession").equals(profession) && legacy.contains(""+level))generated=new MerchantOffers(legacy.getCompound(""+level));
    else{v.setVillagerData(original.setLevel(level));v.setOffers(new MerchantOffers());((VillagerAccess)v).vh$updateTrades();generated=v.getOffers();}
    for(MerchantOffer o:generated){((OfferAccess)o).vh$level(level);((OfferAccess)o).vh$locked(true);current.add(o);}
   }
  }finally{v.setVillagerData(original);v.setOffers(current);GENERATING.remove(v.getUUID());}
  CompoundTag state=new CompoundTag();state.putString("Profession",profession);state.putBoolean("Complete",true);v.getPersistentData().put(KEY,state);v.getPersistentData().remove("VillagerHelperFuture");unlock(v);
 }
}
