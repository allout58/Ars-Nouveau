package com.hollingsworth.arsnouveau.common.spell.method;

import com.hollingsworth.arsnouveau.ModConfig;
import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.AbstractCastMethod;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAccelerate;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentPierce;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSplit;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MethodProjectile extends AbstractCastMethod {

    public MethodProjectile() {
        super(ModConfig.MethodProjectileID, "Projectile");
    }

    @Override
    public int getManaCost() {
        return 10;
    }

    public void summonProjectiles(World world, LivingEntity shooter, List<AbstractAugment> augments){
        ArrayList<EntityProjectileSpell> projectiles = new ArrayList<>();
        int numPierce = getBuffCount(augments, AugmentPierce.class);
        EntityProjectileSpell projectileSpell = new EntityProjectileSpell(world, shooter, this.resolver, numPierce);
        projectiles.add(projectileSpell);
        int numSplits = getBuffCount(augments, AugmentSplit.class);

        for(int i =1; i < numSplits + 1; i++){
            Direction offset =shooter.getHorizontalFacing().rotateY();
            if(i%2==0) offset = offset.getOpposite();
             // Alternate sides
            BlockPos projPos = shooter.getPosition().offset(offset, i);
            projPos = projPos.add(0, 1.5, 0);
            EntityProjectileSpell spell = new EntityProjectileSpell(world, shooter, this.resolver, numPierce);
            spell.setPosition(projPos.getX(), projPos.getY(), projPos.getZ());
            projectiles.add(spell);
        }

        float velocity = 1.0f + getBuffCount(augments, AugmentAccelerate.class);

        for(EntityProjectileSpell proj : projectiles) {
            proj.shoot(shooter, shooter.rotationPitch, shooter.rotationYaw, 0.0F, velocity, .80F);
            world.addEntity(proj);
        }
    }
    // Summons the projectiles directly above the block, facing downwards.
    public void summonProjectiles(World world, BlockPos pos, LivingEntity shooter, List<AbstractAugment> augments){
        ArrayList<EntityProjectileSpell> projectiles = new ArrayList<>();
        int numPierce = getBuffCount(augments, AugmentPierce.class);
        EntityProjectileSpell projectileSpell = new EntityProjectileSpell(world, shooter, this.resolver, numPierce);
        projectileSpell.setPosition(pos.getX(), pos.getY() +1, pos.getZ());
        projectiles.add(projectileSpell);

        int numSplits = getBuffCount(augments, AugmentSplit.class);

        for(int i =1; i < numSplits + 1; i++){
            Direction offset = shooter.getHorizontalFacing().rotateY();
            if(i%2==0) offset = offset.getOpposite();
            // Alternate sides
            BlockPos projPos = pos.offset(offset, i);
            projPos = projPos.add(0, 1.5, 0);
            EntityProjectileSpell spell = new EntityProjectileSpell(world, shooter, this.resolver, numPierce);
            spell.setPosition(projPos.getX(), projPos.getY(), projPos.getZ());
            projectiles.add(spell);
        }

        float velocity = 1.0f + getBuffCount(augments, AugmentAccelerate.class);


        for(EntityProjectileSpell proj : projectiles) {

            proj.shoot(shooter, 90,90f, 0.0F, velocity, .00F);
            world.addEntity(proj);
        }
    }

    @Override
    public void onCast(ItemStack stack, LivingEntity shooter, World world, List<AbstractAugment> augments) {
        summonProjectiles(world, shooter, augments);
        resolver.expendMana(shooter);
    }

    @Override
    public void onCastOnBlock(ItemUseContext context, List<AbstractAugment> augments) {
        World world = context.getWorld();
        PlayerEntity shooter = context.getPlayer();
        summonProjectiles(world, shooter, augments);
        resolver.expendMana(shooter);
    }

    /**
     * Cast by entities.
     */
    @Override
    public void onCastOnBlock(BlockRayTraceResult blockRayTraceResult, LivingEntity caster, List<AbstractAugment> augments) {
        caster.lookAt(EntityAnchorArgument.Type.EYES, blockRayTraceResult.getHitVec());
        summonProjectiles(caster.getEntityWorld(), blockRayTraceResult.getPos(), caster, augments);
        resolver.expendMana(caster);
    }

    @Override
    public void onCastOnEntity(ItemStack stack, LivingEntity caster, LivingEntity target, Hand hand, List<AbstractAugment> augments) {
        summonProjectiles(caster.getEntityWorld(), caster, augments);
        resolver.expendMana(caster);
    }

    @Override
    public boolean wouldCastSuccessfully(@Nullable ItemStack stack, LivingEntity playerEntity, World world, List<AbstractAugment> augments) {
        return true;
    }

    @Override
    public boolean wouldCastOnBlockSuccessfully(ItemUseContext context, List<AbstractAugment> augments) {
        return true;
    }

    @Override
    public boolean wouldCastOnBlockSuccessfully(BlockRayTraceResult blockRayTraceResult, LivingEntity caster, List<AbstractAugment> augments) {
        return true;
    }

    @Override
    public boolean wouldCastOnEntitySuccessfully(@Nullable ItemStack stack, LivingEntity caster, LivingEntity target, Hand hand, List<AbstractAugment> augments) {
        return true;
    }

    @Override
    protected String getBookDescription() {
        return "A spell you start with. Summons a projectile that applies spell effects when this projectile hits a target or block.";
    }
}
