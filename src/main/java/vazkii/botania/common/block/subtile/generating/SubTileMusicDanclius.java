/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Feb 15, 2014, 9:47:56 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.generating;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.apache.logging.log4j.Level;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileGenerating;
import vazkii.botania.common.lexicon.LexiconData;

import java.util.ArrayList;
import java.util.List;

public class SubTileMusicDanclius extends SubTileGenerating {

    private static final int RANGE = 3;
    public int resetSec= 300;
    public int HappyEnergy =0;
    public boolean CoordEqual;
    public ArrayList<int[]> DancedBlocks = new ArrayList<>();

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!supertile.getWorldObj().isRemote &&this.linkedCollector != null && ticksExisted % 20 == 0){
        List<EntityLivingBase> dancers = supertile.getWorldObj().getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(supertile.xCoord - RANGE, supertile.yCoord, supertile.zCoord - RANGE, supertile.xCoord + RANGE + 1, supertile.yCoord + 1, supertile.zCoord + RANGE + 1));
            for (EntityLivingBase dancer : dancers) {
                if (dancer != null&& !dancer.isDead&&dancer.distanceWalkedOnStepModified != dancer.prevDistanceWalkedModified){
                    CoordEqual = false;
                    DancedBlocks.forEach(Sets -> isCoordEqual(Sets,new int[]{(int) dancer.posX, (int) dancer.posY, (int) dancer.posZ}));
                    if (!CoordEqual){
                        HappyEnergy += 60;
                        DancedBlocks.add(new int[]{(int) dancer.posX, (int) dancer.posY, (int) dancer.posZ});
                    }else HappyEnergy += 4;
                    //More people,More frequently reset. Have a party!
                    resetSec --;
                    return;
                }
            }
            if (resetSec <= 0) {
                resetSec = 300;
                DancedBlocks.clear();
            }
        }
       if (HappyEnergy > 0) HappyEnergy --;


       for (int x=supertile.xCoord -RANGE;x<supertile.xCoord+RANGE;x++){
            for (int z=supertile.zCoord -RANGE;z<supertile.zCoord+RANGE;z++){
                supertile.getWorldObj().spawnParticle("minecraft:note", x, supertile.yCoord + 0.1, z, 0.0D, 0.1D, 0.0D);
            }}}
    public boolean isCoordEqual(int[] set1,int[] set2){
        if(set1.length != set2.length) return false;
        for (int i=0;i < set1.length;i++)if (set1[i] != set2[i]) return false;
        CoordEqual = true;
        return true;
    }
    @Override
    public int getMaxMana() {
        return 1000;
    }

    @Override
    public int getColor() {
        return 0x785000;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Square(toChunkCoordinates(), RANGE);
    }

    @Override
    public LexiconEntry getEntry() {
        return LexiconData.musicDanclius;
    }
    @Override
    public int getValueForPassiveGeneration() {
        return 6;
    }
    @Override
    public int getDelayBetweenPassiveGeneration() {
        return 2;
    }
@Override
    public boolean canGeneratePassively() {
        return HappyEnergy > 0;
    }

}
