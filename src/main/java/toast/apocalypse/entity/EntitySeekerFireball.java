package toast.apocalypse.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * This is the type of fireball shot by seekers. It homes in on the shooter when relfected.
 */
public class EntitySeekerFireball extends EntityFireball {

    /** The base explosion strength of this fireball. */
    public int explosionStrength = 1;
    /** Whether the shooter could see the target when this was shot. */
    private boolean canSee = true;
    /** If true, this fireball has been reflected. */
    private boolean reflected;
    /** The target to seek. Only really set when reflected. */
    public Entity seekTarget;

    public EntitySeekerFireball(World world) {
        super(world);
    }
    public EntitySeekerFireball(World world, EntitySeeker shooter, double x, double y, double z, boolean canSee) {
        super(world, shooter, x, y, z);
        this.explosionStrength = shooter.explosionStrength;
        this.canSee = canSee;
    }

    @Override
    protected void onImpact(MovingObjectPosition object) {
        if (!this.worldObj.isRemote) {
            if (object.entityHit != null) {
                object.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 4.0F);
                object.entityHit.setFire(5);
            }
            if (!this.canSee) {
                this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, this.explosionStrength * 3.0F, true, this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
            }
            else if (object.entityHit == null) {
                int i = object.blockX;
                int j = object.blockY;
                int k = object.blockZ;
                switch (object.sideHit) {
                    case 0:
                        j--;
                        break;
                    case 1:
                        j++;
                        break;
                    case 2:
                        k--;
                        break;
                    case 3:
                        k++;
                        break;
                    case 4:
                        i--;
                        break;
                    case 5:
                        i++;
                        break;
                }
                if (this.worldObj.isAirBlock(i, j, k)) {
                    this.worldObj.setBlock(i, j, k, Blocks.fire);
                }
            }
            this.setDead();
        }
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
        else if (!this.worldObj.isRemote && this.reflected) {
            this.setDead();
        }
        super.onUpdate();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("ExplosionPower", this.explosionStrength);
        tag.setBoolean("canSee", this.canSee);
        tag.setBoolean("reflected", this.reflected);
    }
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("ExplosionPower")) {
            this.explosionStrength = tag.getInteger("ExplosionPower");
        }
        if (tag.hasKey("canSee")) {
            this.canSee = tag.getBoolean("canSee");
        }
        if (tag.hasKey("reflected")) {
            this.reflected = tag.getBoolean("reflected");
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        this.setBeenAttacked();
        if (!this.reflected && source.getEntity() != null) {
        	// Reflect fireball
            this.reflected = true;
            if (this.shootingEntity != null) {
                this.seekTarget = this.shootingEntity;
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
