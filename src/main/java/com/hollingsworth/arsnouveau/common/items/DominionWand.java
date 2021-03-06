package com.hollingsworth.arsnouveau.common.items;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.common.PortUtil;
import com.hollingsworth.arsnouveau.common.lib.LibItemNames;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class DominionWand extends ModItem{
    public DominionWand() {
        super(LibItemNames.DOMINION_WAND);
    }

    @Override
    public void inventoryTick(ItemStack stack, World p_77663_2_, Entity p_77663_3_, int p_77663_4_, boolean p_77663_5_) {
        super.inventoryTick(stack, p_77663_2_, p_77663_3_, p_77663_4_, p_77663_5_);
        if(!stack.hasTag())
            stack.setTag(new CompoundNBT());
    }
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return false;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack doNotUseStack, PlayerEntity playerEntity, LivingEntity target, Hand hand) {

        if(playerEntity.world.isRemote || hand != Hand.MAIN_HAND)
            return true;

        ItemStack stack = playerEntity.getHeldItem(hand);
        if(playerEntity.isSneaking() && target instanceof IWandable){

            ((IWandable) target).onWanded(playerEntity);
            clear(stack, playerEntity);
            return true;
        }

        if((getPos(stack) == null || getPos(stack).equals(new BlockPos(0,0,0))) && getEntityID(stack) == -1){
            setEntityID(stack, target.getEntityId());
            PortUtil.sendMessage(playerEntity, "Stored entity");
            return true;
        }
        World world = playerEntity.getEntityWorld();

        if(getPos(stack) != null){
            if(world.getTileEntity(getPos(stack)) instanceof IWandable)
                ((IWandable) world.getTileEntity(getPos(stack))).onFinishedConnectionFirst(getPos(stack),target, playerEntity);

        }
        if(target instanceof IWandable) {
            ((IWandable) target).onFinishedConnectionLast(getPos(stack), target, playerEntity);
            clear(stack, playerEntity);
        }

        return true;
    }

    public void clear(ItemStack stack, PlayerEntity playerEntity){
        setPosTag(stack, null, 0);
        setEntityID(stack, -1);
    }


    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if(context.getWorld().isRemote || context.getPlayer() == null)
            return super.onItemUse(context);
        BlockPos pos = context.getPos();
        World world = context.getWorld();
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack stack = context.getItem();
        if(playerEntity.isSneaking() && world.getTileEntity(pos) instanceof IWandable){
            ((IWandable) world.getTileEntity(pos)).onWanded(playerEntity);
            clear(stack, playerEntity);
            return ActionResultType.CONSUME;
        }

        if(getEntityID(stack) == - 1 && (getPos(stack) == null || getPos(stack).equals(new BlockPos(0,0,0)))){
            setPosTag(stack, pos, playerEntity.dimension.getId());
            PortUtil.sendMessage(playerEntity, "Position set.");
            return ActionResultType.SUCCESS;
        }

        if(getPos(stack) != null){
            if(world.getTileEntity(getPos(stack)) instanceof IWandable) {
                ((IWandable) world.getTileEntity(getPos(stack))).onFinishedConnectionFirst(pos, (LivingEntity) world.getEntityByID(getEntityID(stack)), playerEntity);
            }
        }
        if(world.getTileEntity(pos) instanceof IWandable)
            ((IWandable) world.getTileEntity(pos)).onFinishedConnectionLast(getPos(stack), (LivingEntity) world.getEntityByID(getEntityID(stack)), playerEntity);

        if(getEntityID(stack) != -1 && world.getEntityByID(getEntityID(stack)) instanceof  IWandable){
            ((IWandable)world.getEntityByID(getEntityID(stack))).onFinishedConnectionFirst(pos, null, playerEntity);
        }
        clear(stack, playerEntity);
        return super.onItemUse(context);
    }

    public void drawConnection(BlockPos pos1, BlockPos pos2, ServerWorld world){
        ParticleUtil.beam(pos1, pos2, world);
    }

    public void setPosTag(ItemStack stack, BlockPos pos, int dim){
        CompoundNBT tag = stack.getTag();
        if(pos == null && tag != null && tag.contains("to_x")){
            tag.remove("to_x");
            tag.remove("to_y");
            tag.remove("to_z");
            tag.remove("to_dim");
        }else if(pos != null && tag != null){
            stack.getTag().putInt("to_x", pos.getX());
            stack.getTag().putInt("to_y", pos.getY());
            stack.getTag().putInt( "to_z", pos.getZ());
            stack.getTag().putInt("to_dim", dim);
        }
    }

    public void setEntityID(ItemStack stack, int id){
        CompoundNBT tag = stack.getTag();
        if(tag == null)
            return;
        stack.getTag().putInt("en_id", id);
    }
    public int getEntityID(ItemStack stack){
        CompoundNBT tag = stack.getTag();
        if(tag == null)
            return -1;
        return stack.getTag().getInt("en_id");
    }


    public BlockPos getPos(ItemStack stack){

        if(!stack.hasTag())
            return null;
        CompoundNBT tag = stack.getTag();
        return new BlockPos(tag.getInt("to_x"), tag.getInt("to_y"), tag.getInt("to_z"));
    }

    public int getDimension(ItemStack stack){
        if(!stack.hasTag())
            return -999;
        return stack.getTag().getInt("dim");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag p_77624_4_) {
        BlockPos pos = getPos(stack);
        tooltip.add(new StringTextComponent( getEntityID(stack) == -1 ? "No entity set" : "Entity stored."));
        if(pos == null){
            tooltip.add(new StringTextComponent("No location set."));
            return;
        }

        tooltip.add(new StringTextComponent("Stored: " + getPosString(pos)));
    }

    public static String getPosString(BlockPos pos){
        return "X: " + pos.getX() + " Y: " + pos.getY() + " Z:" + pos.getZ();
    }
}
