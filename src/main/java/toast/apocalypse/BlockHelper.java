package toast.apocalypse;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

/**
 * Used to determine whether mobs can harvest blocks and how fast they can break them.
 */
public class BlockHelper {

    /** Returns true if the mob should detroy the block. */
    public static boolean shouldDamage(Block block, EntityLiving entity, boolean needsTool, World world, int x, int y, int z) {
        ItemStack held = entity.getHeldItem();
        int metadata = world.getBlockMetadata(x, y, z);
        return block.getBlockHardness(world, x, y, z) >= 0.0F && (!needsTool || block.getMaterial().isToolNotRequired() || held != null && ForgeHooks.canToolHarvestBlock(block, metadata, held));
    }

    /** Returns the amount of damage to deal to a block. */
    public static float getDamageAmount(Block block, EntityLiving entity, World world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x, y, z);
        float hardness = block.getBlockHardness(world, x, y, z);
        if (hardness < 0.0F)
            return 0.0F;

        if (!BlockHelper.canHarvestBlock(entity.getHeldItem(), block))
            return PropHelper.BREAK_SPEED / hardness / 100.0F;
        return BlockHelper.getCurrentStrengthVsBlock(entity, block, metadata) * PropHelper.BREAK_SPEED / hardness / 30.0F;
    }

    /** Returns whether the item can harvest the specified block. */
    public static boolean canHarvestBlock(ItemStack itemStack, Block block) {
        return block.getMaterial().isToolNotRequired() || itemStack != null && itemStack.func_150998_b(block);
    }

    /** Returns the mob's strength vs. the given block. */
    public static float getCurrentStrengthVsBlock(EntityLiving entity, Block block, int metadata) {
        ItemStack held = entity.getHeldItem();
        float strength = held == null ? 1.0F : held.getItem().getDigSpeed(held, block, metadata);

        if (strength > 1.0F) {
            int efficiency = EnchantmentHelper.getEfficiencyModifier(entity);
            if (efficiency > 0 && held != null) {
                strength += efficiency * efficiency + 1;
            }
        }

        if (entity.isPotionActive(Potion.digSpeed)) {
            strength *= 1.0F + (entity.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }
        if (entity.isPotionActive(Potion.digSlowdown)) {
            strength *= 1.0F - (entity.getActivePotionEffect(Potion.digSlowdown).getAmplifier() + 1) * 0.2F;
        }

        if (entity.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(entity)) {
            strength /= 5.0F;
        }
        if (!entity.onGround) {
            strength /= 5.0F;
        }

        return strength < 0.0F ? 0.0F : strength;
    }

    private BlockHelper() {}
}