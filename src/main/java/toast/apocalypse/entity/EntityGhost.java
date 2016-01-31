package toast.apocalypse.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import toast.apocalypse.EventHandler;
import toast.apocalypse.MobHelper;
import toast.apocalypse.WorldDifficultyManager;

/**
 * This is a full moon mob that has the odd ability to completely ignore blocks. To compliment this, it has
 * unlimited aggro range and ignores line of sight.<br>
 * These are the bread and butter of invasions. Ghosts deal light damage that can't be reduced below 1 and apply
 * a short gravity effect to help deal with flying players.
 */
public class EntityGhost extends EntityFlying implements IMob, IFullMoonMob {

    /** Cooldown to prevent constant course recalculations. */
    public int courseChangeCooldown;
    /** Current target waypoints. */
    public double waypointX, waypointY, waypointZ;
    /** The currently targeted entity. */
    public Entity targetedEntity;
    /** Cooldown time between target loss and new target aquirement. */
    public int aggroCooldown;
    /** The counter used for the attack cooldown. */
    public int prevAttackCounter, attackCounter;

    public EntityGhost(World world) {
        super(world);
        this.setSize(0.6F, 1.8F);
        this.noClip = true;
        this.experienceValue = 3;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(4.0);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.1);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true; // Immune to drowning
    }

    @Override
    public boolean handleLavaMovement() {
        return false; // Immune to lava
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false; // Immune to suffocation
    }

    @Override
    protected String getLivingSound() {
        return "mob.blaze.breathe";
    }
    @Override
    protected String getHurtSound() {
        return "mob.endermen.scream";
    }
    @Override
    protected String getDeathSound() {
        return "mob.blaze.death";
    }
    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public void onLivingUpdate() {
        if (!WorldDifficultyManager.isFullMoon(this.worldObj)) {
            this.entityAge += 4;
        }
        if (this.worldObj.isDaytime() && !this.worldObj.isRemote) {
            float brightness = this.getBrightness(1.0F);
            if (brightness > 0.5F && this.rand.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F && this.worldObj.canBlockSeeTheSky((int) Math.floor(this.posX), (int) Math.floor(this.posY), (int) Math.floor(this.posZ))) {
                ItemStack helmet = this.getEquipmentInSlot(4);
                if (helmet != null) {
                    if (helmet.isItemStackDamageable()) {
                        helmet.setItemDamage(helmet.getItemDamageForDisplay() + this.rand.nextInt(2));
                        if (helmet.getItemDamageForDisplay() >= helmet.getMaxDamage()) {
                            this.renderBrokenItemStack(helmet);
                            this.setCurrentItemOrArmor(4, (ItemStack)null);
                        }
                    }
                }
                else {
                    this.setFire(8);
                }
            }
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
        if (MobHelper.attackEntityForMob(this, target, 1.0F)) {
            if (target instanceof EntityPlayer) {
                EventHandler.applyGravity((EntityPlayer) target, 80);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.slime_ball, 1);
        }
        for (int i = this.rand.nextInt(2); i-- > 0;) {
            this.dropItem(Items.experience_bottle, 1);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return true;
    }
}