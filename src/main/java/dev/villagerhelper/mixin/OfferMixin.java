package dev.villagerhelper.mixin;
import dev.villagerhelper.OfferAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(MerchantOffer.class) public class OfferMixin implements OfferAccess {
 @Unique private int vh$requiredLevel=1;
 @Unique private boolean vh$locked;
 public int vh$level(){return vh$requiredLevel;}
 public void vh$level(int level){vh$requiredLevel=Math.max(1,Math.min(5,level));}
 public boolean vh$locked(){return vh$locked;}
 public void vh$locked(boolean locked){vh$locked=locked;}
 @Inject(method="<init>(Lnet/minecraft/nbt/CompoundTag;)V",at=@At("RETURN"))
 private void vh$load(CompoundTag tag,CallbackInfo ci){vh$level(tag.contains("VHLevel")?tag.getInt("VHLevel"):1);vh$locked=tag.getBoolean("VHLocked");}
 @Inject(method="createTag",at=@At("RETURN"))
 private void vh$save(CallbackInfoReturnable<CompoundTag> ci){ci.getReturnValue().putInt("VHLevel",vh$requiredLevel);ci.getReturnValue().putBoolean("VHLocked",vh$locked);}
 @Inject(method="isOutOfStock",at=@At("HEAD"),cancellable=true)
 private void vh$stock(CallbackInfoReturnable<Boolean> ci){if(vh$locked)ci.setReturnValue(true);}
 @Inject(method={"satisfiedBy","take"},at=@At("HEAD"),cancellable=true)
 private void vh$match(ItemStack a,ItemStack b,CallbackInfoReturnable<Boolean> ci){if(vh$locked)ci.setReturnValue(false);}
 @Redirect(method="getCostA",at=@At(value="INVOKE",target="Ljava/lang/Math;max(II)I"))
 private int vh$demand(int minimum,int demand){return demand;}
}
