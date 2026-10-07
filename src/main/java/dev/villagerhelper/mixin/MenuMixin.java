package dev.villagerhelper.mixin;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MerchantMenu.class) public class MenuMixin {
 @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
 private void vh$shift(Player player,int index,CallbackInfoReturnable<ItemStack> ci){if(index==2 && !((MerchantMenu)(Object)this).getSlot(2).mayPickup(player))ci.setReturnValue(ItemStack.EMPTY);}
}
