package com.hollingsworth.arsnouveau.client;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.client.ITooltipProvider;
import com.hollingsworth.arsnouveau.api.util.StackUtil;
import com.hollingsworth.arsnouveau.client.gui.GuiManaHUD;
import com.hollingsworth.arsnouveau.client.gui.GuiSpellHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


/**
 * Renders this mod's HUDs.
 *
 * @author Choonster
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ArsNouveau.MODID)
public class HUDEventHandler {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final GuiSpellHUD spellHUD = new GuiSpellHUD();
    private static final GuiManaHUD manaHUD = new GuiManaHUD();
    private static final GuiEntityInfoHUD entityHUD = new GuiEntityInfoHUD();
    /**
     * Render the current spell when the SpellBook is held in the players hand
     *
     * @param event The event
     */
    @SubscribeEvent
    public static void renderSpellHUD(final RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        final PlayerEntity player = minecraft.player;

        if ((StackUtil.getHeldSpellbook(player) == ItemStack.EMPTY))
            return;

        spellHUD.drawHUD();
        manaHUD.drawHUD();

    }

    /**
     *
     *
     * @param event The event
     */
    @SubscribeEvent
    public static void renderEntityHUD(final RenderGameOverlayEvent.Post event) {
        RayTraceResult mouseOver = Minecraft.getInstance().objectMouseOver;
        if (mouseOver != null && mouseOver.getType() == RayTraceResult.Type.ENTITY) {

          EntityRayTraceResult result = (EntityRayTraceResult) mouseOver;
          if(result.getEntity() instanceof ITooltipProvider)
              entityHUD.drawHUD(((ITooltipProvider) result.getEntity()).getTooltip());
        }
        if (mouseOver != null && mouseOver.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult result = (BlockRayTraceResult) mouseOver;
            BlockPos pos = result.getPos();
            if(Minecraft.getInstance().world != null && Minecraft.getInstance().world.getTileEntity(pos) instanceof ITooltipProvider){
                entityHUD.drawHUD(((ITooltipProvider) Minecraft.getInstance().world.getTileEntity(pos)).getTooltip());
            }
        }
    }
}