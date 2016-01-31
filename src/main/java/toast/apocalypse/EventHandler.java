package toast.apocalypse;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Used for handling any needed game events, such as game ticks and mob spawns.
 */
public class EventHandler {

    /** Array of possible potion effects. */
    public static final Potion[] ALLOWED_POTIONS = {
        Potion.damageBoost, Potion.fireResistance, Potion.moveSpeed, Potion.regeneration, Potion.resistance, Potion.waterBreathing
    };
    /** Max level for each potion. */
    public static final int[] POTION_LEVELS = {
        0, 0, 2, 2, 1, 0
    };

    /**
     * Applies the gravity effect.
     * @param player The player to apply gravity to.
     * @param duration The duration to apply it for (in ticks).
     */
    public static void applyGravity(EntityPlayer player, int duration) {
        NBTTagCompound data = WorldDifficultyManager.getData(player);
        data.setInteger(WorldDifficultyManager.TAG_GRAVITY, duration);
    }

    /**
     * Called for each player every 5 ticks on the server side.
     * @param player The player to update.
     * @param data The player's Apocalypse save data.
     */
    public static void onPlayerUpdate(EntityPlayer player, NBTTagCompound data) {
        // Tick rain damage
        if (PropHelper.RAIN_DAMAGE_TICKS > 0 && !EnchantmentHelper.getAquaAffinityModifier(player) && player.worldObj.canLightningStrikeAt((int) Math.floor(player.posX), (int) Math.floor(player.posY + player.height), (int) Math.floor(player.posZ))) {
            int rainTime = data.getInteger(WorldDifficultyManager.TAG_RAIN) + WorldDifficultyManager.TICKS_PER_UPDATE;
            if (rainTime >= PropHelper.RAIN_DAMAGE_TICKS) {
                data.removeTag(WorldDifficultyManager.TAG_RAIN);
                float damage = 1.0F;
                ItemStack helmet = player.getEquipmentInSlot(4);
                if (helmet != null) {
                    if (helmet.getItem() == ApocalypseMod.bucketHelm) {
                        damage = 0.0F;
                    }
                    else {
                        damage *= 0.5F;
                    }
                    if (helmet.isItemStackDamageable()) {
                        helmet.setItemDamage(helmet.getItemDamageForDisplay() + player.getRNG().nextInt(2));
                        if (helmet.getItemDamageForDisplay() >= helmet.getMaxDamage()) {
                            player.renderBrokenItemStack(helmet);
                            player.setCurrentItemOrArmor(4, (ItemStack) null);
                        }
                    }
                }
                if (damage > 0.0F) {
                    player.attackEntityFrom(WorldDifficultyManager.RAIN_DAMAGE, damage);
                }
            }
            else {
                data.setInteger(WorldDifficultyManager.TAG_RAIN, rainTime);
            }
        }
        // Tick gravity effect
        int gravityTime = data.getInteger(WorldDifficultyManager.TAG_GRAVITY);
        if (gravityTime > 0) {
            gravityTime -= WorldDifficultyManager.TICKS_PER_UPDATE;
            if (gravityTime <= 0) {
                data.removeTag(WorldDifficultyManager.TAG_GRAVITY);
            }
            else {
                data.setInteger(WorldDifficultyManager.TAG_GRAVITY, gravityTime);
            }

            if (!player.onGround) {
                player.motionY -= 0.41;
                if (player instanceof EntityPlayerMP) {
                    try {
                        ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(player));
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /** Initializes an entity based on the world difficulty.
     * @param entity The entity to initialize. */
    public static void initializeEntity(EntityLivingBase entity) {
        Class entityClass = entity.getClass();
        long worldDifficulty = WorldDifficultyManager.getWorldDifficulty();
        if (worldDifficulty <= 0L)
            return;
        boolean fullMoon = WorldDifficultyManager.isFullMoon(entity.worldObj);

        EventHandler.initializeAncient(entity, entityClass, worldDifficulty, fullMoon);
        EventHandler.initializeEquipment(entity, entityClass, worldDifficulty, fullMoon);
        EventHandler.initializePotions(entity, entityClass, worldDifficulty, fullMoon);
        EventHandler.initializeAttributes(entity, entityClass, worldDifficulty, fullMoon);
    }

	/** Constructs an EventHandler that automatically registers itself to recieve and handle events. */
    public EventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Called by SpawnerAnimals.findChunksForSpawning().
     * EntityLivingBase entityLiving = the entity potentially being spawned.
     * World world = the world being spawned into.
     * float x = the spawn x coord.
     * float y = the spawn y coord.
     * float z = the spawn z coord.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (event.world.difficultySetting != EnumDifficulty.PEACEFUL && !PropHelper.THUNDER_BLACKLIST.contains(event.entityLiving.getClass()) && event.world.isThundering() && event.world.rand.nextDouble() < PropHelper.THUNDER_SPAWNING) {
            if (event.world.checkNoEntityCollision(event.entityLiving.boundingBox) && event.world.getCollidingBoundingBoxes(event.entityLiving, event.entityLiving.boundingBox).isEmpty() && !event.world.isAnyLiquid(event.entityLiving.boundingBox)) {
                event.setResult(Result.ALLOW);
            }
        }
    }

    /**
     * Called by World.spawnEntityInWorld().
     * Entity entity = the entity being spawned.
     * World world = the world being spawned into.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.world.isRemote && event.entity instanceof EntityLivingBase && !(event.entity instanceof EntityPlayer) && !PropHelper.BLACKLIST.contains(event.entity.getClass())) {
            WorldDifficultyManager.markForInit((EntityLivingBase) event.entity);
        }
    }

    /**
     * Called by EntityPlayer.sleepInBedAt().
     * EntityPlayer entityPlayer = the player trying to sleep.
     * EntityPlayer.EnumStatus result = the sleep status to force.
     * int x = the bed's x coord.
     * int y = the bed's y coord.
     * int z = the bed's z coord.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        if (PropHelper.FULL_MOON_SLEEP || event.entityPlayer.isPlayerSleeping() || !event.entityPlayer.isEntityAlive() || !event.entityPlayer.worldObj.provider.isSurfaceWorld() || event.entityPlayer.worldObj.isDaytime())
            return;
        if (WorldDifficultyManager.isFullMoon(event.entityPlayer.worldObj)) {
            event.result = EntityPlayer.EnumStatus.OTHER_PROBLEM;
            event.entityPlayer.addChatComponentMessage(new ChatComponentTranslation("tile.bed.Apocalypse.fullMoon", new Object[0]));
        }
    }

    /** @param entity The entity to initialize equipment for.
	 * @param entityClass The class of the entity.
	 * @param worldDifficulty The current world difficulty.
	 * @param fullMoon True if there is currently a full moon. */
	private static void initializeAncient(EntityLivingBase entity, Class entityClass, long worldDifficulty, boolean fullMoon) {
        double effectiveDifficulty;
        double bonus;
        IAttributeInstance attribute;

        if (!PropHelper.ANCIENT_BLACKLIST.contains(entityClass)) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.ANCIENT_TIME;
            bonus = PropHelper.ANCIENT_CHANCE * effectiveDifficulty;
            if (PropHelper.ANCIENT_CHANCE_MAX >= 0.0 && bonus > PropHelper.ANCIENT_CHANCE_MAX) {
                bonus = PropHelper.ANCIENT_CHANCE_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.ANCIENT_LUNAR_CHANCE;
            }
            if (entity.getRNG().nextDouble() < bonus) {
            	// Apply potions
                if (PropHelper.ANCIENT_REGEN >= 0) {
                    EquipmentHelper.forcePotion(entity, new PotionEffect(Potion.regeneration.id, Integer.MAX_VALUE, PropHelper.ANCIENT_REGEN, true));
                }
                if (PropHelper.ANCIENT_RESIST >= 0) {
                    EquipmentHelper.forcePotion(entity, new PotionEffect(Potion.resistance.id, Integer.MAX_VALUE, PropHelper.ANCIENT_RESIST, true));
                }
                if (PropHelper.ANCIENT_FIRE_RESIST) {
                	EquipmentHelper.forcePotion(entity, new PotionEffect(Potion.fireResistance.id, Integer.MAX_VALUE, 0, true));
                }
                if (PropHelper.ANCIENT_WATER_BREATH) {
                	EquipmentHelper.forcePotion(entity, new PotionEffect(Potion.waterBreathing.id, Integer.MAX_VALUE, 0, true));
                }

                // Apply attributes
                attribute = entity.getEntityAttribute(SharedMonsterAttributes.attackDamage);
                if (attribute != null) {
                    if (PropHelper.ANCIENT_DAMAGE_BONUS != 0.0) {
                        attribute.applyModifier(new AttributeModifier("AncientFlatDAMAGE", PropHelper.ANCIENT_DAMAGE_BONUS, 0));
                    }
                    if (PropHelper.ANCIENT_DAMAGE_MULT != 0.0) {
                        attribute.applyModifier(new AttributeModifier("AncientMultDAMAGE", PropHelper.ANCIENT_DAMAGE_MULT, 1));
                    }
                }
                attribute = entity.getEntityAttribute(SharedMonsterAttributes.maxHealth);
                if (attribute != null) {
                    if (PropHelper.ANCIENT_HEALTH_BONUS != 0.0) {
                        attribute.applyModifier(new AttributeModifier("AncientFlatHEALTH", PropHelper.ANCIENT_HEALTH_BONUS, 0));
                    }
                    if (PropHelper.ANCIENT_HEALTH_MULT != 0.0) {
                        attribute.applyModifier(new AttributeModifier("AncientMultHEALTH", PropHelper.ANCIENT_HEALTH_MULT, 1));
                    }
                }
                attribute = entity.getEntityAttribute(SharedMonsterAttributes.knockbackResistance);
                if (attribute != null) {
                    if (PropHelper.ANCIENT_KNOCKBACK_RESIST_BONUS != 0.0) {
                        attribute.applyModifier(new AttributeModifier("AncientFlatRESIST", PropHelper.ANCIENT_KNOCKBACK_RESIST_BONUS, 0));
                    }
                }
                attribute = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
                if (attribute != null) {
                    if (PropHelper.ANCIENT_SPEED_MULT != 0.0) {
                        attribute.applyModifier(new AttributeModifier("AncientMultSPEED", PropHelper.ANCIENT_SPEED_MULT, 1));
                    }
                }
                entity.setHealth(entity.getMaxHealth());
                String name = NameHelper.setEntityName(entity.getRNG(), entity);

                // Apply normal equipment
                if (entity.getEquipmentInSlot(0) == null || !(entity.getEquipmentInSlot(0).getItem() instanceof ItemBow)) {
            		entity.setCurrentItemOrArmor(0, new ItemStack(EquipmentHelper.ANCIENT_WEAPONS[EquipmentHelper.nextWeaponType(entity)]));
                }
            	for (int slot = 5; slot-- > 1;) {
            		entity.setCurrentItemOrArmor(slot, new ItemStack(EquipmentHelper.ANCIENT_ARMOR[slot - 1]));
            	}

            	// Make one piece unique
            	int uniqueSlot = entity.getRNG().nextBoolean() ? 0 : entity.getRNG().nextInt(4) + 1;
        		ItemStack uniqueItem = entity.getEquipmentInSlot(uniqueSlot);
        		if (uniqueItem != null) {
	        		if (entity instanceof EntityLiving) {
	        			((EntityLiving) entity).setEquipmentDropChance(uniqueSlot, 2.0F);
	        		}

	                int enchantType = uniqueSlot > 0 ? 0 : uniqueItem.getItem() instanceof ItemBow ? 2 : 1;
	                Enchantment enchantment = EquipmentHelper.ANCIENT_ENCHANTS[enchantType][entity.getRNG().nextInt(EquipmentHelper.ANCIENT_ENCHANTS[enchantType].length)];
	                NameHelper.setItemName(entity.getRNG(), uniqueItem, name, enchantment);

	                try {
	                	EnchantmentHelper.addRandomEnchantment(entity.getRNG(), uniqueItem, 30);
	                }
	                catch (Exception ex) {
	            		ApocalypseMod.logDebug("Error applying enchantments to unique item! slot:" + uniqueSlot + " entity:" + entity.toString());
	                }
	                EquipmentHelper.forceEnchant(uniqueItem, enchantment, enchantment.getMaxLevel() + 1, true);
	                EquipmentHelper.forceEnchant(uniqueItem, Enchantment.unbreaking, enchantType == 1 ? 5 : 3, false);
        		}
            }
        }
	}

    /** @param entity The entity to initialize equipment for.
	 * @param entityClass The class of the entity.
	 * @param worldDifficulty The current world difficulty.
	 * @param fullMoon True if there is currently a full moon. */
	private static void initializeEquipment(EntityLivingBase entity, Class entityClass, long worldDifficulty, boolean fullMoon) {
        double effectiveDifficulty;
        double bonus, mult, level;

        boolean[] equipped = new boolean[5];

        if (!PropHelper.ARMOR_BLACKLIST.contains(entityClass)) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.ARMOR_TIME;
            bonus = PropHelper.ARMOR_CHANCE * effectiveDifficulty;
            mult = PropHelper.ARMOR_EXTRA * effectiveDifficulty;
            level = PropHelper.ARMOR_TIER * effectiveDifficulty;
            if (PropHelper.ARMOR_CHANCE_MAX >= 0.0 && bonus > PropHelper.ARMOR_CHANCE_MAX) {
                bonus = PropHelper.ARMOR_CHANCE_MAX;
            }
            if (PropHelper.ARMOR_EXTRA_MAX >= 0.0 && mult > PropHelper.ARMOR_EXTRA_MAX) {
                mult = PropHelper.ARMOR_EXTRA_MAX;
            }
            if (PropHelper.ARMOR_TIER_MAX >= 0.0 && level > PropHelper.ARMOR_TIER_MAX) {
                level = PropHelper.ARMOR_TIER_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.ARMOR_LUNAR_CHANCE;
                mult += PropHelper.ARMOR_LUNAR_EXTRA;
                level += PropHelper.ARMOR_LUNAR_TIER;
            }
            if (entity.getRNG().nextDouble() < bonus) {
            	int tier = 0;
            	for (int i = 3; i-- > 0;) if (entity.getRNG().nextDouble() < level) {
            		tier++;
            	}

            	int slot;
            	do {
            		slot = EquipmentHelper.nextOpenArmorSlot(entity);
            		if (slot < 0) {
						break;
					}
            		entity.setCurrentItemOrArmor(slot, new ItemStack(EquipmentHelper.ARMOR_TIERS[slot - 1][tier]));
            		equipped[slot] = true;
            	}
            	while (entity.getRNG().nextDouble() < mult);
            }
        }

        if (!PropHelper.WEAPONS_BLACKLIST.contains(entityClass) && entity.getEquipmentInSlot(0) == null) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.WEAPONS_TIME;
            bonus = PropHelper.WEAPONS_CHANCE * effectiveDifficulty;
            level = PropHelper.WEAPONS_TIER * effectiveDifficulty;
            if (PropHelper.WEAPONS_CHANCE_MAX >= 0.0 && bonus > PropHelper.WEAPONS_CHANCE_MAX) {
                bonus = PropHelper.WEAPONS_CHANCE_MAX;
            }
            if (PropHelper.WEAPONS_TIER_MAX >= 0.0 && level > PropHelper.WEAPONS_TIER_MAX) {
                level = PropHelper.WEAPONS_TIER_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.WEAPONS_LUNAR_CHANCE;
                level += PropHelper.WEAPONS_LUNAR_TIER;
            }
            if (entity.getRNG().nextDouble() < bonus) {
            	int tier = 0;
            	for (int i = 3; i-- > 0;) if (entity.getRNG().nextDouble() < level) {
            		tier++;
            	}

            	entity.setCurrentItemOrArmor(0, new ItemStack(EquipmentHelper.WEAPON_TIERS[EquipmentHelper.nextWeaponType(entity)][tier]));
            	equipped[0] = true;
            }
        }

        if (!PropHelper.ENCHANTS_BLACKLIST.contains(entityClass)) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.ENCHANTS_TIME;
            bonus = PropHelper.ENCHANTS_CHANCE * effectiveDifficulty;
            level = PropHelper.ENCHANTS_LEVEL * effectiveDifficulty;
            if (PropHelper.ENCHANTS_CHANCE_MAX >= 0.0 && bonus > PropHelper.ENCHANTS_CHANCE_MAX) {
                bonus = PropHelper.ENCHANTS_CHANCE_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.ENCHANTS_LUNAR_CHANCE;
                level += PropHelper.ENCHANTS_LUNAR_LEVEL;
            }
            if (PropHelper.ENCHANTS_LEVEL_MAX >= 0.0 && level > PropHelper.ENCHANTS_LEVEL_MAX) {
                level = PropHelper.ENCHANTS_LEVEL_MAX;
            }
            for (int slot = equipped.length; slot-- > 0;) {
	            if ((equipped[slot] || entity.getEquipmentInSlot(slot) != null && entity.getEquipmentInSlot(slot).getItem() instanceof ItemBow) && entity.getRNG().nextDouble() < bonus) {
	            	try {
	            		EnchantmentHelper.addRandomEnchantment(entity.getRNG(), entity.getEquipmentInSlot(slot), Math.max(0, (int) Math.round(level + entity.getRNG().nextGaussian() * PropHelper.ENCHANTS_STDEV)));
	            	}
	            	catch (Exception ex) {
	            		ApocalypseMod.logDebug("Error applying enchantments! slot:" + slot + " entity:" + entity.toString());
	            	}
	            }
            }
        }
	}

    /** @param entity The entity to initialize equipment for.
	 * @param entityClass The class of the entity.
	 * @param worldDifficulty The current world difficulty.
	 * @param fullMoon True if there is currently a full moon. */
	private static void initializePotions(EntityLivingBase entity, Class entityClass, long worldDifficulty, boolean fullMoon) {
        double effectiveDifficulty;
        double bonus, mult;

        if (!PropHelper.POTIONS_BLACKLIST.contains(entityClass)) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.POTIONS_TIME;
            bonus = PropHelper.POTIONS_CHANCE * effectiveDifficulty;
            if (PropHelper.POTIONS_CHANCE_MAX >= 0.0 && bonus > PropHelper.POTIONS_CHANCE_MAX) {
                bonus = PropHelper.POTIONS_CHANCE_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.POTIONS_LUNAR_CHANCE;
            }
            if (entity.getRNG().nextDouble() < bonus) {
                mult = PropHelper.POTIONS_EXTRA * effectiveDifficulty;
                if (PropHelper.POTIONS_EXTRA_MAX >= 0.0 && bonus > PropHelper.POTIONS_EXTRA_MAX) {
                    mult = PropHelper.POTIONS_EXTRA_MAX;
                }
                if (fullMoon) {
                    mult += PropHelper.POTIONS_LUNAR_EXTRA;
                }

                int count = 1;
                for (int i = 2; i-- > 0;) {
                    if (entity.getRNG().nextDouble() < mult) {
                        count++;
                    }
                }

                int potionIndex;
                PotionEffect potion;
                while (count-- > 0) {
                    potionIndex = entity.getRNG().nextInt(EventHandler.ALLOWED_POTIONS.length);
                    potion = new PotionEffect(EventHandler.ALLOWED_POTIONS[potionIndex].id, Integer.MAX_VALUE, EventHandler.POTION_LEVELS[potionIndex] > 0 ? entity.getRNG().nextInt(EventHandler.POTION_LEVELS[potionIndex]) : 0, false);
                    EquipmentHelper.forcePotion(entity, potion);
                }
            }
        }
	}

    /** @param entity The entity to initialize equipment for.
	 * @param entityClass The class of the entity.
	 * @param worldDifficulty The current world difficulty.
	 * @param fullMoon True if there is currently a full moon. */
	private static void initializeAttributes(EntityLivingBase entity, Class entityClass, long worldDifficulty, boolean fullMoon) {
        double effectiveDifficulty;
        double bonus, mult;
        IAttributeInstance attribute;

        attribute = entity.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        if (attribute != null && !PropHelper.HEALTH_BLACKLIST.contains(entityClass)) {
            float prevMax = entity.getMaxHealth();
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.HEALTH_TIME;

            bonus = PropHelper.HEALTH_BONUS * effectiveDifficulty;
            mult = PropHelper.HEALTH_MULT * effectiveDifficulty;
            if (PropHelper.HEALTH_BONUS_MAX >= 0.0 && bonus > PropHelper.HEALTH_BONUS_MAX) {
                bonus = PropHelper.HEALTH_BONUS_MAX;
            }
            if (PropHelper.HEALTH_MULT_MAX >= 0.0 && mult > PropHelper.HEALTH_MULT_MAX) {
                mult = PropHelper.HEALTH_MULT_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.HEALTH_LUNAR_BONUS;
                mult += PropHelper.HEALTH_LUNAR_MULT;
            }

            if (bonus != 0.0) {
                attribute.applyModifier(new AttributeModifier("ApocalypseFlatHEALTH", bonus, 0));
            }
            if (mult != 0.0) {
                attribute.applyModifier(new AttributeModifier("ApocalypseMultHEALTH", mult, 1));
            }

            entity.setHealth(entity.getHealth() + entity.getMaxHealth() - prevMax);
        }

        attribute = entity.getEntityAttribute(SharedMonsterAttributes.attackDamage);
        if (attribute != null && !PropHelper.DAMAGE_BLACKLIST.contains(entityClass)) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.DAMAGE_TIME;

            bonus = PropHelper.DAMAGE_BONUS * effectiveDifficulty;
            mult = PropHelper.DAMAGE_MULT * effectiveDifficulty;
            if (PropHelper.DAMAGE_BONUS_MAX >= 0.0 && bonus > PropHelper.DAMAGE_BONUS_MAX) {
                bonus = PropHelper.DAMAGE_BONUS_MAX;
            }
            if (PropHelper.DAMAGE_MULT_MAX >= 0.0 && mult > PropHelper.DAMAGE_MULT_MAX) {
                mult = PropHelper.DAMAGE_MULT_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.DAMAGE_LUNAR_BONUS;
                mult += PropHelper.DAMAGE_LUNAR_MULT;
            }

            if (bonus != 0.0) {
                attribute.applyModifier(new AttributeModifier("ApocalypseFlatDAMAGE", bonus, 0));
            }
            if (mult != 0.0) {
                attribute.applyModifier(new AttributeModifier("ApocalypseMultDAMAGE", mult, 1));
            }
        }

        attribute = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (attribute != null && !PropHelper.SPEED_BLACKLIST.contains(entityClass)) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.SPEED_TIME;

            mult = PropHelper.SPEED_MULT * effectiveDifficulty;
            if (PropHelper.SPEED_MULT_MAX >= 0.0 && mult > PropHelper.SPEED_MULT_MAX) {
                mult = PropHelper.SPEED_MULT_MAX;
            }
            if (fullMoon) {
                mult += PropHelper.SPEED_LUNAR_MULT;
            }

            if (mult != 0.0) {
                attribute.applyModifier(new AttributeModifier("ApocalypseMultSPEED", mult, 1));
            }
        }

        attribute = entity.getEntityAttribute(SharedMonsterAttributes.knockbackResistance);
        if (attribute != null && !PropHelper.KNOCKBACK_RESIST_BLACKLIST.contains(entityClass)) {
            effectiveDifficulty = (double) worldDifficulty / (double) PropHelper.KNOCKBACK_RESIST_TIME;

            bonus = PropHelper.KNOCKBACK_RESIST_BONUS * effectiveDifficulty;
            if (PropHelper.KNOCKBACK_RESIST_BONUS_MAX >= 0.0 && bonus > PropHelper.KNOCKBACK_RESIST_BONUS_MAX) {
                bonus = PropHelper.KNOCKBACK_RESIST_BONUS_MAX;
            }
            if (fullMoon) {
                bonus += PropHelper.KNOCKBACK_RESIST_LUNAR_BONUS;
            }

            if (bonus != 0.0) {
                attribute.applyModifier(new AttributeModifier("ApocalypseFlatRESIST", bonus, 0));
            }
        }
	}
}