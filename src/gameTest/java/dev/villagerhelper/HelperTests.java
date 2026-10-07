package dev.villagerhelper;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
@GameTestHolder("villagerhelper") @PrefixGameTestTemplate(false)
public class HelperTests {
 static class TradingVillager extends Villager {
  TradingVillager(net.minecraft.server.level.ServerLevel level){super(EntityType.VILLAGER,level);}
  void serverStep(){super.customServerAiStep();}
 }
 @GameTest(template="empty") public static void realTradingOpenMenuUpgrade(GameTestHelper h){realTrading(h,true);}
 @GameTest(template="empty") public static void realTradingClosedMenuUpgrade(GameTestHelper h){realTrading(h,false);}
 private static void realTrading(GameTestHelper h,boolean keepOpen){
  TradingVillager v=new TradingVillager(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));Preview.ensure(v);
  var p=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());v.setPos(p.getX(),p.getY(),p.getZ());v.setTradingPlayer(p);
  var m=new net.minecraft.world.inventory.MerchantMenu(61,p.getInventory(),v);p.containerMenu=m;int count=v.getOffers().size();
  for(int level=1;level<5;level++){
   MerchantOffer o=v.getOffers().stream().filter(x->((OfferAccess)x).vh$level()==v.getVillagerData().getLevel() && x.getXp()>0).findFirst().orElseThrow();o.setSpecialPriceDiff(-10000);
   int index=v.getOffers().indexOf(o);m.setSelectionHint(index);var c=((dev.villagerhelper.mixin.MenuAccess)m).vh$container();
   while(v.getVillagerXp()<VillagerData.getMaxXpPerLevel(level)){
    Helpers.restock(v);c.setItem(0,o.getCostA().copy());c.setItem(1,o.getCostB().copy());c.updateSellItem();
    h.assertTrue(m.getSlot(2).mayPickup(p),"real result pickup denied");int before=v.getVillagerXp();var result=m.getSlot(2).remove(64);h.assertTrue(!result.isEmpty(),"real result absent");m.getSlot(2).onTake(p,result);
    h.assertTrue(v.getVillagerXp()==before+o.getXp(),"real transaction lost or doubled XP");h.assertTrue(o.getUses()==1,"notifyTrade did not increment uses");
   }
   if(!keepOpen)v.setTradingPlayer(null);
   for(int tick=0;tick<41;tick++)v.serverStep();
   h.assertTrue(v.getVillagerData().getLevel()==level+1,"real XP reached threshold but natural upgrade stalled (open="+keepOpen+") at level "+level);
   h.assertTrue(v.getOffers().size()==count,"natural upgrade duplicated offers");
   int unlocked=level+1;h.assertTrue(v.getOffers().stream().filter(x->((OfferAccess)x).vh$level()==unlocked).noneMatch(MerchantOffer::isOutOfStock),"natural level did not unlock offers");
   v.setTradingPlayer(p);
  }
  System.out.println("REAL_TRADE_PASS open="+keepOpen+" level="+v.getVillagerData().getLevel()+" xp="+v.getVillagerXp());v.setTradingPlayer(null);h.succeed();
 }
 @GameTest(template="empty") public static void automaticDailyRestockLimit(GameTestHelper h){
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));Preview.ensure(v);
  MerchantOffer o=v.getOffers().get(0);MerchantOffer locked=v.getOffers().stream().filter(x->((OfferAccess)x).vh$level()==2).findFirst().orElseThrow();
  for(int cycle=0;cycle<2;cycle++){
   for(int i=0;i<o.getMaxUses();i++)o.increaseUses();
   var state=new net.minecraft.nbt.CompoundTag();v.saveWithoutId(state);state.putLong("LastRestock",h.getLevel().getGameTime()-2401);state.putInt("RestocksToday",cycle);v.readAdditionalSaveData(state);o=v.getOffers().get(0);
   h.assertTrue(v.shouldRestock(),"normal automatic restock denied at cycle "+(cycle+1));v.restock();h.assertTrue(o.getUses()==0,"automatic restock did not reset stock");
   h.assertTrue(v.getOffers().stream().filter(x->((OfferAccess)x).vh$level()==2).allMatch(MerchantOffer::isOutOfStock),"automatic restock unlocked future");
   h.assertTrue(!v.shouldRestock(),"restock cooldown bypassed");
  }
  var daily=new net.minecraft.nbt.CompoundTag();v.saveWithoutId(daily);daily.putLong("LastRestock",h.getLevel().getGameTime()-2401);daily.putInt("RestocksToday",2);v.readAdditionalSaveData(daily);v.getOffers().get(0).increaseUses();h.assertTrue(!v.shouldRestock(),"third automatic restock accepted");
  h.succeed();
 }
 @GameTest(template="empty") public static void repeatedPacketIndexState(GameTestHelper h){
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));Preview.ensure(v);
  var p=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());var menu=new net.minecraft.world.inventory.MerchantMenu(73,p.getInventory(),v);
  for(int cycle=0;cycle<12;cycle++){
   if(cycle%3==0)h.assertTrue(Helpers.refresh(v),"untraded refresh denied");Helpers.restock(v);Preview.ensure(v);
   var original=v.getOffers();int[] levels=original.stream().mapToInt(o->((OfferAccess)o).vh$level()).toArray();
   var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
   var packet=new net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket(73,original,1,v.getVillagerXp(),true,true);packet.write(b);
   var copy=new net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket(b);b.release();menu.setOffers(copy.getOffers());
   h.assertTrue(Network.applyLevels(menu,new Network.Levels(73,levels)),"metadata rejected");
   h.assertTrue(!Network.applyLevels(menu,new Network.Levels(72,levels)),"stale menu metadata accepted");h.assertTrue(!Network.applyLevels(menu,new Network.Levels(73,new int[0])),"mismatched metadata accepted");
   for(int i=0;i<original.size();i++){
    var actual=menu.getOffers().get(i);var source=original.get(i);
    h.assertTrue(ItemStack.matches(actual.getResult(),source.getResult()),"packet item index drift");
    h.assertTrue(((OfferAccess)actual).vh$level()==levels[i],"icon required level drift");
    h.assertTrue(actual.isOutOfStock()==source.isOutOfStock(),"vanilla packet lock stock drift");
   }
  }
  h.succeed();
 }
 @GameTest(template="empty") public static void manualRestockNoDailyLimit(GameTestHelper h){
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));Preview.ensure(v);
  for(int i=0;i<10;i++){v.getOffers().get(0).increaseUses();Helpers.restock(v);h.assertTrue(v.getOffers().get(0).getUses()==0,"manual daily limit");}
  var state=new net.minecraft.nbt.CompoundTag();v.saveWithoutId(state);h.assertTrue(state.getInt("RestocksToday")==0,"manual restock counted as automatic");
  h.assertTrue(v.getOffers().stream().filter(o->((OfferAccess)o).vh$level()>1).allMatch(MerchantOffer::isOutOfStock),"manual restock cleared locks");h.succeed();
 }
 @GameTest(template="empty") public static void rapidSwitchDiscountedAndPaidShift(GameTestHelper h){
  TradingVillager v=new TradingVillager(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));
  var paid=new MerchantOffer(new ItemStack(Items.EMERALD,4),new ItemStack(Items.IRON_SHOVEL),100,1,0f);
  var discounted=new MerchantOffer(new ItemStack(Items.EMERALD),new ItemStack(Items.BREAD),100,1,0f);discounted.setSpecialPriceDiff(-10);
  var locked=new MerchantOffer(new ItemStack(Items.EMERALD),new ItemStack(Items.DIAMOND),100,1,0f);((OfferAccess)locked).vh$level(2);((OfferAccess)locked).vh$locked(true);
  v.getOffers().clear();v.getOffers().add(paid);v.getOffers().add(discounted);v.getOffers().add(locked);
  var p=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());p.getInventory().clearContent();var m=new net.minecraft.world.inventory.MerchantMenu(81,p.getInventory(),v);p.containerMenu=m;
  var c=((dev.villagerhelper.mixin.MenuAccess)m).vh$container();
  for(int i=0;i<12;i++){
   m.setSelectionHint(1);c.setItem(0,ItemStack.EMPTY);c.setItem(1,ItemStack.EMPTY);c.updateSellItem();
   h.assertTrue(c.getItem(2).isEmpty() && m.quickMoveStack(p,2).isEmpty(),"discounted trade accepted empty payment");
   c.setItem(0,new ItemStack(Items.EMERALD));c.updateSellItem();
   h.assertTrue(c.getActiveOffer()==discounted && c.getItem(2).is(Items.BREAD),"discounted paid output mismatched");
   int discountedUses=discounted.getUses();h.assertTrue(!m.quickMoveStack(p,2).isEmpty() && c.getItem(0).isEmpty() && discounted.getUses()==discountedUses+1,"discounted shift failed to consume one payment");
   m.setSelectionHint(0);c.updateSellItem();h.assertTrue(c.getItem(2).isEmpty(),"paid shovel became discounted after fast switching");
   c.setItem(0,new ItemStack(Items.EMERALD,4));c.updateSellItem();h.assertTrue(c.getActiveOffer()==paid && c.getItem(2).is(Items.IRON_SHOVEL),"paid selected output mismatched");
   int xp=v.getVillagerXp(),uses=paid.getUses();h.assertTrue(!m.quickMoveStack(p,2).isEmpty(),"paid shift failed");h.assertTrue(c.getItem(0).isEmpty() && paid.getUses()==uses+1 && v.getVillagerXp()==xp+1,"paid shift payment/uses/XP mismatch");
   m.setSelectionHint(2);c.setItem(0,new ItemStack(Items.EMERALD));c.updateSellItem();h.assertTrue(c.getItem(2).isEmpty() && m.quickMoveStack(p,2).isEmpty(),"switching bypassed locked trade");
   p.getInventory().clearContent();
  }
  h.assertTrue(paid.getCostA().getCount()==4 && discounted.getCostA().getCount()==1,"discount minimum state changed");h.succeed();
 }
 @GameTest(template="empty") public static void networkValidation(GameTestHelper h) {
  var p=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setPos(p.getX(),p.getY(),p.getZ());v.setTradingPlayer(p);
  var m=new net.minecraft.world.inventory.MerchantMenu(31,p.getInventory(),v);p.containerMenu=m;
  MerchantOffer o=new MerchantOffer(new ItemStack(Items.EMERALD),new ItemStack(Items.BREAD),3,1,0.05f);v.getOffers().clear();v.getOffers().add(o);o.increaseUses();
  h.assertTrue(!Network.apply(p,new Network.Request(32,0)),"forged menu accepted");
  v.setTradingPlayer(null);h.assertTrue(!Network.apply(p,new Network.Request(31,0)),"wrong trading player accepted");
  v.setTradingPlayer(p);v.setPos(p.getX()+20,p.getY(),p.getZ());h.assertTrue(!Network.apply(p,new Network.Request(31,0)),"far request accepted");
  v.setPos(p.getX(),p.getY(),p.getZ());h.assertTrue(!Network.apply(p,new Network.Request(31,9)),"unknown action accepted");
  h.assertTrue(o.getUses()==1,"rejected request mutated offers");h.succeed();
 }

 @GameTest(template="empty") public static void lockedPurchasePaths(GameTestHelper h) {
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));Preview.ensure(v);
  MerchantOffer o=v.getOffers().stream().filter(x->((OfferAccess)x).vh$level()==2).findFirst().orElseThrow();
  o.setSpecialPriceDiff(-1000);h.assertTrue(!o.satisfiedBy(o.getCostA().copy(),o.getCostB().copy()) && !o.take(o.getCostA().copy(),o.getCostB().copy()),"discounted paid lock bypass");
  Helpers.restock(v);h.assertTrue(o.isOutOfStock(),"restock unlocked future");
  var p=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());v.setTradingPlayer(p);
  var m=new net.minecraft.world.inventory.MerchantMenu(42,p.getInventory(),v);var c=((dev.villagerhelper.mixin.MenuAccess)m).vh$container();int index=v.getOffers().indexOf(o);
  m.setSelectionHint(index);c.updateSellItem();h.assertTrue(c.getItem(2).isEmpty(),"locked result populated");
  v.setVillagerData(v.getVillagerData().setLevel(2));c.setItem(0,o.getCostA().copy());c.setItem(1,o.getCostB().copy());c.updateSellItem();h.assertTrue(!c.getItem(2).isEmpty(),"unlocked paid result absent");
  v.setVillagerData(v.getVillagerData().setLevel(1));
  h.assertTrue(!m.getSlot(2).mayPickup(p),"stale result ordinary pickup accepted");
  h.assertTrue(m.quickMoveStack(p,2).isEmpty(),"stale result shift accepted");h.succeed();
 }
 @GameTest(template="empty") public static void existingSavePreserved(GameTestHelper h) {
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.LIBRARIAN).setLevel(3));
  MerchantOffer o=new MerchantOffer(new ItemStack(Items.EMERALD,7),new ItemStack(Items.DIAMOND),4,2,0.1f);o.increaseUses();v.getOffers().clear();v.getOffers().add(o);v.setVillagerXp(30);String before=o.createTag().toString();
  Preview.ensure(v);h.assertTrue(v.getOffers().get(0)==o && before.equals(o.createTag().toString()),"existing offer changed");
  h.assertTrue(!Helpers.refresh(v),"traded save refreshed");
  var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());v.getOffers().writeToStream(b);var copy=MerchantOffers.createFromStream(b);
  h.assertTrue(copy.size()==v.getOffers().size() && copy.get(1).isOutOfStock(),"vanilla packet lost locked stock");b.release();h.succeed();
 }
 @GameTest(template="empty") public static void professionChange(GameTestHelper h){
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));Preview.ensure(v);
  v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.LIBRARIAN));((VillagerAccess)v).vh$updateTrades();
  h.assertTrue(!v.getOffers().isEmpty(),"profession change blocked regeneration");h.succeed();
 }
 @GameTest(template="empty") public static void allLevelsLocked(GameTestHelper h) {
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));
  Preview.ensure(v);
  h.assertTrue(v.getOffers().size()>=10,"future trades absent from vanilla offers");
  h.assertTrue(v.getOffers().stream().filter(MerchantOffer::isOutOfStock).count()>=8,"future trades unlocked");h.succeed();
 }
 @GameTest(template="empty") public static void previewPersistence(GameTestHelper h) {
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.LIBRARIAN));
  Preview.ensure(v);String before=Preview.data(v).toString();
  h.assertTrue(!Preview.data(v).isEmpty(),"future missing");
  net.minecraft.nbt.CompoundTag saved=new net.minecraft.nbt.CompoundTag();v.saveWithoutId(saved);
  Villager copy=EntityType.VILLAGER.create(h.getLevel());copy.load(saved);
  h.assertTrue(Preview.data(copy).toString().equals(before),"future persistence differs");
  String offers=v.getOffers().createTag().toString();
  h.assertTrue(copy.getOffers().createTag().toString().equals(offers),"offer level NBT lost");
  int count=v.getOffers().size();v.setVillagerData(v.getVillagerData().setLevel(2));((VillagerAccess)v).vh$updateTrades();
  h.assertTrue(v.getOffers().size()==count,"upgrade duplicated offers");
  h.assertTrue(v.getOffers().stream().filter(o->((OfferAccess)o).vh$level()==2).noneMatch(MerchantOffer::isOutOfStock),"level 2 still locked");
  v.setVillagerData(v.getVillagerData().setLevel(1));v.getOffers().clear();
  h.assertTrue(Helpers.refresh(v),"untraded refresh denied");
  h.assertTrue(!Preview.data(v).isEmpty(),"refresh failed to rebuild future");
  v.setVillagerXp(1);h.assertTrue(!Helpers.refresh(v),"traded refresh permitted");
  v.setVillagerXp(0);v.getPersistentData().putBoolean("VHEverTraded",true);Helpers.restock(v);h.assertTrue(!Helpers.refresh(v),"restock erased trading history");h.succeed();
 }

 @GameTest(template="empty") public static void discountedMinimumPayment(GameTestHelper h) {
  MerchantOffer o=new MerchantOffer(new ItemStack(Items.EMERALD,7),new ItemStack(Items.BREAD),3,1,0.05f);o.setSpecialPriceDiff(-10000);
  h.assertTrue(o.getCostA().is(Items.EMERALD) && o.getCostA().getCount()==1,"large discount must retain one payment item");
  h.assertTrue(!o.satisfiedBy(ItemStack.EMPTY,ItemStack.EMPTY) && !o.take(ItemStack.EMPTY,ItemStack.EMPTY),"empty payment accepted");
  ItemStack payment=new ItemStack(Items.EMERALD,2);
  h.assertTrue(o.satisfiedBy(payment,ItemStack.EMPTY) && o.take(payment,ItemStack.EMPTY) && payment.getCount()==1,"one-item payment not matched/consumed");
  var saved=new MerchantOffers();saved.add(o);var reloaded=new MerchantOffers(saved.createTag());var old=reloaded.get(0);
  h.assertTrue(old.getSpecialPriceDiff()==-10000 && old.getCostA().is(Items.EMERALD) && old.getCostA().getCount()==1,"existing offers NBT reload lost minimum or discount");
  Villager v=EntityType.VILLAGER.create(h.getLevel());v.getOffers().clear();v.getOffers().add(old);
  var c=new net.minecraft.world.inventory.MerchantContainer(v);c.setSelectionHint(0);c.updateSellItem();
  h.assertTrue(c.getItem(2).isEmpty(),"discounted result displayed without payment");
  c.setItem(0,new ItemStack(Items.EMERALD));c.updateSellItem();h.assertTrue(c.getItem(2).is(Items.BREAD),"paid discounted result absent");
  h.assertTrue(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount()==0,"EMPTY corrupted");h.succeed();
 }
 @GameTest(template="empty") public static void restock(GameTestHelper h) {
  Villager v=EntityType.VILLAGER.create(h.getLevel());
  v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));
  MerchantOffer o=new MerchantOffer(new ItemStack(Items.EMERALD,5),new ItemStack(Items.BREAD),2,3,0.05f);
  v.getOffers().clear();v.getOffers().add(o);o.increaseUses();o.increaseUses();o.setSpecialPriceDiff(-2);
  int price=o.getCostA().getCount(), xp=v.getVillagerXp(), demand=o.getDemand();
  Helpers.restock(v);
  h.assertTrue(o.getUses()==0,"uses not reset");
  h.assertTrue(o.getCostA().getCount()==price && v.getVillagerXp()==xp && o.getDemand()==demand,"restock changed price/xp/demand");h.succeed();
 }
}
