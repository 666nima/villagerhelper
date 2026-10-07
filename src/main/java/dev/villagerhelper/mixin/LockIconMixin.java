package dev.villagerhelper.mixin;
import dev.villagerhelper.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Only replaces the 10x9 sold-out arrow for level-locked rows. Vanilla renders everything else. */
@Mixin(MerchantScreen.class) public class LockIconMixin {
 @Shadow private int scrollOff;
 @Inject(method="renderButtonArrows",at=@At("HEAD"),cancellable=true)
 private void vh$icon(GuiGraphics g,MerchantOffer offer,int x,int y,CallbackInfo ci){
  var s=(MerchantScreen)(Object)this;
  if(((OfferAccess)offer).vh$level()>s.getMenu().getTraderLevel()){
   ClientUI.lock(g,x+60,y+3);ci.cancel();
  }
 }
 @Inject(method="render",at=@At("TAIL"))
 private void vh$tooltip(GuiGraphics g,int mouseX,int mouseY,float partial,CallbackInfo ci){
  var s=(MerchantScreen)(Object)this;int left=(s.width-276)/2,top=(s.height-166)/2;
  int row=(mouseY-top-17)/20,index=scrollOff+row;
  if(mouseX<left+60 || mouseX>=left+70 || mouseY<top+17 || row<0 || row>=7 || index>=s.getMenu().getOffers().size())return;
  var o=s.getMenu().getOffers().get(index);int required=((OfferAccess)o).vh$level();
  if(required>s.getMenu().getTraderLevel())g.renderTooltip(net.minecraft.client.Minecraft.getInstance().font,Component.literal("需要村民等级 "+required),mouseX,mouseY);
 }
}
