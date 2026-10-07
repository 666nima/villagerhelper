package dev.villagerhelper.mixin;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Slot.class) public class ResultSlotMixin {
 @Shadow @Final public net.minecraft.world.Container container;
 @Inject(method="mayPickup",at=@At("HEAD"),cancellable=true)
 private void vh$pickup(Player player,CallbackInfoReturnable<Boolean> ci){if((Object)this instanceof MerchantResultSlot && container instanceof MerchantContainer c){var offer=c.getActiveOffer();if(offer==null || offer.isOutOfStock())ci.setReturnValue(false);}}
}
