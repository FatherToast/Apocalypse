package toast.apocalypse;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;

/**
 * Contains various helper methods used to set up equipment on mobs.
 */
public class EquipmentHelper {

	/** @param entity The entity to test.
	 * @return A random armor slot on the entity that is empty (1-4), or -1 if there are none. */
	public static int nextOpenArmorSlot(EntityLivingBase entity) {
		int[] slots = new int[4];
		int count = 0;
		for (int s = 5; s-- > 1;) {
			if (entity.getEquipmentInSlot(s) == null) {
				slots[count++] = s;
			}
		}
		if (count > 0)
			return slots[entity.getRNG().nextInt(count)];
		return -1;
	}

	/** @param entity The entity to generate a weapon for.
	 * @return A random weapon type (0-3) based on the weights in the config. */
	public static int nextWeaponType(EntityLivingBase entity) {
    	int type = 0;
    	if (PropHelper.WEAPON_WEIGHTS_TOTAL > 0) {
    		int choice = entity.getRNG().nextInt(PropHelper.WEAPON_WEIGHTS_TOTAL);
    		for (int weight : PropHelper.WEAPON_WEIGHTS) {
    			if ((choice -= weight) < 0) {
					break;
				}
    			type++;
    		}
			if (type > 3) {
				ApocalypseMod.logError("Invalid weapon weights!");
				type = 0;
			}
    	}
		return type;
	}

	/** @param entity The entity to apply a potion to.
	 * @param potion The potion effect to apply. Will apply even if it is not applicable. */
	public static void forcePotion(EntityLivingBase entity, PotionEffect potion) {
        if (!entity.isPotionApplicable(potion)) {
            // Force apply potion
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            if (!tag.hasKey("ActiveEffects")) {
                tag.setTag("ActiveEffects", new NBTTagList());
            }
            tag.getTagList("ActiveEffects", tag.getId()).appendTag(potion.writeCustomPotionEffectToNBT(new NBTTagCompound()));
            entity.readFromNBT(tag);
        }
        else {
            entity.addPotionEffect(potion);
        }
	}

	/** @param itemStack The item to enchant or modify.
	 * @param enchantment The enchantment to apply.
	 * @param level The level to apply the enchantment at (or to raise the existing enchantment to).
	 * @param insert If true, the tag will be inserted at the start of the list. */
	public static void forceEnchant(ItemStack itemStack, Enchantment enchantment, int level, boolean insert) {
        if (!itemStack.stackTagCompound.hasKey("ench")) {
        	itemStack.stackTagCompound.setTag("ench", new NBTTagList());
        }
        NBTTagList enchList = itemStack.getEnchantmentTagList();
        NBTTagCompound enchTag;
        int tagCount = enchList.tagCount();
        for (int i = tagCount; i-- > 0;) {
            enchTag = enchList.getCompoundTagAt(i);
            if (enchTag.getShort("id") == enchantment.effectId) {
                enchTag.setShort("lvl", (byte) Math.max(level, enchTag.getShort("lvl")));
                if (insert && tagCount > 1) {
                	enchList.removeTag(i);
                	enchList.func_150304_a(0, enchTag); // insertTag
                }
                return;
            }
        }
        enchTag = new NBTTagCompound();
        enchTag.setShort("id", (short) enchantment.effectId);
        enchTag.setShort("lvl", (byte) level);
        if (insert && tagCount > 0) {
        	enchList.func_150304_a(0, enchTag); // insertTag
        }
        else {
			enchList.appendTag(enchTag);
		}
	}

	/** All armor stored by tier.<br>
	 * The first index is the type (0-3) in the order: boots, leggings, chestplate, helmet.<br>
	 * The second index is the tier (0-3) in the order: leather, chain, iron, diamond. */
	public static final Item[][] ARMOR_TIERS = {
		{ Items.leather_boots, Items.chainmail_boots, Items.iron_boots, Items.diamond_boots },
		{ Items.leather_leggings, Items.chainmail_leggings, Items.iron_leggings, Items.diamond_leggings },
		{ Items.leather_chestplate, Items.chainmail_chestplate, Items.iron_chestplate, Items.diamond_chestplate },
		{ Items.leather_helmet, Items.chainmail_helmet, Items.iron_helmet, Items.diamond_helmet }
	};
	/** All weapons stored by tier.<br>
	 * The first index is the type (0-3) in the order: sword, axe, pickaxe, shovel.<br>
	 * The second index is the tier (0-3) in the order: wood, stone, iron, diamond. */
	public static final Item[][] WEAPON_TIERS = {
		{ Items.wooden_sword, Items.stone_sword, Items.iron_sword, Items.diamond_sword },
		{ Items.wooden_axe, Items.stone_axe, Items.iron_axe, Items.diamond_axe },
		{ Items.wooden_pickaxe, Items.stone_pickaxe, Items.iron_pickaxe, Items.diamond_pickaxe },
		{ Items.wooden_shovel, Items.stone_shovel, Items.iron_shovel, Items.diamond_shovel }
	};

	/** All ancient armor.<br>
	 * The index is the type (0-3) in the order: boots, leggings, chestplate, helmet. */
	public static final Item[] ANCIENT_ARMOR = {
		Items.golden_boots, Items.golden_leggings, Items.golden_chestplate, Items.golden_helmet
	};
	/** All ancient weapons.<br>
	 * The index is the type (0-3) in the order: sword, axe, pickaxe, shovel. */
	public static final Item[] ANCIENT_WEAPONS = {
		Items.golden_sword, Items.golden_axe, Items.golden_pickaxe, Items.golden_shovel
	};
    /** Array of all useful enchantments for each equipment type.<br>
	 * The index is the type (0-2) in the order: armor, weapon, bow. */
    public static final Enchantment[][] ANCIENT_ENCHANTS = {
		{ Enchantment.protection, Enchantment.fireProtection, Enchantment.featherFalling, Enchantment.blastProtection, Enchantment.projectileProtection, Enchantment.respiration, Enchantment.aquaAffinity, Enchantment.thorns },
		{ Enchantment.sharpness, Enchantment.smite, Enchantment.baneOfArthropods, Enchantment.knockback, Enchantment.fireAspect, Enchantment.looting, Enchantment.efficiency, Enchantment.silkTouch, Enchantment.fortune },
		{ Enchantment.looting, Enchantment.power, Enchantment.punch, Enchantment.flame, Enchantment.infinity }
	};

	private EquipmentHelper() {}
}
