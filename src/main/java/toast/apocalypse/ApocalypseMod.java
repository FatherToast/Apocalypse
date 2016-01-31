package toast.apocalypse;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import toast.apocalypse.entity.EntityDestroyerFireball;
import toast.apocalypse.entity.EntityMonsterFishHook;
import toast.apocalypse.entity.EntitySeekerFireball;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * This is the mod class. Everything the mod does is initialized by this class.
 */
@Mod(modid = ApocalypseMod.MODID, name = "Apocalypse", version = ApocalypseMod.VERSION)
public class ApocalypseMod {

    /* TO DO *\
	 * Improve ghast AI by not checking the entire distance when attempting to move towards the player.
	
     * Equipment drop/pickup chance?
     * Drop rate?
     * XP?

     * HUD displays months in SSP?
     * Lunar clock?

     * Full moon "event"
       > Monsters auto-target players
	   > Something to better defeat underwater/underground bases
	   > Something to better defeat lava
    \* ** ** */

    /** This mod's id. */
    public static final String MODID = "Apocalypse";
    /** This mod's version. */
    public static final String VERSION = "0.0.7";

    /** If true, this mod starts up in debug mode. */
    public static boolean debug = false;
    /** This mod's sided proxy. */
    @SidedProxy(clientSide = "toast.apocalypse.client.ClientProxy", serverSide = "toast.apocalypse.CommonProxy")
    public static CommonProxy proxy;

    /** The random number generator for this mod. */
    public static final Random random = new Random();
    /** The network channel for this mod. */
    public static SimpleNetworkWrapper CHANNEL;

    /** The path to the textures folder. */
    public static final String TEXTURE_PATH = ApocalypseMod.MODID + ":textures/";

    /** Array of all entity savegame ids to register. */
    public static final String[] ENTITIES = {
        "Breecher", "Grump", "Seeker", "Ghost", "Destroyer"
    };
    /** Array of all egg colors to use for the entities in {@link #ENTITIES}.<br>
     * The first index is the index in ENTITIES and the second index is 0 for primary and 1 for secondary egg color. */
    public static final int[][] ENTITY_EGG_COLORS = {
        /*"Breecher",             "Grump",                "Seeker",               "Ghost",                "Destroyer" */
        { 0x0da70b, 0xf9f9f9 }, { 0xf9f9f9, 0x2d41f4 }, { 0xf9f9f9, 0xa80e0e }, { 0xbcbcbc, 0x708899 }, { 0x7d7d7d, 0xa80e0e }
    };

    /** The bucket helmet. */
    public static ItemBucketHelm bucketHelm;

    /** Called before initialization. Loads the properties/configurations. */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Properties.init(new File(event.getModConfigurationDirectory(), ApocalypseMod.MODID));
        ApocalypseMod.debug = Properties.getBoolean(Properties.GENERAL, "debug");
        ApocalypseMod.logDebug("Loading in debug mode!");
        PropHelper.class.getName();

        ApocalypseMod.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("Apocalypse|Info");
        int id = 0;
        ApocalypseMod.CHANNEL.registerMessage(MessageWorldDifficulty.Handler.class, MessageWorldDifficulty.class, id++, Side.CLIENT);

        String texture = ApocalypseMod.MODID + ":";
        ApocalypseMod.bucketHelm = (ItemBucketHelm) new ItemBucketHelm().setUnlocalizedName("bucketHelm").setCreativeTab(CreativeTabs.tabCombat).setTextureName(texture + "bucket_helm");
        GameRegistry.registerItem(ApocalypseMod.bucketHelm, ApocalypseMod.bucketHelm.getUnlocalizedName().substring(5));
    }

    /** Called during initialization. Registers entities, mob spawns, and renderers. */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new WorldDifficultyManager();
        new EventHandler();
        this.registerMobs();
        ApocalypseMod.proxy.registerRenderers();

        GameRegistry.addShapelessRecipe(new ItemStack(ApocalypseMod.bucketHelm), Items.bucket);
    }

    /** Registers the entities in this mod. */
    private void registerMobs() {
        int id = 0;
        boolean makeSpawnEggs = Properties.getBoolean(Properties.GENERAL, "spawn_eggs");
        Method eggIdClaimer = null;
        int eggId;
        if (makeSpawnEggs) {
            try {
                eggIdClaimer = EntityRegistry.class.getDeclaredMethod("validateAndClaimId", int.class);
                eggIdClaimer.setAccessible(true);
            }
            catch (Exception ex) {
                ApocalypseMod.log("Error claiming spawn egg ID! Spawn eggs will probably be overwritten.");
                ex.printStackTrace();
            }
        }

        Class entityClass;
        for (int i = 0; i < ApocalypseMod.ENTITIES.length; i++) {
            try {
                entityClass = Class.forName("toast.apocalypse.entity.Entity" + ApocalypseMod.ENTITIES[i]);
                EntityRegistry.registerModEntity(entityClass, ApocalypseMod.ENTITIES[i], id++, this, 80, 3, true);

                if (makeSpawnEggs) {
                    eggId = EntityRegistry.findGlobalUniqueEntityId();
                    try {
                        if (eggIdClaimer != null) {
                            eggId = ((Integer)eggIdClaimer.invoke(EntityRegistry.instance(), Integer.valueOf(eggId))).intValue();
                        }
                    }
                    catch (Exception ex) {
                        // Do nothing
                    }
                    EntityList.IDtoClassMapping.put(Integer.valueOf(eggId), entityClass);
                    EntityList.entityEggs.put(Integer.valueOf(eggId), new EntityEggInfo(eggId, ApocalypseMod.ENTITY_EGG_COLORS[i][0], ApocalypseMod.ENTITY_EGG_COLORS[i][1]));
                }
            }
            catch (ClassNotFoundException ex) {
                ApocalypseMod.logError("@" + ApocalypseMod.ENTITIES[i] + ": class not found!");
            }
        }

        EntityRegistry.registerModEntity(EntitySeekerFireball.class, "SeekerFireball", id++, this, 64, 10, true);
        EntityRegistry.registerModEntity(EntityDestroyerFireball.class, "DestroyerFireball", id++, this, 64, 10, true);
        EntityRegistry.registerModEntity(EntityMonsterFishHook.class, "FishHook", id++, this, 64, 5, true);
    }

    /** Called as the server is starting. */
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        WorldDifficultyManager.init(event.getServer());

        ServerCommandManager commandManager = (ServerCommandManager)event.getServer().getCommandManager();
        commandManager.registerCommand(new CommandSetDifficulty());
    }

    /** Called as the server is stopping. */
    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        WorldDifficultyManager.cleanup();
    }

    /** Prints the message to the console with this mod's name tag. */
    public static void log(String message) {
        System.out.println("[" + ApocalypseMod.MODID + "] " + message);
    }
    /** Prints the message to the console with this mod's name tag if debugging is enabled. */
    public static void logDebug(String message) {
        if (ApocalypseMod.debug) {
            System.out.println("[" + ApocalypseMod.MODID + "] [debug] " + message);
        }
    }
    /** Prints the message to the console with this mod's name tag and a warning tag. */
    public static void logWarning(String message) {
        System.out.println("[" + ApocalypseMod.MODID + "] [WARNING] " + message);
    }
    /** Prints the message to the console with this mod's name tag and an error tag.<br>
     * Throws a runtime exception with a message and this mod's name tag if debugging is enabled. */
    public static void logError(String message) {
        if (ApocalypseMod.debug)
            throw new RuntimeException("[" + ApocalypseMod.MODID + "] " + message);
        ApocalypseMod.log("[ERROR] " + message);
    }
    /** Throws a runtime exception with a message and this mod's name tag. */
    public static void exception(String message) {
        throw new RuntimeException("[" + ApocalypseMod.MODID + "] " + message);
    }
}
