package com.hollingsworth.arsnouveau.common.block.tile;

import com.hollingsworth.arsnouveau.api.util.NBTUtil;
import com.hollingsworth.arsnouveau.common.block.PortalBlock;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketWarpPosition;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

import static com.hollingsworth.arsnouveau.setup.BlockRegistry.PORTAL_TILE_TYPE;

public class PortalTile extends TileEntity implements ITickableTileEntity {
    public BlockPos warpPos;
    public int dimID;
    public PortalTile() {
        super(PORTAL_TILE_TYPE);
    }



    public void warp(Entity e){
        if(!world.isRemote && warpPos != null && !(world.getBlockState(warpPos).getBlock() instanceof PortalBlock)) {
            System.out.println("warping");
            e.setLocationAndAngles(warpPos.getX(), warpPos.getY(), warpPos.getZ(), e.rotationYaw, e.rotationPitch);
            Networking.sendToNearby(world, e, new PacketWarpPosition(e.getEntityId(), e.getPosX(), e.getPosY(), e.getPosZ(), true));
            ((ServerWorld) world).spawnParticle(ParticleTypes.PORTAL, warpPos.getX(),  warpPos.getY() + 1,  warpPos.getZ(),
                    4,(this.world.rand.nextDouble() - 0.5D) * 2.0D, -this.world.rand.nextDouble(), (this.world.rand.nextDouble() - 0.5D) * 2.0D, 0.1f);

        }
    }


    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.dimID = compound.getInt("dim");
        this.warpPos = NBTUtil.getBlockPos(compound, "warp");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if(this.warpPos != null){
            NBTUtil.storeBlockPos(compound, "warp", this.warpPos);
        }
        compound.putInt("dim", this.dimID);

        return super.write(compound);
    }

    @Override
    public void tick() {
        if(!world.isRemote && warpPos != null && !(world.getBlockState(warpPos).getBlock() instanceof PortalBlock)) {
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos));
            for(Entity e : entities){
                world.playSound(null, warpPos, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                if(e instanceof EntityProjectileSpell) {
                    e.setLocationAndAngles(warpPos.getX(), warpPos.getY(), warpPos.getZ(), e.rotationYaw, e.rotationPitch);
                }else {
                    e.setPositionAndUpdate(warpPos.getX(), warpPos.getY(), warpPos.getZ());
                }
                Networking.sendToNearby(world, e, new PacketWarpPosition(e.getEntityId(),warpPos.getX(), warpPos.getY(), warpPos.getZ(), true));
                ((ServerWorld) world).spawnParticle(ParticleTypes.PORTAL, warpPos.getX(),  warpPos.getY() + 1,  warpPos.getZ(),
                    4,(this.world.rand.nextDouble() - 0.5D) * 2.0D, -this.world.rand.nextDouble(), (this.world.rand.nextDouble() - 0.5D) * 2.0D, 0.1f);

            }
        }
    }
}
