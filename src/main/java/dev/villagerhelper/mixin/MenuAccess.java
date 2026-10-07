package dev.villagerhelper.mixin;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(MerchantMenu.class) public interface MenuAccess {
 @Accessor("trader") Merchant vh$trader();
 @Accessor("tradeContainer") MerchantContainer vh$container();
}
