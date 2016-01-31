package toast.apocalypse;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

/**
 * Contains various helper methods common to many mobs.
 */
public class MobHelper {

    /**
     * Checks the area around a mob to see if it can spawn.
     * @param entity The entity to check around.
     * @return True if the area is valid for spawning.
     */
    public static boolean canSpawn(EntityLiving entity) {
        return entity.worldObj.checkNoEntityCollision(entity.boundingBox) && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty();
    }

    /**
     * External implementation of EntityLivingBase.attackEntityAsMob(Entity) for the mobs in this mod with optional minimum damage.
     *
     * @param attacker The attacking entity.
     * @param target The entity being attacked.
     * @param minDamage The minimum damage that will be dealt (will not apply if the attack fails).
     * @return True if the attack is successful.
     *
     * @see EntityLivingBase#attackEntityFrom(DamageSource, float)
     */
    public static boolean attackEntityForMob(EntityLiving attacker, Entity target, float minDamage) {
        EntityLivingBase livingTarget;
        float prevHealth;
        float damage;
        int knockback;
        try {
            damage = (float) attacker.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        }
        catch (Exception ex) {
            damage = 4.0F;
        }
        if (target instanceof EntityLivingBase) {
            livingTarget = (EntityLivingBase) target;
            prevHealth = livingTarget.getHealth() + livingTarget.getAbsorptionAmount();
            damage += EnchantmentHelper.getEnchantmentModifierLiving(attacker, livingTarget);
            knockback = EnchantmentHelper.getKnockbackModifier(attacker, livingTarget);
        }
        else {
            livingTarget = null;
            prevHealth = 0.0F;
            knockback = 0;
        }

        if (target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage)) {
            if (knockback > 0) {
                target.addVelocity(-MathHelper.sin(attacker.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F, 0.1, MathHelper.cos(attacker.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F);
                attacker.motionX *= 0.6;
                attacker.motionZ *= 0.6;
            }

            int fire = EnchantmentHelper.getFireAspectModifier(attacker) << 2;
            if (attacker.isBurning()) {
                fire += 2;
            }
            if (fire > 0) {
                target.setFire(fire);
            }

            if (target instanceof EntityLivingBase) {
                EnchantmentHelper.func_151384_a((EntityLivingBase) target, attacker); // Triggers hit entity's enchants
            }
            EnchantmentHelper.func_151385_b(attacker, target); // Triggers attacker's enchants

            // Enforce minimum damage limit
            if (minDamage > 0.0F && livingTarget != null) {
                float remainingDamage = livingTarget.getHealth() + livingTarget.getAbsorptionAmount() + minDamage - prevHealth;
                if (remainingDamage > 0.0F) {
                    if (livingTarget.getAbsorptionAmount() >= remainingDamage) {
                        livingTarget.setAbsorptionAmount(livingTarget.getAbsorptionAmount() - remainingDamage);
                    }
                    else {
                        if (livingTarget.getAbsorptionAmount() > 0.0F) {
                            remainingDamage -= livingTarget.getAbsorptionAmount();
                            livingTarget.setAbsorptionAmount(0.0F);
                        }
                        livingTarget.setHealth(livingTarget.getHealth() - remainingDamage);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private MobHelper() {}
}
