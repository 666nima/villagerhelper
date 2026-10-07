package dev.villagerhelper.mixin;
import dev.villagerhelper.*;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Villager.class) public abstract class VillagerMixin {
 // Keep vanilla 40-tick promotion, but let it run while our all-level menu remains open.
 @Redirect(method="customServerAiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/npc/Villager;isTrading()Z",ordinal=0))
 private boolean vh$promotionWhileTrading(Villager v){return v.isTrading() && !Preview.data(v).getBoolean("Complete");}
 // Remove only the daily count read; the vanilla 2400-tick interval and work AI remain.
 @Redirect(method="allowedToRestock",at=@At(value="FIELD",target="Lnet/minecraft/world/entity/npc/Villager;numberOfRestocksToday:I",ordinal=1))
 private int vh$unlimitedDaily(Villager v){return 0;}
 @Inject(method={"rewardTradeXp","increaseMerchantCareer"},at=@At("RETURN"))
 private void vh$sync(CallbackInfo ci){Network.sync((Villager)(Object)this);}
 @Inject(method="updateTrades",at=@At("HEAD"),cancellable=true)
 private void vh$consume(CallbackInfo ci){if(Preview.consume((Villager)(Object)this))ci.cancel();}
 @Redirect(method="onReputationEventFrom",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/gossip/GossipContainer;add(Ljava/util/UUID;Lnet/minecraft/world/entity/ai/gossip/GossipType;I)V",ordinal=3))
 private void vh$hurt(net.minecraft.world.entity.ai.gossip.GossipContainer g,java.util.UUID id,net.minecraft.world.entity.ai.gossip.GossipType type,int amount){Villager v=(Villager)(Object)this;float ratio=v.getHealth()/v.getMaxHealth();g.add(id,ratio<0.5f?net.minecraft.world.entity.ai.gossip.GossipType.MAJOR_POSITIVE:type,ratio<0.5f?50-net.minecraft.util.Mth.floor(ratio*100):amount);}
 @ModifyArg(method="onReputationEventFrom",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/gossip/GossipContainer;add(Ljava/util/UUID;Lnet/minecraft/world/entity/ai/gossip/GossipType;I)V",ordinal=4),index=1)
 private net.minecraft.world.entity.ai.gossip.GossipType vh$death(net.minecraft.world.entity.ai.gossip.GossipType old){return net.minecraft.world.entity.ai.gossip.GossipType.MAJOR_POSITIVE;}
 @Inject(method="handleEntityEvent",at=@At("HEAD"),cancellable=true)
 private void vh$particles(byte event,CallbackInfo ci){Villager v=(Villager)(Object)this;if(event==13 && v.getHealth()/v.getMaxHealth()<0.5f)ci.cancel();}
 @ModifyArg(method="updateSpecialPrices",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/trading/MerchantOffer;addToSpecialPriceDiff(I)V"),index=0)
 private int vh$discount(int value){return value==Integer.MIN_VALUE?Integer.MIN_VALUE:-Math.abs(value);}
 @Inject(method="rewardTradeXp",at=@At("HEAD"))
 private void vh$traded(net.minecraft.world.item.trading.MerchantOffer offer,CallbackInfo ci){((Villager)(Object)this).getPersistentData().putBoolean("VHEverTraded",true);}
 @Inject(method="setVillagerData",at=@At("RETURN"))
 private void vh$level(net.minecraft.world.entity.npc.VillagerData data,CallbackInfo ci){Villager v=(Villager)(Object)this;if(!Preview.GENERATING.contains(v.getUUID()) && Preview.data(v).getBoolean("Complete"))Preview.unlock(v);}
 @Inject(method="setTradingPlayer",at=@At("HEAD"))
 private void vh$prepare(net.minecraft.world.entity.player.Player player,CallbackInfo ci){if(player!=null)Preview.ensure((Villager)(Object)this);}
}
