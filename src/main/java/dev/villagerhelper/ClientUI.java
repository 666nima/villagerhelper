package dev.villagerhelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="villagerhelper",value=Dist.CLIENT)
public final class ClientUI {
 public static void levels(Network.Levels p){
  var player=Minecraft.getInstance().player;
  if(player!=null && player.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu m && m.containerId==p.menu()){
   Network.applyLevels(m,p);
  }
 }
 public static void lock(net.minecraft.client.gui.GuiGraphics g,int x,int y){
  g.fill(x+2,y+4,x+9,y+9,0xff665532);g.fill(x+3,y+1,x+8,y+2,0xffbba46c);
  g.fill(x+2,y+2,x+4,y+5,0xffbba46c);g.fill(x+7,y+2,x+9,y+5,0xffbba46c);
  g.fill(x+5,y+5,x+6,y+8,0xffead9a2);
 }
 public static final KeyMapping RESTOCK=new KeyMapping("key.villagerhelper.restock",InputConstants.UNKNOWN.getValue(),"key.categories.villagerhelper");
 @Mod.EventBusSubscriber(modid="villagerhelper",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
 public static class Keys { @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(RESTOCK);} }
 @SubscribeEvent public static void init(ScreenEvent.Init.Post e){if(e.getScreen() instanceof MerchantScreen s){
  if(s.getTitle().getString().startsWith("贸易台")||s.getClass().getName().equals("dev.villagertradehub.HubTradeScreen"))return;
  int x=s.width/2-138,y=Math.max(2,s.height/2-110);
  e.addListener(new IconButton(x,y,false,b->Network.request(s.getMenu().containerId,0)));
  e.addListener(new IconButton(x+22,y,true,b->Network.request(s.getMenu().containerId,1)));
 }}
 static final class IconButton extends Button {
  private final boolean refresh;
  IconButton(int x,int y,boolean refresh,OnPress press){super(x,y,20,20,Component.literal(refresh?"刷新交易":"快速补货"),press,DEFAULT_NARRATION);this.refresh=refresh;setTooltip(net.minecraft.client.gui.components.Tooltip.create(getMessage()));}
  @Override public void renderString(net.minecraft.client.gui.GuiGraphics g,net.minecraft.client.gui.Font font,int color){
   int x=getX()+4,y=getY()+4;
   if(refresh){
    int ink=active?0xffe8e8dd:0xff777777;
    g.fill(x+3,y+1,x+9,y+3,ink);g.fill(x+1,y+3,x+3,y+8,ink);g.fill(x+3,y+9,x+9,y+11,ink);g.fill(x+9,y+4,x+11,y+9,ink);
    g.fill(x+8,y,x+10,y+5,ink);g.fill(x+6,y+3,x+10,y+5,ink);g.fill(x+2,y+7,x+6,y+9,ink);g.fill(x+2,y+7,x+4,y+12,ink);
   }else{
    g.fill(x+1,y+3,x+11,y+11,0xff62472d);g.fill(x+2,y+4,x+10,y+10,0xffb88a51);g.fill(x+1,y+2,x+11,y+4,0xffddad6a);g.fill(x+5,y+3,x+7,y+11,0xff775735);
   }
  }
 }
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END)while(RESTOCK.consumeClick())if(Minecraft.getInstance().screen instanceof MerchantScreen s)Network.request(s.getMenu().containerId,0);}
}
