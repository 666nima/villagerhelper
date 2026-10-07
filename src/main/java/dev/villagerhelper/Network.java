package dev.villagerhelper;
import dev.villagerhelper.mixin.MenuAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;
public final class Network {
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("villagerhelper","main"),()->"1","1"::equals,"1"::equals);
 public record Request(int menu,int action){}
 public record Levels(int menu,int[] levels){}
 public static void sync(Villager v){
  if(v.getTradingPlayer() instanceof ServerPlayer s && s.containerMenu instanceof MerchantMenu m && ((MenuAccess)m).vh$trader()==v){
   s.sendMerchantOffers(m.containerId,v.getOffers(),v.getVillagerData().getLevel(),v.getVillagerXp(),true,v.canRestock());
   m.broadcastChanges();
  }
 }
 public static boolean applyLevels(MerchantMenu menu,Levels packet){
  if(menu.containerId!=packet.menu || menu.getOffers().size()!=packet.levels.length)return false;
  for(int level:packet.levels)if(level<1 || level>5)return false;
  for(int i=0;i<packet.levels.length;i++)((OfferAccess)menu.getOffers().get(i)).vh$level(packet.levels[i]);
  return true;
 }
 public static void sendLevels(ServerPlayer s,int menu,net.minecraft.world.item.trading.MerchantOffers offers){
  CHANNEL.send(PacketDistributor.PLAYER.with(()->s),new Levels(menu,offers.stream().mapToInt(o->((OfferAccess)o).vh$level()).toArray()));
 }
 public static void init(){
  CHANNEL.messageBuilder(Request.class,0,NetworkDirection.PLAY_TO_SERVER).encoder((p,b)->{b.writeVarInt(p.menu);b.writeVarInt(p.action);}).decoder(b->new Request(b.readVarInt(),b.readVarInt())).consumerMainThread(Network::handle).add();
  CHANNEL.messageBuilder(Levels.class,1,NetworkDirection.PLAY_TO_CLIENT).encoder((p,b)->{b.writeVarInt(p.menu);b.writeVarIntArray(p.levels);}).decoder(b->new Levels(b.readVarInt(),b.readVarIntArray(256))).consumerMainThread((p,c)->{ClientUI.levels(p);c.get().setPacketHandled(true);}).add();
 }
 public static void handle(Request p,Supplier<NetworkEvent.Context> ctx){ServerPlayer s=ctx.get().getSender();if(s!=null)apply(s,p);ctx.get().setPacketHandled(true);}
 public static boolean apply(ServerPlayer s,Request p){
  if(p.action<0 || p.action>1 || !(s.containerMenu instanceof MerchantMenu menu))return false;
  if(!(((MenuAccess)menu).vh$trader() instanceof Villager v))return false;
  long now=s.level().getGameTime();String key="VHRequest";long previous=s.getPersistentData().contains(key)?s.getPersistentData().getLong(key):now-10;
  if(!Policy.allowed(menu.containerId,p.menu,true,v.getTradingPlayer()==s,v.isAlive(),s.distanceToSqr(v),now,previous))return false;
  s.getPersistentData().putLong(key,now);
  if(p.action==0)Helpers.restock(v);
  if(p.action==1 && !Helpers.refresh(v))return false;
  Preview.ensure(v);
  ((MenuAccess)menu).vh$container().updateSellItem();
  s.sendMerchantOffers(menu.containerId,v.getOffers(),v.getVillagerData().getLevel(),v.getVillagerXp(),true,v.canRestock());menu.broadcastChanges();
  return true;
 }
 public static void request(int menu,int action){CHANNEL.sendToServer(new Request(menu,action));}
}
