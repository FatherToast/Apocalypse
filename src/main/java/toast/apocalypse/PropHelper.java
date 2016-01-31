package toast.apocalypse;

import java.util.HashSet;

import net.minecraft.entity.EntityList;

/**
 * Contains direct references to most properties for easier access.
 */
public class PropHelper {

    public static final int DAY_LENGTH = 24000;

    public static final HashSet<Class> BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.GENERAL, "blacklist"));
    public static final HashSet<Class> THUNDER_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.GENERAL, "thunderstorm_blacklist"));

    public static final HashSet<Class> ANCIENT_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.ANCIENT_GENERAL, "_blacklist"));

    public static final HashSet<Class> ENCHANTS_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.ENCHANTS, "_blacklist"));
    public static final HashSet<Class> ARMOR_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.ARMOR, "_blacklist"));
    public static final HashSet<Class> WEAPONS_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.WEAPONS, "_blacklist"));

    public static final HashSet<Class> POTIONS_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.POTIONS, "_blacklist"));
    public static final HashSet<Class> DAMAGE_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.DAMAGE, "_blacklist"));
    public static final HashSet<Class> KNOCKBACK_RESIST_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.KNOCKBACK_RESIST, "_blacklist"));
    public static final HashSet<Class> HEALTH_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.HEALTH, "_blacklist"));
    public static final HashSet<Class> SPEED_BLACKLIST = PropHelper.buildEntitySet(Properties.getString(Properties.SPEED, "_blacklist"));

    // General

    public static final double DIFFICULTY_PER_PERSON = Properties.getDouble(Properties.GENERAL, "smp_difficulty_mult");
    public static final float BREAK_SPEED = (float) Properties.getDouble(Properties.GENERAL, "break_speed");
    public static final long GRACE_PERIOD = (long) (Properties.getDouble(Properties.GENERAL, "grace_period") * PropHelper.DAY_LENGTH);
    public static final int RAIN_DAMAGE_TICKS = (int) (Properties.getDouble(Properties.GENERAL, "rain_damage_rate") * 20);
    public static final double DIMENSION_PENALTY = Properties.getDouble(Properties.GENERAL, "dimension_penalty");
    public static final double SLEEP_PENALTY = Properties.getDouble(Properties.GENERAL, "sleep_penalty");
    public static final double THUNDER_SPAWNING = Properties.getDouble(Properties.GENERAL, "thunderstorm_spawning");

    // Ancients

    public static final double ANCIENT_LUNAR_CHANCE = Properties.getDouble(Properties.ANCIENT_GENERAL, "_lunar_chance");
    public static final long ANCIENT_TIME = (long) (Properties.getDouble(Properties.ANCIENT_GENERAL, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double ANCIENT_CHANCE = Properties.getDouble(Properties.ANCIENT_GENERAL, "chance");
    public static final double ANCIENT_CHANCE_MAX = Properties.getDouble(Properties.ANCIENT_GENERAL, "chance_max");

    public static final double ANCIENT_DAMAGE_BONUS = Properties.getDouble(Properties.ANCIENT_ATTRIBUTES, "damage_flat_bonus");
    public static final double ANCIENT_DAMAGE_MULT = Properties.getDouble(Properties.ANCIENT_ATTRIBUTES, "damage_mult_bonus");
    public static final double ANCIENT_HEALTH_BONUS = Properties.getDouble(Properties.ANCIENT_ATTRIBUTES, "health_flat_bonus");
    public static final double ANCIENT_HEALTH_MULT = Properties.getDouble(Properties.ANCIENT_ATTRIBUTES, "health_mult_bonus");
    public static final double ANCIENT_KNOCKBACK_RESIST_BONUS = Properties.getDouble(Properties.ANCIENT_ATTRIBUTES, "resist_flat_bonus");
    public static final double ANCIENT_SPEED_MULT = Properties.getDouble(Properties.ANCIENT_ATTRIBUTES, "speed_mult_bonus");

    public static final boolean ANCIENT_FIRE_RESIST = Properties.getBoolean(Properties.ANCIENT_POTIONS, "fire_resistance");
    public static final int ANCIENT_REGEN = Properties.getInt(Properties.ANCIENT_POTIONS, "regeneration");
    public static final int ANCIENT_RESIST = Properties.getInt(Properties.ANCIENT_POTIONS, "resistance");
    public static final boolean ANCIENT_WATER_BREATH = Properties.getBoolean(Properties.ANCIENT_POTIONS, "water_breathing");

    // Equipment

    public static final double ENCHANTS_LUNAR_CHANCE = Properties.getDouble(Properties.ENCHANTS, "_lunar_chance");
    public static final double ENCHANTS_LUNAR_LEVEL = Properties.getDouble(Properties.ENCHANTS, "_lunar_level");
    public static final double ENCHANTS_STDEV = Properties.getDouble(Properties.ENCHANTS, "_standard_deviation");
    public static final long ENCHANTS_TIME = (long) (Properties.getDouble(Properties.ENCHANTS, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double ENCHANTS_CHANCE = Properties.getDouble(Properties.ENCHANTS, "chance");
    public static final double ENCHANTS_CHANCE_MAX = Properties.getDouble(Properties.ENCHANTS, "chance_max");
    public static final double ENCHANTS_LEVEL = Properties.getDouble(Properties.ENCHANTS, "level");
    public static final double ENCHANTS_LEVEL_MAX = Properties.getDouble(Properties.ENCHANTS, "level_max");

    public static final double ARMOR_LUNAR_CHANCE = Properties.getDouble(Properties.ARMOR, "_lunar_chance");
    public static final double ARMOR_LUNAR_EXTRA = Properties.getDouble(Properties.POTIONS, "_lunar_extra_chance");
    public static final double ARMOR_LUNAR_TIER = Properties.getDouble(Properties.ARMOR, "_lunar_tier");
    public static final long ARMOR_TIME = (long) (Properties.getDouble(Properties.ARMOR, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double ARMOR_CHANCE = Properties.getDouble(Properties.ARMOR, "chance");
    public static final double ARMOR_CHANCE_MAX = Properties.getDouble(Properties.ARMOR, "chance_max");
    public static final double ARMOR_EXTRA = Properties.getDouble(Properties.ARMOR, "extra_chance");
    public static final double ARMOR_EXTRA_MAX = Properties.getDouble(Properties.ARMOR, "extra_chance_max");
    public static final double ARMOR_TIER = Properties.getDouble(Properties.ARMOR, "tier");
    public static final double ARMOR_TIER_MAX = Properties.getDouble(Properties.ARMOR, "tier_max");

    public static final int[] WEAPON_WEIGHTS = {
    	Math.max(0, Properties.getInt(Properties.WEAPON_WEIGHTS, "sword")),
    	Math.max(0, Properties.getInt(Properties.WEAPON_WEIGHTS, "axe")),
    	Math.max(0, Properties.getInt(Properties.WEAPON_WEIGHTS, "pickaxe")),
    	Math.max(0, Properties.getInt(Properties.WEAPON_WEIGHTS, "shovel"))
    };
    public static final int WEAPON_WEIGHTS_TOTAL = PropHelper.buildTotalWeight(PropHelper.WEAPON_WEIGHTS);

    public static final double WEAPONS_LUNAR_CHANCE = Properties.getDouble(Properties.WEAPONS, "_lunar_chance");
    public static final double WEAPONS_LUNAR_TIER = Properties.getDouble(Properties.WEAPONS, "_lunar_tier");
    public static final long WEAPONS_TIME = (long) (Properties.getDouble(Properties.WEAPONS, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double WEAPONS_CHANCE = Properties.getDouble(Properties.WEAPONS, "chance");
    public static final double WEAPONS_CHANCE_MAX = Properties.getDouble(Properties.WEAPONS, "chance_max");
    public static final double WEAPONS_TIER = Properties.getDouble(Properties.WEAPONS, "tier");
    public static final double WEAPONS_TIER_MAX = Properties.getDouble(Properties.WEAPONS, "tier_max");

    // Full moons

    public static final double FULL_MOON_EVENT = Properties.getDouble(Properties.FULL_MOONS, "_full_moon_siege");
    public static final boolean FULL_MOON_SLEEP = Properties.getBoolean(Properties.FULL_MOONS, "_sleep_on_full_moon");
    public static final long FULL_MOON_TIME = (long) (Properties.getDouble(Properties.FULL_MOONS, "_time_span") * PropHelper.DAY_LENGTH);
    public static final long START_BREECHERS = (long) (Properties.getDouble(Properties.FULL_MOONS, "_start_breechers") * PropHelper.DAY_LENGTH);
    public static final int MOON_BREECHERS_MIN = Properties.getInt(Properties.FULL_MOONS, "min_breechers");
    public static final double MOON_BREECHERS = Properties.getDouble(Properties.FULL_MOONS, "max_breechers");
    public static final long START_GRUMPS = (long) (Properties.getDouble(Properties.FULL_MOONS, "_start_grumps") * PropHelper.DAY_LENGTH);
    public static final int MOON_GRUMPS_MIN = Properties.getInt(Properties.FULL_MOONS, "min_grumps");
    public static final double MOON_GRUMPS = Properties.getDouble(Properties.FULL_MOONS, "max_grumps");
    public static final long START_SEEKERS = (long) (Properties.getDouble(Properties.FULL_MOONS, "_start_seekers") * PropHelper.DAY_LENGTH);
    public static final int MOON_SEEKERS_MIN = Properties.getInt(Properties.FULL_MOONS, "min_seekers");
    public static final double MOON_SEEKERS = Properties.getDouble(Properties.FULL_MOONS, "max_seekers");
    public static final long START_GHOSTS = (long) (Properties.getDouble(Properties.FULL_MOONS, "_start_ghosts") * PropHelper.DAY_LENGTH);
    public static final int MOON_GHOSTS_MIN = Properties.getInt(Properties.FULL_MOONS, "min_ghosts");
    public static final double MOON_GHOSTS = Properties.getDouble(Properties.FULL_MOONS, "max_ghosts");
    public static final long START_DESTROYERS = (long) (Properties.getDouble(Properties.FULL_MOONS, "_start_destroyers") * PropHelper.DAY_LENGTH);
    public static final int MOON_DESTROYERS_MIN = Properties.getInt(Properties.FULL_MOONS, "min_destroyers");
    public static final double MOON_DESTROYERS = Properties.getDouble(Properties.FULL_MOONS, "max_destroyers");

    // Potions and attributes

    public static final double POTIONS_LUNAR_CHANCE = Properties.getDouble(Properties.POTIONS, "_lunar_chance");
    public static final double POTIONS_LUNAR_EXTRA = Properties.getDouble(Properties.POTIONS, "_lunar_extra_chance");
    public static final long POTIONS_TIME = (long) (Properties.getDouble(Properties.POTIONS, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double POTIONS_CHANCE = Properties.getDouble(Properties.POTIONS, "chance");
    public static final double POTIONS_CHANCE_MAX = Properties.getDouble(Properties.POTIONS, "chance_max");
    public static final double POTIONS_EXTRA = Properties.getDouble(Properties.POTIONS, "extra_chance");
    public static final double POTIONS_EXTRA_MAX = Properties.getDouble(Properties.POTIONS, "extra_chance_max");

    public static final double HEALTH_LUNAR_BONUS = Properties.getDouble(Properties.HEALTH, "_lunar_flat_bonus");
    public static final double HEALTH_LUNAR_MULT = Properties.getDouble(Properties.HEALTH, "_lunar_mult_bonus");
    public static final long HEALTH_TIME = (long) (Properties.getDouble(Properties.HEALTH, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double HEALTH_BONUS = Properties.getDouble(Properties.HEALTH, "flat_bonus");
    public static final double HEALTH_BONUS_MAX = Properties.getDouble(Properties.HEALTH, "flat_bonus_max");
    public static final double HEALTH_MULT = Properties.getDouble(Properties.HEALTH, "mult_bonus");
    public static final double HEALTH_MULT_MAX = Properties.getDouble(Properties.HEALTH, "mult_bonus_max");

    public static final double DAMAGE_LUNAR_BONUS = Properties.getDouble(Properties.DAMAGE, "_lunar_flat_bonus");
    public static final double DAMAGE_LUNAR_MULT = Properties.getDouble(Properties.DAMAGE, "_lunar_mult_bonus");
    public static final long DAMAGE_TIME = (long) (Properties.getDouble(Properties.DAMAGE, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double DAMAGE_BONUS = Properties.getDouble(Properties.DAMAGE, "flat_bonus");
    public static final double DAMAGE_BONUS_MAX = Properties.getDouble(Properties.DAMAGE, "flat_bonus_max");
    public static final double DAMAGE_MULT = Properties.getDouble(Properties.DAMAGE, "mult_bonus");
    public static final double DAMAGE_MULT_MAX = Properties.getDouble(Properties.DAMAGE, "mult_bonus_max");

    public static final double SPEED_LUNAR_MULT = Properties.getDouble(Properties.SPEED, "_lunar_mult_bonus");
    public static final long SPEED_TIME = (long) (Properties.getDouble(Properties.SPEED, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double SPEED_MULT = Properties.getDouble(Properties.SPEED, "mult_bonus");
    public static final double SPEED_MULT_MAX = Properties.getDouble(Properties.SPEED, "mult_bonus_max");

    public static final double KNOCKBACK_RESIST_LUNAR_BONUS = Properties.getDouble(Properties.KNOCKBACK_RESIST, "_lunar_flat_bonus");
    public static final long KNOCKBACK_RESIST_TIME = (long) (Properties.getDouble(Properties.KNOCKBACK_RESIST, "_time_span") * PropHelper.DAY_LENGTH);
    public static final double KNOCKBACK_RESIST_BONUS = Properties.getDouble(Properties.KNOCKBACK_RESIST, "flat_bonus");
    public static final double KNOCKBACK_RESIST_BONUS_MAX = Properties.getDouble(Properties.KNOCKBACK_RESIST, "flat_bonus_max");

    /**
     * Builds a set of entity classes from the supplied string.
     * @param string The string list of entity classes.
     * @return A HashSet containing all the entities specified in the string.
     */
    private static HashSet<Class> buildEntitySet(String string) {
        HashSet<Class> set = new HashSet<Class>();
        String[] entries = string.split(",");
        Class entity;
        for (String entry : entries) {
            entity = (Class) EntityList.stringToClassMapping.get(entry);
            if (entity != null) {
                set.add(entity);
            }
        }
        return set;
    }

    /** @param weights The individual weights.
     * @return The total weight of all entries in the weights array. */
    private static int buildTotalWeight(int[] weights) {
    	int totalWeight = 0;
    	for (int weight : weights) {
			totalWeight += weight;
		}
    	return totalWeight;
    }

    private PropHelper() {}
}
