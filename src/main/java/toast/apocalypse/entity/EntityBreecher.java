package toast.apocalypse.entity;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import toast.apocalypse.MobHelper;
import toast.apocalypse.WorldDifficultyManager;

/**
 * This is a full moon mob identical to a creeper in almost every way, except that it has a much farther aggro range
 * that ignores line of sight and will explode when they detect that they can't get any closer to the player.<br>
 * Visually, the ony difference is that their eyes are entranced by the moon's power.
 */
public class EntityBreecher extends EntityCreeper implements IFullMoonMob {

    /** Set to true when this creeper is exploding from being blocked. */
    private boolean exploding;

    public EntityBreecher(World world) {
        super(world);
        this.targetTasks.addTask(0, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0);
    }

    /** @return True if this breach creeper should explode. */
    public boolean shouldExplode() {
        if (this.exploding)
            return true;

        EntityLivingBase target = this.getAttackTarget();
        if (target == null)
            return false;
        double dX = target.posX - this.posX;
        double dZ = target.posZ - this.posZ;

        float range = (target.width + this.width) / 2.0F;
        if (target.boundingBox.maxY <= this.boundingBox.minY && dX * dX + dZ * dZ < range * range && this.motionX * this.motionX + this.motionZ * this.motionZ < 0.1)
            return this.exploding = true; // right above, moving slow or stopped

        if (dX < 0.0 && this.motionX > 0.0 || dX > 0.0 && this.motionX < 0.0 || dZ < 0.0 && this.motionZ > 0.0 || dZ > 0.0 && this.motionZ < 0.0)
            return false; // moving away, presumably to path
        double dH = Math.sqrt(dX * dX + dZ * dZ) * 2.0;
        dX /= dH;
        dZ /= dH;

        double vX = dX;
        double vZ = dZ;
        List list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(dX, 0.0, dZ));
        for (Object box : list) {
            vX = ((AxisAlignedBB)box).calculateXOffset(this.boundingBox, vX);
        }
        for (Object box : list) {
            vZ = ((AxisAlignedBB)box).calculateZOffset(this.boundingBox, vZ);
        }
        boolean hitX = vX != dX;
        boolean hitZ = vZ != dZ;
        if (!hitX && !hitZ)
            return false; // not even by anything to jump over

        list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(dX, 1.0, dZ));
        vX = dX;
        vZ = dZ;
        for (Object box : list) {
            vX = ((AxisAlignedBB)box).calculateXOffset(this.boundingBox, vX);
        }
        for (Object box : list) {
            vZ = ((AxisAlignedBB)box).calculateZOffset(this.boundingBox, vZ);
        }
        hitX = hitX && vX != dX;
        hitZ = hitZ && vZ != dZ;

        if (!hitX && !hitZ)
            return false; // not by anything at all
        if (hitX && hitZ)
            return this.exploding = true; // in corner
        if (hitX && target.boundingBox.maxZ > this.boundingBox.minZ && target.boundingBox.minZ < this.boundingBox.maxZ)
            return this.exploding = true; // z-aligned
        if (hitZ && target.boundingBox.maxX > this.boundingBox.minX && target.boundingBox.minX < this.boundingBox.maxX)
            return this.exploding = true; // x-aligned
        return false; // blocked, but not close enough
    }

    @Override
    public void onUpdate() {
        if (this.isEntityAlive() && this.shouldExplode()) {
            this.setCreeperState(1);
        }
        super.onUpdate();
    }
    @Override
    public void onLivingUpdate() {
        if (!WorldDifficultyManager.isFullMoon(this.worldObj)) {
            this.entityAge += 4;
        }
        super.onLivingUpdate();
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        for (int i = this.rand.nextInt(3) + 1; i-- > 0;) {
            this.dropItem(Items.gunpowder, 1);
        }
        for (int i = this.rand.nextInt(2); i-- > 0;) {
            this.dropItem(Items.experience_bottle, 1);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        int x = (int) Math.floor(this.posX);
        int y = (int) Math.floor(this.posY) - 1;
        int z = (int) Math.floor(this.posZ);
        return MobHelper.canSpawn(this) && this.worldObj.getBlock(x, y, z).getCollisionBoundingBoxFromPool(this.worldObj, x, y, z) != null;
    }
}
