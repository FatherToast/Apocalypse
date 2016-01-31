package toast.apocalypse.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toast.apocalypse.MobHelper;
import toast.apocalypse.WorldDifficultyManager;

/**
 * This is a full moon mob similar to a ghast, though it has unlimited aggro range ignoring line of sight and
 * its fireballs can destroy anything within a small area.
 */
public class EntityDestroyer extends EntityGhast implements IFullMoonMob {

    /** The base explosion strength of this ghast's fireballs. */
    public int explosionStrength = 1;
    /** The currently targeted entity. */
    public Entity targetedEntity;
    /** Cooldown time between target loss and new target aquirement. */
    public int aggroCooldown;
    /** Whether the target was in range last tick. */
    public boolean prevInRange;

    public EntityDestroyer(World world) {
        super(world);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true; // Immune to drowning
    }

    @Override
    protected String getLivingSound() {
        return null;
    }

    /** @return True if the fire (shooting) texture should be displayed. */
    public byte getFireTexture() {
        return this.dataWatcher.getWatchableObjectByte(16);
    }
    /** @param fire If true, turns the fire (shooting) texture on, turns it off if false. */
    public void setFireTexture(boolean fire) {
        this.dataWatcher.updateObject(16, Byte.valueOf(fire ? (byte) 1 : (byte) 0));
    }

    @Override
    public void onLivingUpdate() {
        if (!WorldDifficultyManager.isFullMoon(this.worldObj)) {
            this.entityAge += 4;
        }
        super.onLivingUpdate();
    }
    @Override
    protected void updateEntityActionState() {
        // Check for despawning
        this.despawnEntity();
        this.prevAttackCounter = this.attackCounter;
        // Update movement, targets, and attacks
        this.updateEntityGoal();
        // Update the texture
        if (!this.worldObj.isRemote) {
            boolean shooting = this.getFireTexture() == 1;
            boolean shouldBeShooting = this.attackCounter > 10;
            if (shooting != shouldBeShooting) {
                this.setFireTexture(shouldBeShooting);
            }
        }
    }

    /** Updates the current goal. */
    protected void updateEntityGoal() {
        // Update the current target
        this.updateEntityTarget();
        // Perform movement
        float distanceSq = Float.POSITIVE_INFINITY;
        if (this.targetedEntity != null) {
            distanceSq = (float)this.targetedEntity.getDistanceSqToEntity(this);
        }
        boolean inRange = distanceSq < 256.0;
        double vX = this.waypointX - this.posX;
        double vY = this.waypointY - this.posY;
        double vZ = this.waypointZ - this.posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        if (v < 1.0 || v > 3600.0 || inRange != this.prevInRange) {
            if (inRange) {
                this.setRandomWaypoints(6.0F);
            }
            else if (this.targetedEntity != null) {
                this.waypointX = this.targetedEntity.posX;
                this.waypointY = this.targetedEntity.posY + this.targetedEntity.height / 2.0F;
                this.waypointZ = this.targetedEntity.posZ;
                if (!this.isCourseTraversable(Math.sqrt(v))) {
                    this.setRandomWaypoints(32.0F);
                }
            }
            else {
                this.setRandomWaypoints(32.0F);
                this.waypointY = Math.max(this.waypointY, Math.max(70.0, this.worldObj.getHeightValue((int)Math.floor(this.waypointX), (int)Math.floor(this.waypointZ)) + 16.0));
            }
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.rand.nextInt(5) + 2;
            v = Math.sqrt(v);
            if (this.isCourseTraversable(v)) {
                double speed = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() / v;
                this.motionX += vX * speed;
                this.motionY += vY * speed;
                this.motionZ += vZ * speed;
            }
            else {
                this.clearWaypoints();
            }
        }
        // Execute goal, if able
        if (distanceSq < 4096.0) {
            double x = this.targetedEntity.posX - this.posX;
            double z = this.targetedEntity.posZ - this.posZ;
            this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(x, z)) * 180.0F / (float)Math.PI;

            if (this.attackCounter == 10) {
                this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1007, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
            }
            this.attackCounter++;
            if (this.attackCounter == 20) {
                if (!this.worldObj.isRemote) {
                    this.shootFireballAtEntity(this.targetedEntity);
                }
                this.attackCounter = -40;
            }
        }
        else {
            this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float)Math.PI;
            if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        }
        this.prevInRange = inRange;
    }

    /** Sets a random waypoint within range.
     * @param range The edge length of the cubic area the waypoint will be contained in. */
    public void setRandomWaypoints(float range) {
        this.waypointX = this.posX + (this.rand.nextFloat() - 0.5F) * range;
        this.waypointY = this.posY + (this.rand.nextFloat() - 0.5F) * range;
        this.waypointZ = this.posZ + (this.rand.nextFloat() - 0.5F) * range;
    }

    /** Removes the active waypoint by setting it to the entity's current position. */
    public void clearWaypoints() {
        this.waypointX = this.posX;
        this.waypointY = this.posY;
        this.waypointZ = this.posZ;
    }

    /** @param v The velocity of the ghast. Higher velocities cause more checks.
     * @return True if the ghast has an unobstructed line of travel to the waypoint. */
    public boolean isCourseTraversable(double v) {
        double dX = (this.waypointX - this.posX) / v;
        double dY = (this.waypointY - this.posY) / v;
        double dZ = (this.waypointZ - this.posZ) / v;
        AxisAlignedBB aabb = this.boundingBox.copy();
        for (int i = 1; i < v; i++) {
            aabb.offset(dX, dY, dZ);
            if (!this.worldObj.getCollidingBoundingBoxes(this, aabb).isEmpty())
                return false;
        }
        return true;
    }

    /** Updates this entity's target. */
    protected void updateEntityTarget() {
        if (this.targetedEntity != null && this.targetedEntity.isDead) {
            this.targetedEntity = null;
        }
        if (this.targetedEntity == null || this.aggroCooldown-- <= 0) {
            this.targetedEntity = this.worldObj.getClosestVulnerablePlayerToEntity(this, -1.0);
            if (this.targetedEntity != null) {
                this.aggroCooldown = 20;
            }
        }
    }

    /** Called to attack an entity with a fireball.
     * @param target The entity to shoot the fireball at. */
    public void shootFireballAtEntity(Entity target) {
        double x = target.posX - this.posX;
        double y = target.boundingBox.minY - (this.posY + this.height / 2.0F);
        double z = target.posZ - this.posZ;
        this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1008, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
        EntityDestroyerFireball fireball = new EntityDestroyerFireball(this.worldObj, this, x, y, z);
        Vec3 vec3 = this.getLook(1.0F);
        fireball.posX = this.posX + vec3.xCoord * this.width;
        fireball.posY = this.posY + this.height / 2.0F;
        fireball.posZ = this.posZ + vec3.zCoord * this.width;
        this.worldObj.spawnEntityInWorld(fireball);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("ExplosionPower", this.explosionStrength);
    }
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("ExplosionPower")) {
            this.explosionStrength = tag.getInteger("ExplosionPower");
        }
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
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (this.isEntityInvulnerable())
            return false;
        // Prevent return to sender achievement
        if ("fireball".equals(source.getDamageType()) && source.getEntity() instanceof EntityPlayer) {
            super.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) source.getEntity()), damage * 2.0F + this.getMaxHealth() / 2.0F);
            return true;
        }
        else if (source.getEntity() == this)
            return false;
        return super.attackEntityFrom(source, damage);
    }

    @Override
    public boolean getCanSpawnHere() {
        return MobHelper.canSpawn(this) && (this.dimension != 0 || this.worldObj.canBlockSeeTheSky((int)Math.floor(this.posX), (int)Math.floor(this.posY), (int)Math.floor(this.posZ)));
    }
}