package dev.villagerhelper.mixin;
import dev.villagerhelper.VillagerAccess;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Villager.class) public interface VillagerInvoker extends VillagerAccess {
 @Invoker("updateTrades") void vh$updateTrades();
}
