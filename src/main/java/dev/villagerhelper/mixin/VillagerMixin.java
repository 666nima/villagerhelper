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
 @Inject(method={"rewardTradeXp","increaseMerchantCareer"},at=@At("RETURN"))
 private void vh$sync(CallbackInfo ci){Network.sync((Villager)(Object)this);}
 @Inject(method="updateTrades",at=@At("HEAD"),cancellable=true)
 private void vh$consume(CallbackInfo ci){if(Preview.consume((Villager)(Object)this))ci.cancel();}
 @Inject(method="rewardTradeXp",at=@At("HEAD"))
 private void vh$traded(net.minecraft.world.item.trading.MerchantOffer offer,CallbackInfo ci){((Villager)(Object)this).getPersistentData().putBoolean("VHEverTraded",true);}
 @Inject(method="setVillagerData",at=@At("RETURN"))
 private void vh$level(net.minecraft.world.entity.npc.VillagerData data,CallbackInfo ci){Villager v=(Villager)(Object)this;if(!Preview.GENERATING.contains(v.getUUID()) && Preview.data(v).getBoolean("Complete"))Preview.unlock(v);}
 @Inject(method="setTradingPlayer",at=@At("HEAD"))
 private void vh$prepare(net.minecraft.world.entity.player.Player player,CallbackInfo ci){if(player!=null)Preview.ensure((Villager)(Object)this);}
}
