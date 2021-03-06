package com.hollingsworth.arsnouveau.common.block.tile;

import com.hollingsworth.arsnouveau.api.util.NBTUtil;
import com.hollingsworth.arsnouveau.common.entity.EntityFollowProjectile;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ArcaneRelaySplitterTile extends ArcaneRelayTile{

    ArrayList<BlockPos> toList = new ArrayList<>();
    ArrayList<BlockPos> fromList = new ArrayList<>();

    public ArcaneRelaySplitterTile() {
        super(BlockRegistry.ARCANE_RELAY_SPLITTER_TILE);
    }

    @Override
    public boolean setTakeFrom(BlockPos pos) {
        return closeEnough(pos, 10) && fromList.add(pos) && update();
    }

    @Override
    public boolean setSendTo(BlockPos pos) {
        return closeEnough(pos, 10) && toList.add(pos) && update();
    }

    @Override
    public void clearPos() {
        this.toList.clear();
        this.fromList.clear();
        update();
    }

    public void processFromList(){
        if(fromList.isEmpty())
            return;
        ArrayList<BlockPos> stale = new ArrayList<>();
        int ratePer = getTransferRate() / fromList.size();
        for(BlockPos fromPos : fromList){
            if(!(world.getTileEntity(fromPos) instanceof AbstractManaTile)){
                stale.add(fromPos);
                continue;
            }
            AbstractManaTile fromTile = (AbstractManaTile) world.getTileEntity(fromPos);
            if(fromTile.getCurrentMana() >= ratePer && this.getCurrentMana() + ratePer <= this.getMaxMana()){
                fromTile.removeMana(ratePer);
                this.addMana(ratePer);
                EntityFollowProjectile aoeProjectile = new EntityFollowProjectile(world, fromPos, pos);
                world.addEntity(aoeProjectile);
            }
        }
        for(BlockPos s : stale)
            fromList.remove(s);

    }

    public void processToList(){
        if(toList.isEmpty())
            return;
        ArrayList<BlockPos> stale = new ArrayList<>();
        int ratePer = getTransferRate() / toList.size();
        for(BlockPos toPos : toList){
            if(!(world.getTileEntity(toPos) instanceof AbstractManaTile)){
                stale.add(toPos);
                continue;
            }
            AbstractManaTile toTile = (AbstractManaTile) world.getTileEntity(toPos);
            if(this.getCurrentMana() >= ratePer && toTile.getCurrentMana() + ratePer <= toTile.getMaxMana()){
                this.removeMana(ratePer);
                toTile.addMana(ratePer);
                EntityFollowProjectile aoeProjectile = new EntityFollowProjectile(world,  pos, toPos);
                world.addEntity(aoeProjectile);
            }
        }
        for(BlockPos s : stale)
            toList.remove(s);

    }

    @Override
    public void tick() {
        if(world.getGameTime() % 20 != 0 || toList.isEmpty())
            return;
        processFromList();
        processToList();
        update();
    }

    @Override
    public int getTransferRate() {
        return 500;
    }

    @Override
    public int getMaxMana() {
        return 2500;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        int counter = 0;
        while(NBTUtil.hasBlockPos(tag, "from_" + counter)){
            BlockPos pos = NBTUtil.getBlockPos(tag, "from_" + counter);
            if(!fromList.contains(pos))
                this.fromList.add(pos);
            counter++;
        }

        counter = 0;
        while(NBTUtil.hasBlockPos(tag, "to_" + counter)){
            BlockPos pos = NBTUtil.getBlockPos(tag, "to_" + counter);
            if(!toList.contains(pos))
                this.toList.add(pos);
            counter++;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        int counter = 0;
        for(BlockPos p : this.fromList){
            NBTUtil.storeBlockPos(tag, "from_" +counter, p);
            counter++;
        }
        counter = 0;
        for(BlockPos p : this.toList){
            NBTUtil.storeBlockPos(tag, "to_" +counter, p);
            counter ++;
        }
        return super.write(tag);
    }

    @Override
    public List<String> getTooltip() {
        List<String> list = new ArrayList<>();
        if(toList == null || toList.isEmpty()){
            list.add(new TranslationTextComponent("ars_nouveau.relay.no_to").getString());
        }else{
            list.add(new TranslationTextComponent("ars_nouveau.relay.one_to", toList.size()).getString());
        }
        if(fromList == null || fromList.isEmpty()){
            list.add(new TranslationTextComponent("ars_nouveau.relay.no_from").getString());
        }else{
            list.add(new TranslationTextComponent("ars_nouveau.relay.one_from", fromList.size()).getString());
        }
        return list;
    }
}
