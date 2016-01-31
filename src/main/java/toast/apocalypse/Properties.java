package toast.apocalypse;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

import net.minecraftforge.common.config.Configuration;

/**
 * This helper class automatically creates, stores, and retrieves properties.
 * Supported data types:
 * String, boolean, int, double
 *
 * Any property can be retrieved as an Object or String.
 * Any non-String property can also be retrieved as any other non-String property.
 * Retrieving a number as a boolean will produce a randomized output depending on the value.
 */
public abstract class Properties {

    /** Mapping of all properties in the mod to their values. */
    private static final HashMap<String, Object> map = new HashMap();

    // Common category names.
    public static final String GENERAL = "_general";
    public static final String DISPLAY = "gui_display";

    public static final String ANCIENT_GENERAL = "ancient__general";
    public static final String ANCIENT_ATTRIBUTES = "ancient_attibutes";
    public static final String ANCIENT_POTIONS = "ancient_potions";

    public static final String ENCHANTS = "_enchantments";
    public static final String ARMOR = "armor";
    public static final String WEAPON_WEIGHTS = "weapon_weights";
    public static final String WEAPONS = "weapons";

    public static final String FULL_MOONS = "full_moons";

    public static final String POTIONS = "_potion_effects";
    public static final String DAMAGE = "attack_damage";
    public static final String KNOCKBACK_RESIST = "knockback_resistance";
    public static final String HEALTH = "max_health";
    public static final String SPEED = "movement_speed";

    /** Initializes these properties. */
    public static void init(File configDir) {
    	String fileExt = ".cfg";
    	Configuration config;

        config = new Configuration(new File(configDir, ApocalypseMod.MODID + fileExt));
        config.load();

        Properties.add(config, Properties.GENERAL, "blacklist", "EnderDragon,Wolf,SnowMan,Ozelot,VillagerGolem,EntityHorse", "Comma-separated list of string entity ids that will not gain any bonuses from this mod. Default is EnderDragon,Wolf,SnowMan,Ozelot,VillagerGolem,EntityHorse.");
        Properties.add(config, Properties.GENERAL, "break_speed", 0.3, "(0.0-INFINITY) The block breaking speed multiplier for mobs, relative to the player's block breaking speed. Default is 30% speed.");
        Properties.add(config, Properties.GENERAL, "debug", false, "If true, the mod will load in debug mode. Default is false.");
        Properties.add(config, Properties.GENERAL, "dimension_penalty", 0.2, "The percent increase in difficulty rate while at least one player is in a dimension other than the overworld. Default is 0.2 (+20% difficulty rate).");
        Properties.add(config, Properties.GENERAL, "grace_period", 7.0, "The number of days before the difficulty begins increasing. Default is 7.0.");
        Properties.add(config, Properties.GENERAL, "rain_damage_rate", 3.75, "The number of seconds between each tick of rain damage (rounds up to the nearest 0.25 second). 0.0 disables rain damage. Default is 3.75 (vanilla hunger heals at a rate of 4.0).");
        Properties.add(config, Properties.GENERAL, "sleep_penalty", 2.0, "The multiplier given to time skipped (as through sleeping) when added to difficulty. Default is 2.0 (200% of skipped time is added to difficulty).");
        Properties.add(config, Properties.GENERAL, "smp_difficulty_mult", 0.666, "The difficulty will increase by this much more per additional player online. Default is 66.6% (2/3 speed for each player beyond the first).");
        Properties.add(config, Properties.GENERAL, "spawn_eggs", true, "If true, the mod will attempt to generate spawn eggs for each added mob. Default is true.");
        Properties.add(config, Properties.GENERAL, "thunderstorm_blacklist", "", "Comma-separated list of string entity ids that will not be affected by \"thunderstorm_spawning\". Default is none.");
        Properties.add(config, Properties.GENERAL, "thunderstorm_spawning", 0.4, "The chance (from 0 to 1) for mobs to ignore normal spawning rules during thunderstorms. Default is 0.4 (40% chance).");

        Properties.add(config, Properties.DISPLAY, "color_change", 240.0, "The number of days it takes to go through the six difficulty colors. -1.0 disables color changes. Default is 240.0 (1 color per 40 days).");
        Properties.add(config, Properties.DISPLAY, "offset_h", 0, "The horizontal offset (in pixels) of the timer from the nearest edge of the screen. If centered, a negative number will shift the timer leftward. Default is 0.");
        Properties.add(config, Properties.DISPLAY, "offset_v", 0, "The vertical offset (in pixels) of the timer from the nearest edge of the screen. If centered, a negative number will shift the timer upward. Default is 0.");
        Properties.add(config, Properties.DISPLAY, "position_h", "center", "The horizontal orientation for the timer (left/center/right). Default is center.");
        Properties.add(config, Properties.DISPLAY, "position_v", "top", "The vertical orientation for the timer (top/center/bottom). Default is top.");

        config.addCustomCategoryComment(Properties.GENERAL, "General and/or miscellaneous options.");
        config.addCustomCategoryComment(Properties.DISPLAY, "Options to customize the GUI.");
        config.save();

        config = new Configuration(new File(configDir, "Bosses" + fileExt));
        config.load();

        Properties.add(config, Properties.ANCIENT_GENERAL, "_blacklist", "WitherBoss", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is WitherBoss.");
        Properties.add(config, Properties.ANCIENT_GENERAL, "_lunar_chance", 0.005, "The additional chance gained from a full moon. Default is 0.005 (+0.5% chance on full moon).");
        Properties.add(config, Properties.ANCIENT_GENERAL, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.ANCIENT_GENERAL, "chance", 0.001, "The chance, for each \"_time_span\" days, for a mob to spawn as an ancient. Default is 0.001 (0.1% chance).");
        Properties.add(config, Properties.ANCIENT_GENERAL, "chance_max", 0.005, "The maximum ancient chance that can be given over time. Default is 0.005 (0.5% chance).");

        Properties.add(config, Properties.ANCIENT_ATTRIBUTES, "damage_flat_bonus", 2.0, "How much more damage bosses deal. Default is 2.0 (+2 damage).");
        Properties.add(config, Properties.ANCIENT_ATTRIBUTES, "damage_mult_bonus", 0.2, "How much more damage bosses deal. Default is 0.2 (+20% damage).");
        Properties.add(config, Properties.ANCIENT_ATTRIBUTES, "health_flat_bonus", 10.0, "How much more health a boss has. Default is 10.0 (10 health).");
        Properties.add(config, Properties.ANCIENT_ATTRIBUTES, "health_mult_bonus", 2.0, "How much more health a boss has. Default is 2.0 (+200% health).");
        Properties.add(config, Properties.ANCIENT_ATTRIBUTES, "resist_flat_bonus", 0.85, "How much more resistant bosses are to being knocked back. Default is 0.3 (+30% chance to resist knockback).");
        Properties.add(config, Properties.ANCIENT_ATTRIBUTES, "speed_mult_bonus", -0.15, "How much faster bosses move. Default is -0.15 (-15% speed).");

        Properties.add(config, Properties.ANCIENT_POTIONS, "fire_resistance", true, "If true, ancient mobs will be immune to fire damage. Default is true.");
        Properties.add(config, Properties.ANCIENT_POTIONS, "regeneration", 0, "Regenerates health (each rank halves the time between heals). -1 disables this. Default is 0 (1 health per 2.5 seconds).");
        Properties.add(config, Properties.ANCIENT_POTIONS, "resistance", 0, "Increases damage resistance (each rank grants -20% damage). -1 disables this. Default is 0 (-20% damage).");
        Properties.add(config, Properties.ANCIENT_POTIONS, "water_breathing", true, "If true, ancient mobs will not drown. Default is true.");

        config.addCustomCategoryComment(Properties.ANCIENT_GENERAL, "General and/or miscellaneous options for ancients.");
        config.addCustomCategoryComment(Properties.ANCIENT_ATTRIBUTES, "Options controlling ancients' extra attributes.");
        config.addCustomCategoryComment(Properties.ANCIENT_POTIONS, "Options controlling ancients' potion effects.");
        config.save();

        config = new Configuration(new File(configDir, "Equipment" + fileExt));
        config.load();

        Properties.add(config, Properties.ENCHANTS, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.ENCHANTS, "_lunar_chance", 0.5, "The additional chance gained from a full moon. Default is 0.5 (+50% chance on full moon).");
        Properties.add(config, Properties.ENCHANTS, "_lunar_level", 10.0, "The additional average enchantment level gained from a full moon. Will not increase above the max. Default is 10.0 (+10 levels on full moon).");
        Properties.add(config, Properties.ENCHANTS, "_standard_deviation", 2.5, "The standard deviation of the enchant level chosen (chosen on a normal distribution). Default is 2.5 (+/-5 levels ~95% of the time).");
        Properties.add(config, Properties.ENCHANTS, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.ENCHANTS, "chance", 0.05, "The chance, for each \"_time_span\" days, for each item equipped by this mod on any mob to be enchanted. Default is 0.05 (5% chance).");
        Properties.add(config, Properties.ENCHANTS, "chance_max", 0.5, "The maximum enchant chance that can be given over time. Default is 0.5 (50% chance).");
        Properties.add(config, Properties.ENCHANTS, "level", 5.0, "The average enchantment level, for each \"_time_span\" days, that an item will be enchanted by. Default is 5.0.");
        Properties.add(config, Properties.ENCHANTS, "level_max", 30.0, "The maximum average enchant level that can be given over time (you should not raise this above 30). Default is 30.0.");

        Properties.add(config, Properties.ARMOR, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.ARMOR, "_lunar_chance", 0.2, "The additional chance gained from a full moon. Default is 0.2 (+20% chance on full moon).");
        Properties.add(config, Properties.ARMOR, "_lunar_extra_chance", 0.3, "The additional extra armor chance gained from a full moon. Default is 0.3 (+30% chance on full moon).");
        Properties.add(config, Properties.ARMOR, "_lunar_tier", 0.1, "The additional tier-up chance gained from a full moon. Default is 0.1 (+10% chance on full moon).");
        Properties.add(config, Properties.ARMOR, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.ARMOR, "chance", 0.05, "The chance, for each \"_time_span\" days, of at least one piece of armor being equipped on any mob. Default is 0.05 (5% chance).");
        Properties.add(config, Properties.ARMOR, "chance_max", 1.0, "The maximum armor chance that can be given over time. Default is 1.0 (100% chance).");
        Properties.add(config, Properties.ARMOR, "extra_chance", 0.2, "The chance, for each \"_time_span\" days, to equip additional armor (rolled until it fails or all slots are filled). Default is 0.2 (20% chance).");
        Properties.add(config, Properties.ARMOR, "extra_chance_max", 0.9, "The maximum extra armor chance that can be given over time. Default is 0.9 (90% chance).");
        Properties.add(config, Properties.ARMOR, "tier", 0.07, "The chance, for each \"_time_span\" days, for the tier of a mob's armor increasing by 1 (rolled three times for each mob). Default is 0.07 (7% chance).");
        Properties.add(config, Properties.ARMOR, "tier_max", 0.7, "The maximum tier-up chance that can be given over time. Default is 0.7 (70% chance).");

        Properties.add(config, Properties.WEAPON_WEIGHTS, "axe", 3, "The weight that an axe will be chosen. Default is 3.");
        Properties.add(config, Properties.WEAPON_WEIGHTS, "pickaxe", 2, "The weight that a pickaxe will be chosen. Default is 2.");
        Properties.add(config, Properties.WEAPON_WEIGHTS, "shovel", 1, "The weight that a shovel will be chosen. Default is 1.");
        Properties.add(config, Properties.WEAPON_WEIGHTS, "sword", 6, "The weight that a sword will be chosen. Default is 6.");

        Properties.add(config, Properties.WEAPONS, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.WEAPONS, "_lunar_chance", 0.2, "The additional chance gained from a full moon. Default is 0.2 (+20% chance on full moon).");
        Properties.add(config, Properties.WEAPONS, "_lunar_tier", 0.1, "The additional tier-up chance gained from a full moon. Default is 0.1 (+10% chance on full moon).");
        Properties.add(config, Properties.WEAPONS, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.WEAPONS, "chance", 0.1, "The chance, for each \"_time_span\" days, of at least one piece of armor being equipped on any mob. Default is 0.1 (10% chance).");
        Properties.add(config, Properties.WEAPONS, "chance_max", 0.95, "The maximum armor chance that can be given over time. Default is 0.95 (95% chance).");
        Properties.add(config, Properties.WEAPONS, "tier", 0.07, "The chance, for each \"_time_span\" days, for the tier of a mob's weapon increasing by 1 (rolled three times for each mob). Default is 0.07 (7% chance).");
        Properties.add(config, Properties.WEAPONS, "tier_max", 0.7, "The maximum tier-up chance that can be given over time. Default is 0.7 (70% chance).");

        config.addCustomCategoryComment(Properties.ENCHANTS, "Options controlling mobs' enchantments. This applies to both normal equipment and to ancients' non-unique equipment.");
        config.addCustomCategoryComment(Properties.ARMOR, "Options controlling mobs' equipped armor.");
        config.addCustomCategoryComment(Properties.WEAPON_WEIGHTS, "The weight for each type of equippable weapon to be picked. This applies to both normal weapon picks and to ancients.");
        config.addCustomCategoryComment(Properties.WEAPONS, "Options controlling mobs' equipped weapons.");
        config.save();

        config = new Configuration(new File(configDir, "FullMoons" + fileExt));
        config.load();

        Properties.add(config, Properties.FULL_MOONS, "_full_moon_siege", 1.0, "The chance (from 0 to 1) for a full moon to trigger a siege event. Default is 1.0 (100% chance).");
        Properties.add(config, Properties.FULL_MOONS, "_sleep_on_full_moon", false, "If false, the player cannot sleep during a full moon. Default is false.");
        Properties.add(config, Properties.FULL_MOONS, "_start_breechers", 10.0, "The difficulty level when breechers can first appear in sieges. -1.0 disables them. Default is 10.0 days.");
        Properties.add(config, Properties.FULL_MOONS, "_start_destroyers", 75.0, "The difficulty level when destroyers can first appear in sieges. -1.0 disables them. Default is 75.0 days.");
        Properties.add(config, Properties.FULL_MOONS, "_start_ghosts", 0.0, "The difficulty level when ghosts can first appear in sieges (it's a good idea to have at least one mob start at 0). -1.0 disables them. Default is 0.0 days.");
        Properties.add(config, Properties.FULL_MOONS, "_start_grumps", 20.0, "The difficulty level when grumps can first appear in sieges. -1.0 disables them. Default is 20.0 days.");
        Properties.add(config, Properties.FULL_MOONS, "_start_seekers", 50.0, "The difficulty level when seekers can first appear in sieges. -1.0 disables them. Default is 50.0 days.");
        Properties.add(config, Properties.FULL_MOONS, "_time_span", 40.0, "The number of days for each application of the below max spawn cap increases. Default is 40.0.");
        Properties.add(config, Properties.FULL_MOONS, "max_breechers", 2.0, "The amount of breechers that can appear in a single siege per \"time_span\" days. Default is 2.0.");
        Properties.add(config, Properties.FULL_MOONS, "max_destroyers", 1.0, "The amount of destroyers that can appear in a single siege per \"time_span\" days. Default is 1.0.");
        Properties.add(config, Properties.FULL_MOONS, "max_ghosts", 6.0, "The amount of ghosts that can appear in a single siege per \"time_span\" days. Default is 6.0.");
        Properties.add(config, Properties.FULL_MOONS, "max_grumps", 1.5, "The amount of grumps that can appear in a single siege per \"time_span\" days. Default is 1.5.");
        Properties.add(config, Properties.FULL_MOONS, "max_seekers", 1.0, "The amount of seekers that can appear in a single siege per \"time_span\" days. Default is 1.0.");
        Properties.add(config, Properties.FULL_MOONS, "min_breechers", 4, "The starting amount of breechers that can appear in a single siege. Default is 4.");
        Properties.add(config, Properties.FULL_MOONS, "min_destroyers", 1, "The starting amount of destroyers that can appear in a single siege. Default is 1.");
        Properties.add(config, Properties.FULL_MOONS, "min_ghosts", 4, "The starting amount of ghosts that can appear in a single siege. Default is 4.");
        Properties.add(config, Properties.FULL_MOONS, "min_grumps", 4, "The starting amount of grumps that can appear in a single siege. Default is 4.");
        Properties.add(config, Properties.FULL_MOONS, "min_seekers", 1, "The starting amount of seekers that can appear in a single siege. Default is 1.");
        Properties.add(config, Properties.FULL_MOONS, "weight_breecher", 4, "The relative spawn weight for breechers. Default is 4.");
        Properties.add(config, Properties.FULL_MOONS, "weight_destroyer", 2, "The relative spawn weight for destroyers. Default is 2.");
        Properties.add(config, Properties.FULL_MOONS, "weight_ghost", 8, "The relative spawn weight for ghosts. Default is 8.");
        Properties.add(config, Properties.FULL_MOONS, "weight_grump", 3, "The relative spawn weight for grumps. Default is 3.");
        Properties.add(config, Properties.FULL_MOONS, "weight_seeker", 2, "The relative spawn weight for seekers. Default is 2.");

        config.addCustomCategoryComment(Properties.FULL_MOONS, "Options related to full moons and full moon siege events.");
        config.save();

        config = new Configuration(new File(configDir, "PotionsAndAttributes" + fileExt));
        config.load();

        Properties.add(config, Properties.POTIONS, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.POTIONS, "_lunar_chance", 0.3, "The additional chance gained from a full moon. Default is 0.3 (+30% chance on full moon).");
        Properties.add(config, Properties.POTIONS, "_lunar_extra_chance", 0.3, "The additional extra effect chance gained from a full moon. Default is 0.3 (+30% chance on full moon).");
        Properties.add(config, Properties.POTIONS, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.POTIONS, "chance", 0.1, "The chance, for each \"_time_span\" days, of a random positive potion effect being applied to any mob. Default is 0.1 (10% chance).");
        Properties.add(config, Properties.POTIONS, "chance_max", 1.0, "The maximum potion effect chance that can be given over time. Default is 1.0 (100% chance).");
        Properties.add(config, Properties.POTIONS, "extra_chance", 0.1, "The chance, for each \"_time_span\" days, of an additional random positive potion effect (rolled twice) being applied to any mob already given an effect. Default is 0.1 (10% chance).");
        Properties.add(config, Properties.POTIONS, "extra_chance_max", 0.7, "The maximum extra potion effect chance that can be given over time. Default is 0.7 (70% chance).");

        Properties.add(config, Properties.DAMAGE, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.DAMAGE, "_lunar_flat_bonus", 1.0, "The flat bonus gained from a full moon. Default is 1.0 (+1 on full moon).");
        Properties.add(config, Properties.DAMAGE, "_lunar_mult_bonus", 0.2, "The multiplier bonus gained from a full moon. Default is 0.2 (+20% on full moon).");
        Properties.add(config, Properties.DAMAGE, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.DAMAGE, "flat_bonus", 1.0, "The flat bonus given for each \"_time_span\" days. Default is 1.0.");
        Properties.add(config, Properties.DAMAGE, "flat_bonus_max", -1.0, "The maximum flat bonus that can be given over time. Default is -1.0 (no limit).");
        Properties.add(config, Properties.DAMAGE, "mult_bonus", 0.3, "The multiplier bonus given for each \"_time_span\" days. Default is 0.5 (+50%).");
        Properties.add(config, Properties.DAMAGE, "mult_bonus_max", 5.0, "The maximum multiplier bonus that can be given over time. Default is 5.0 (+500%).");

        Properties.add(config, Properties.KNOCKBACK_RESIST, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.KNOCKBACK_RESIST, "_lunar_flat_bonus", 0.2, "The flat bonus gained from a full moon. Default is 0.2 (+20% on full moon).");
        Properties.add(config, Properties.KNOCKBACK_RESIST, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.KNOCKBACK_RESIST, "flat_bonus", 0.05, "The flat bonus given for each \"_time_span\" days. Default is 0.05 (+5%).");
        Properties.add(config, Properties.KNOCKBACK_RESIST, "flat_bonus_max", 0.3, "The maximum flat bonus that can be given over time. Default is 0.3 (+30%).");

        Properties.add(config, Properties.HEALTH, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.HEALTH, "_lunar_flat_bonus", 10.0, "The flat bonus gained from a full moon. Default is 10.0 (+10 on full moon).");
        Properties.add(config, Properties.HEALTH, "_lunar_mult_bonus", 0.5, "The multiplier bonus gained from a full moon. Default is 0.5 (+50%).");
        Properties.add(config, Properties.HEALTH, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.HEALTH, "flat_bonus", 1.0, "The flat bonus given for each \"_time_span\" days. Default is 1.0.");
        Properties.add(config, Properties.HEALTH, "flat_bonus_max", -1.0, "The maximum flat bonus that can be given over time. Default is -1.0 (no limit).");
        Properties.add(config, Properties.HEALTH, "mult_bonus", 0.8, "The multiplier bonus given for each \"_time_span\" days. Default is 0.8 (+80%).");
        Properties.add(config, Properties.HEALTH, "mult_bonus_max", -1.0, "The maximum multiplier bonus that can be given over time. Default is -1.0 (no limit).");

        Properties.add(config, Properties.SPEED, "_blacklist", "", "Comma-separated list of string entity ids that will not gain any of these bonuses. Default is none.");
        Properties.add(config, Properties.SPEED, "_lunar_mult_bonus", 0.1, "The multiplier bonus gained from a full moon. Default is 0.1 (+10% on full moon).");
        Properties.add(config, Properties.SPEED, "_time_span", 40.0, "The number of days for each application of the below values. Default is 40.0.");
        Properties.add(config, Properties.SPEED, "mult_bonus", 0.05, "The multiplier bonus given for each \"_time_span\" days. Default is 0.05 (+5%).");
        Properties.add(config, Properties.SPEED, "mult_bonus_max", 0.2, "The maximum multiplier bonus that can be given over time. Default is 0.2 (+20%).");

        config.addCustomCategoryComment(Properties.POTIONS, "Options controlling mobs' potion effects.");

        config.addCustomCategoryComment(Properties.DAMAGE, "Options controlling the melee attack damage modifiers.\nAll damage values are in half hearts.");
        config.addCustomCategoryComment(Properties.KNOCKBACK_RESIST, "Options controlling the knockback resistance modifiers.");
        config.addCustomCategoryComment(Properties.HEALTH, "Options controlling the max health modifiers.\nAll health values are in half hearts.");
        config.addCustomCategoryComment(Properties.SPEED, "Options controlling the movement speed modifiers.");
        config.save();
    }

    /** Gets the mod's random number generator. */
    private static Random random() {
        return ApocalypseMod.random;
    }

    /** Passes to the mod. */
    private static void debugException(String message) {
        ApocalypseMod.logError(message);
    }

    // Loads the property as the specified value.
    public static void add(Configuration config, String category, String field, String defaultValue, String comment) {
        Properties.map.put(category + "@" + field, config.get(category, field, defaultValue, comment).getString());
    }
    public static void add(Configuration config, String category, String field, int defaultValue, String comment) {
        Properties.map.put(category + "@" + field, Integer.valueOf(config.get(category, field, defaultValue, comment).getInt(defaultValue)));
    }
    public static void add(Configuration config, String category, String field, boolean defaultValue, String comment) {
        Properties.map.put(category + "@" + field, Boolean.valueOf(config.get(category, field, defaultValue, comment).getBoolean(defaultValue)));
    }
    public static void add(Configuration config, String category, String field, double defaultValue, String comment) {
        Properties.map.put(category + "@" + field, Double.valueOf(config.get(category, field, defaultValue, comment).getDouble(defaultValue)));
    }

    /** Gets the Object property. */
    public static Object getProperty(String category, String field) {
        return Properties.map.get(category + "@" + field);
    }

    // Gets the value of the property (instead of an Object representing it).
    public static String getString(String category, String field) {
        return Properties.getProperty(category, field).toString();
    }
    public static boolean getBoolean(String category, String field) {
        Object property = Properties.getProperty(category, field);
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue();
        if (property instanceof Integer)
            return Properties.random().nextInt( ((Number) property).intValue()) == 0;
        if (property instanceof Double)
            return Properties.random().nextDouble() < ((Number) property).doubleValue();
        Properties.debugException("Tried to get boolean for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return false;
    }
    public static int getInt(String category, String field) {
        Object property = Properties.getProperty(category, field);
        if (property instanceof Number)
            return ((Number) property).intValue();
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue() ? 1 : 0;
        Properties.debugException("Tried to get int for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return 0;
    }
    public static double getDouble(String category, String field) {
        Object property = Properties.getProperty(category, field);
        if (property instanceof Number)
            return ((Number) property).doubleValue();
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue() ? 1.0 : 0.0;
        Properties.debugException("Tried to get double for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return 0.0;
    }
}