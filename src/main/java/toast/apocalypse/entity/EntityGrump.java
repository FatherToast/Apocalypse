package toast.apocalypse.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import toast.apocalypse.EventHandler;
import toast.apocalypse.MobHelper;
import toast.apocalypse.WorldDifficultyManager;

/**
 * This is a full moon mob that is meant to be a high threat to players that are not in a safe area from them.
 * Grumps fly, have a pulling attack, and have a melee attack that can't be reduced below 2 damage and applies a
 * short gravity effect.<br>
 * Unlike most full moon mobs, this one has no means of breaking through defenses and therefore relies on the
 * player being vulnerable to attack - whether by will or by other mobs breaking through to the player.
 */
public class EntityGrump extends EntityGhast implements IFullMoonMob {

    /** The currently targeted entity. */
    public Entity targetedEntity;
    /** Cooldown time between target loss and new target aquirement. */
    public int aggroCooldown;
    /** Time until this entity can shoot another fish hook. */
    public int rodTime;

    public EntityGrump(World world) {
        super(world);
        this.setSize(1.0F, 1.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true; // Immune to drowning
    }

    @Override
    protected String getLivingSound() {
        return null;
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
        if (!this.worldObj.isRemote && this.worldObj.difficultySetting.getDifficultyId() == 0) {
            this.setDead();
        }
        this.despawnEntity();
        this.prevAttackCounter = this.attackCounter;
        // Update movement, targets, and attacks
        this.updateEntityGoal();
    }

    /** Updates the current goal. */
    protected void updateEntityGoal() {
        // Perform movement
        double vX = this.waypointX - this.posX;
        double vY = this.waypointY - this.posY;
        double vZ = this.waypointZ - this.posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        if (v < 0.1 || v > 3600.0) {
            if (this.targetedEntity != null) {
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
                this.setRandomWaypoints(8.0F);
            }
        }
        // Update the current target
        this.updateEntityTarget();
        // Execute goal, if able
        if (this.attackCounter > 0) {
            this.attackCounter--;
        }
        if (this.rodTime > 0) {
            this.rodTime--;
        }
        if (this.targetedEntity != null) {
            this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(this.targetedEntity.posX - this.posX, this.targetedEntity.posZ - this.posZ)) * 180.0F / (float)Math.PI;
            double distance = this.getDistanceSq(this.targetedEntity.posX, this.targetedEntity.posY + this.targetedEntity.height / 2.0F, this.targetedEntity.posZ);
            if (this.attackCounter <= 0) {
                double reach = this.width * this.width * 4.0F + this.targetedEntity.width;
                if (distance <= reach) {
                    this.attackCounter = 20;
                    this.swingItem();
                    this.attackEntityAsMob(this.targetedEntity);
                }
            }
            if (this.rodTime <= 0) {
                if (distance > 9.0F && distance < 100.0F && this.getEntitySenses().canSee(this.targetedEntity)) {
                    this.worldObj.spawnEntityInWorld(new EntityMonsterFishHook(this.worldObj, this, this.targetedEntity));
                    this.worldObj.playSoundAtEntity(this, "random.bow", 0.5F, 0.4F / (this.rand.nextFloat() * 0.4F + 0.8F));
                    this.rodTime = this.rand.nextInt(32) + 32;
                }
            }
        }
        else {
            this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float)Math.PI;
        }
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

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (MobHelper.attackEntityForMob(this, target, 2.0F)) {
            if (target instanceof EntityPlayer) {
                EventHandler.applyGravity((EntityPlayer) target, 20);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        for (int i = this.rand.nextInt(3); i-- > 0;) {
            this.dropItem(Items.cookie, 1);
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
        if ("fireball".equals(source.getDamageType()) && source.getEntity() instanceof EntityPlayer)
            return super.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) source.getEntity()), damage);
        return super.attackEntityFrom(source, damage);
    }

    @Override
    public boolean getCanSpawnHere() {
        return MobHelper.canSpawn(this) && (this.dimension != 0 || this.worldObj.canBlockSeeTheSky((int)Math.floor(this.posX), (int)Math.floor(this.posY), (int)Math.floor(this.posZ)));
    }
}