package toast.apocalypse.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

/**
 * This is the type of fireball shot by destroyers. It is capable of destroying any block.
 */
public class EntityDestroyerFireball extends EntityFireball {

    /** The base explosion strength of this fireball. */
    public int explosionStrength = 1;
    /** The time until this fireball explodes. Set when reflected. */
    private int fuseTime = -1;
    /** The target to seek, if any. */
    public Entity seekTarget;

    public EntityDestroyerFireball(World world) {
        super(world);
    }
    public EntityDestroyerFireball(World world, EntityDestroyer shooter, double x, double y, double z) {
        super(world, shooter, x, y, z);
        this.explosionStrength = shooter.explosionStrength;
    }

    @Override
    protected void onImpact(MovingObjectPosition object) {
        if (!this.worldObj.isRemote) {
            if (object.entityHit != null) {
                object.entityHit.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.shootingEntity), 4.0F);
                object.entityHit.setFire(5);
            }
            this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, this.explosionStrength, true, this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
            this.setDead();
        }
    }

    // Returns an adjusted blast resistance for a block.
    @Override
    public float func_145772_a(Explosion explosion, World world, int x, int y, int z, Block block) {
        return Math.min(0.8F, super.func_145772_a(explosion, world, x, y, z, block));
    }

    @Override
    public void onUpdate() {
        if (this.seekTarget != null) {
            double x = this.seekTarget.posX - this.posX;
            double y = this.seekTarget.posY + this.seekTarget.height / 2.0F - this.posY;
            double z = this.seekTarget.posZ - this.posZ;
            double v = Math.sqrt(x * x + y * y + z * z);
            this.motionX = x / v;
            this.motionY = y / v;
            this.motionZ = z / v;
        }
        if (this.fuseTime >= 0 && --this.fuseTime < 0) {
            this.onImpact(new MovingObjectPosition((int) Math.floor(this.posX), (int) Math.floor(this.posY), (int) Math.floor(this.posZ), 0, Vec3.createVectorHelper(this.posX, this.posY, this.posZ), false));
        }
        super.onUpdate();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("ExplosionPower", this.explosionStrength);
        tag.setByte("fuseTime", (byte) this.fuseTime);
    }
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("ExplosionPower")) {
            this.explosionStrength = tag.getInteger("ExplosionPower");
        }
        if (tag.hasKey("fuseTime")) {
            this.fuseTime = tag.getByte("fuseTime");
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        this.setBeenAttacked();
        if (this.fuseTime < 0 && source.getEntity() != null) {
        	// Reflect fireball
            this.fuseTime = 10;
            Vec3 vec3 = source.getEntity().getLookVec();
            if (vec3 != null) {
                this.motionX = vec3.xCoord;
                this.motionY = vec3.yCoord;
                this.motionZ = vec3.zCoord;
                this.accelerationX = this.motionX * 0.1;
                this.accelerationY = this.motionY * 0.1;
                this.accelerationZ = this.motionZ * 0.1;
            }
            if (source.getEntity() instanceof EntityLivingBase) {
                this.shootingEntity = (EntityLivingBase)source.getEntity();
            }
            else {
                this.shootingEntity = null;
            }
            return true;
        }
        return false;
    }
}
