package dev.villagerhelper.mixin;
import dev.villagerhelper.Network;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerPlayer.class) public class PlayerOffersMixin {
 @Inject(method="sendMerchantOffers",at=@At("RETURN"))
 private void vh$levels(int menu,MerchantOffers offers,int level,int xp,boolean progress,boolean restock,CallbackInfo ci){Network.sendLevels((ServerPlayer)(Object)this,menu,offers);}
}
