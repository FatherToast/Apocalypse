package toast.apocalypse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import toast.apocalypse.event.EventBase;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The major backbone of Apocalypse, this class manages everything to do with the world difficulty - increases it over time,
 * saves and loads it to and from the disk, and notifies clients of changes to it.<br>
 * In addition, it houses many helper methods related to world difficulty and save data.
 */
public class WorldDifficultyManager {

    /** The rain damage source. */
    public static final DamageSource RAIN_DAMAGE = new DamageSource("Apocalypse.rain").setDamageBypassesArmor();

    /** Map of all worlds to their respective player-based difficulties. */
    private static final HashMap<Integer, WorldDifficultyData> WORLD_MAP = new HashMap<Integer, WorldDifficultyData>();
    /** Stack of entities that need to be initialized. */
    private static final ArrayDeque<EntityLivingBase> ENTITY_STACK = new ArrayDeque<EntityLivingBase>();

    /** Number of ticks per update. */
    public static final int TICKS_PER_UPDATE = 5;

    // The NBT tags used to store all of this mod's NBT info.
    /** The NBT tag storing all other tags on an entity. */
    public static final String TAG_BASE = "Apocalypse";
    /** The ticks a player has been in the rain since their last rain damage tick. */
    public static final String TAG_RAIN = "Rain";
    /** The ticks remaining for the gravity effect. */
    public static final String TAG_GRAVITY = "Gravity";

    /** The NBT tag to mark a mob as initialized. */
    public static final String TAG_INIT = "Apocalypse|Init";

    /** The current world folder. */
    public static File worldDir;

    /** The current difficulty. */
    private static long worldDifficulty;
    /** The difficulty last saved. */
    private static long lastWorldDifficulty;

    /** The current difficulty rate multiplier. */
    private static double difficultyRateMult = 1.0;
    /** The last calculated difficulty rate multiplier. */
    private static double lastDifficultyRateMult = 1.0;

    /** The currently active event. */
    private static EventBase activeEvent;

    /** Used to prevent full moons from constantly happening. */
    private static boolean checkedFullMoon = true;

    /**
     * Gets the NBT tag compound that holds all of this mod's data for a player.
     * @param player The player to load the data from.
     * @return A compound tag containing all of the player's data related to this mod.
     */
    public static NBTTagCompound getData(EntityPlayer player) {
        NBTTagCompound tag = player.getEntityData();
        if (!tag.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            tag.setTag(EntityPlayer.PERSISTED_NBT_TAG, tag = new NBTTagCompound());
        }
        else {
            tag = tag.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }
        if (!tag.hasKey(WorldDifficultyManager.TAG_BASE)) {
            tag.setTag(WorldDifficultyManager.TAG_BASE, tag = new NBTTagCompound());
        }
        else {
            tag = tag.getCompoundTag(WorldDifficultyManager.TAG_BASE);
        }
        return tag;
    }

    /** @return The current world difficulty (in ticks). */
    public static long getWorldDifficulty() {
        return Math.max(0L, WorldDifficultyManager.worldDifficulty);
    }

    /** Updates the world difficulty level for a client.
     * @param difficulty The difficulty level to set (in ticks). */
    @SideOnly(Side.CLIENT)
    public static void setWorldDifficulty(long difficulty) {
        WorldDifficultyManager.worldDifficulty = difficulty;
    }

    /** @return The current world difficulty rate multiplier. */
    public static double getDifficultyRate() {
        return WorldDifficultyManager.difficultyRateMult;
    }

    /** Updates the world difficulty rate for a client.
     * @param difficultyRate The difficulty rate to set. */
    @SideOnly(Side.CLIENT)
    public static void setDifficultyRate(double difficultyRate) {
        WorldDifficultyManager.difficultyRateMult = difficultyRate;
    }

    /** Processes the set command when it is used.
     * @param arg The argument supplied by the user (world difficulty in days. */
    public static void processSetCommand(double arg) {
        WorldDifficultyManager.worldDifficulty = (long) (arg * 24000L);
        WorldDifficultyManager.updateWorldDifficulty();
    }

    /** Saves the world's difficulty and updates the clients, if needed. */
    private static void updateWorldDifficulty() {
        if (WorldDifficultyManager.difficultyRateMult != WorldDifficultyManager.lastDifficultyRateMult) {
            ApocalypseMod.CHANNEL.sendToAll(new MessageWorldDifficulty(WorldDifficultyManager.difficultyRateMult));
            WorldDifficultyManager.lastDifficultyRateMult = WorldDifficultyManager.difficultyRateMult;
        }
        if (WorldDifficultyManager.activeEvent != null || WorldDifficultyManager.worldDifficulty != WorldDifficultyManager.lastWorldDifficulty) {
            if ((int) (WorldDifficultyManager.worldDifficulty / 24000L) != (int) (WorldDifficultyManager.lastWorldDifficulty / 24000L) || (int) (WorldDifficultyManager.worldDifficulty % 24000L / 2400) != (int) (WorldDifficultyManager.lastWorldDifficulty % 24000L / 2400)) {
                ApocalypseMod.CHANNEL.sendToAll(new MessageWorldDifficulty(WorldDifficultyManager.worldDifficulty));
            }
            WorldDifficultyManager.lastWorldDifficulty = WorldDifficultyManager.worldDifficulty;
            WorldDifficultyManager.save();
        }
    }

    /** @return The active event, or null if none are active. */
    public static EventBase getActiveEvent() {
        return WorldDifficultyManager.activeEvent;
    }

    /** Starts an event, if possible.
     * @param event The event to start.
     * @return True if the event was successfully started. */
    public static boolean startEvent(EventBase event) {
        if (event == null)
            return false;
        if (WorldDifficultyManager.activeEvent != null) {
            if (!WorldDifficultyManager.activeEvent.canBeInterrupted(event))
                return false;
            WorldDifficultyManager.activeEvent.onEnd();
        }
        event.onStart();
        WorldServer[] worlds = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers;
        for (WorldServer world : worlds) {
            if (world != null) {
                for (Object player : world.playerEntities) {
                    if (player instanceof EntityPlayer) {
                        ((EntityPlayer) player).addChatMessage(new ChatComponentTranslation(event.getStartMessage(), new Object[0]));
                    }
                }
            }
        }
        WorldDifficultyManager.activeEvent = event;
        WorldDifficultyManager.updateWorldDifficulty();
        return true;
    }

    /** Ends the current active event, if any. */
    public static void endEvent() {
        if (WorldDifficultyManager.activeEvent != null) {
            WorldDifficultyManager.activeEvent.onEnd();
            WorldDifficultyManager.activeEvent = null;
            WorldDifficultyManager.updateWorldDifficulty();
        }
    }

    /** @param world The world to check in.
     * @return True if there is currently a full moon. */
    public static boolean isFullMoon(World world) {
        return world.provider.getMoonPhase(world.getWorldTime()) == 0;
    }

    /**
     * Updates the world and all players and the event in it. Handles difficulty changes.
     *
     * @param world The world to update.
     * @param mostSkippedTime The largest time difference of any other world since the last update.
     * @return If the time difference in this world is larger than mostSkippedTime, then that time difference is
     * 		returned - otherwise mostSkippedTime is returned.
     */
    public static long updateWorld(WorldServer world, long mostSkippedTime) {
        if (world == null)
            return mostSkippedTime;
        WorldDifficultyData worldData = WorldDifficultyManager.WORLD_MAP.get(Integer.valueOf(world.provider.dimensionId));

        // Check for time jumps (aka sleeping in bed)
        long skippedTime = 0L;
        if (world.provider.dimensionId == 0) { // TEST - base time jumps only on overworld
            if (WorldDifficultyManager.difficultyRateMult > 0.0 && worldData != null && PropHelper.SLEEP_PENALTY > 0.0) {
                skippedTime = world.getWorldTime() - worldData.lastWorldTime; // normally == 5
            }
        }

        // Starts the full moon event
        if (WorldDifficultyManager.worldDifficulty > 0L && WorldDifficultyManager.activeEvent != EventBase.fullMoon) {
            int dayTime = (int) (world.getWorldTime() % 24000);
            if (dayTime < 13000) {
                WorldDifficultyManager.checkedFullMoon = false;
            }
            else if (!WorldDifficultyManager.checkedFullMoon && WorldDifficultyManager.isFullMoon(world)) {
                WorldDifficultyManager.checkedFullMoon = true;
                if (world.rand.nextDouble() < PropHelper.FULL_MOON_EVENT) {
                    WorldDifficultyManager.startEvent(EventBase.fullMoon);
                }
            }
        }

        // Update active event
        if (WorldDifficultyManager.activeEvent != null) {
            WorldDifficultyManager.activeEvent.update(world);
        }

        // Update players
        EntityPlayer player = null;
        NBTTagCompound data;
        for (Object entity : new ArrayList(world.playerEntities)) {
            if (entity instanceof EntityPlayer) {
                player = (EntityPlayer) entity;
                data = WorldDifficultyManager.getData(player);
                // Update active event
                if (WorldDifficultyManager.activeEvent != null) {
                    WorldDifficultyManager.activeEvent.update(player);
                }
                // Update effects
                EventHandler.onPlayerUpdate(player, data);
            }
        }

        if (worldData == null) {
            WorldDifficultyManager.WORLD_MAP.put(Integer.valueOf(world.provider.dimensionId), worldData = new WorldDifficultyData(world.provider.dimensionId));
        }
        worldData.lastWorldTime = world.getWorldTime();

        if (mostSkippedTime < skippedTime)
            return skippedTime;
        return mostSkippedTime;
    }

    /** Marks an entity as needing initialization if it has not already been initialized.
     * @param entity The entity to mark. */
    public static void markForInit(EntityLivingBase entity) {
        if (entity.getEntityData().getByte(WorldDifficultyManager.TAG_INIT) == 0) {
            WorldDifficultyManager.ENTITY_STACK.add(entity);
        }
    }

    /** Initializes server files. Called before the server starts up.
     * @param server The server about to be started. */
    public static void init(MinecraftServer server) {
        try {
            WorldDifficultyManager.worldDir = server.getFile("saves/" + server.getFolderName() + "/" + ApocalypseMod.MODID);
            WorldDifficultyManager.worldDir.mkdirs();
            WorldDifficultyManager.load();
        }
        catch (Exception ex) {
            ApocalypseMod.log("Failed to initialize data storage! You should probably reload the world as soon as possible.");
            ex.printStackTrace();
            WorldDifficultyManager.worldDir = null;
        }
    }

    /** Cleans up the references to things in a server when the server stops. */
    public static void cleanup() {
        WorldDifficultyManager.WORLD_MAP.clear();
        WorldDifficultyManager.ENTITY_STACK.clear();
        WorldDifficultyManager.worldDifficulty = 0L;
        WorldDifficultyManager.worldDir = null;
    }

    /** The counter to the next update. */
    private int updateCounter = 0;

    /** Constructs a WorldDifficultyManager that will automatically register itself to do all work it needs to do. */
    public WorldDifficultyManager() {
        FMLCommonHandler.instance().bus().register(this);
    }

    /**
     * Called when a player logs in.
     * EntityPlayer player = the player logging in.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            // Send the current server info to each player as they log in
            ApocalypseMod.CHANNEL.sendTo(new MessageWorldDifficulty(WorldDifficultyManager.worldDifficulty, WorldDifficultyManager.difficultyRateMult), (EntityPlayerMP) event.player);
        }
    }

    /**
     * Called each game tick.
     * TickEvent.Type type = the type of tick.
     * Side side = the side this tick is on.
     * TickEvent.Phase phase = the phase of this tick (START, END).
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Counter to update the world
            if (++this.updateCounter >= WorldDifficultyManager.TICKS_PER_UPDATE) {
                this.updateCounter = 0;
                // Update active event
                if (WorldDifficultyManager.activeEvent != null) {
                    WorldDifficultyManager.activeEvent.update();
                }

                MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                WorldServer[] worlds = server.worldServers;
                // Update difficulty rate, scaled per person
                WorldDifficultyManager.difficultyRateMult = server.getCurrentPlayerCount();
                if (WorldDifficultyManager.difficultyRateMult > 1.0) {
                    WorldDifficultyManager.difficultyRateMult = (WorldDifficultyManager.difficultyRateMult - 1.0) * PropHelper.DIFFICULTY_PER_PERSON + 1.0;
                }
                // Apply dimension difficulty rate penalty if any player is in another dimension
                if (PropHelper.DIMENSION_PENALTY > 0.0) {
                    for (Object player : server.getConfigurationManager().playerEntityList) {
                        if (player instanceof EntityPlayer && ((EntityPlayer) player).dimension != 0) {
                            WorldDifficultyManager.difficultyRateMult *= 1.0 + PropHelper.DIMENSION_PENALTY;
                            break;
                        }
                    }
                }
                WorldDifficultyManager.worldDifficulty += WorldDifficultyManager.TICKS_PER_UPDATE * WorldDifficultyManager.difficultyRateMult;

                // Update each world
                long mostSkippedTime = 0L;
                for (WorldServer world : worlds) {
                    mostSkippedTime = WorldDifficultyManager.updateWorld(world, mostSkippedTime);
                }
                // Handle sleep penalty
                if (mostSkippedTime > 20L) {
                    WorldDifficultyManager.worldDifficulty += mostSkippedTime * PropHelper.SLEEP_PENALTY * WorldDifficultyManager.difficultyRateMult;
                    // Send skipped time messages
                    for (Object player : server.getConfigurationManager().playerEntityList) {
                        if (player instanceof EntityPlayer) {
                            ((EntityPlayer) player).addChatComponentMessage(new ChatComponentTranslation("event.Apocalypse.sleepPenalty", new Object[0]));
                        }
                    }
                }
                WorldDifficultyManager.updateWorldDifficulty();
            }
            // Initialize any spawned entities
            if (!WorldDifficultyManager.ENTITY_STACK.isEmpty()) {
                int count = 10;
                EntityLivingBase entity;
                while (count-- > 0) {
                    entity = WorldDifficultyManager.ENTITY_STACK.pollFirst();
                    if (entity == null) {
                        break;
                    }
                    EventHandler.initializeEntity(entity);
                    entity.getEntityData().setByte(WorldDifficultyManager.TAG_INIT, (byte) 1);
                }
            }
        }
    }

    /** Saves any world data. */
    private static void save() {
        try {
            File fileTmp = new File(WorldDifficultyManager.worldDir, "data.bin.tmp");
            File file = new File(WorldDifficultyManager.worldDir, "data.bin");
            fileTmp.createNewFile();
            FileOutputStream stream = new FileOutputStream(fileTmp, true);
            DataOutputStream out = new DataOutputStream(stream);
            out.writeLong(WorldDifficultyManager.worldDifficulty);
            if (WorldDifficultyManager.activeEvent != null) {
                out.writeByte(WorldDifficultyManager.activeEvent.EVENT_ID);
                WorldDifficultyManager.activeEvent.save(out);
            }
            else {
                out.writeByte(-1);
            }
            stream.close();
            file.delete();
            fileTmp.renameTo(file);
        }
        catch (Exception ex) {
            ApocalypseMod.log("[ERROR] Failed to save world data!");
            ex.printStackTrace();
        }
    }
    /** Loads any world data. */
    private static void load() {
        try {
            File file = new File(WorldDifficultyManager.worldDir, "data.bin");
            if (file.exists()) {
                FileInputStream stream = new FileInputStream(file);
                DataInputStream in = new DataInputStream(stream);
                WorldDifficultyManager.worldDifficulty = in.readLong();
                int eventId = in.readByte();
                if (eventId >= 0) {
                    WorldDifficultyManager.activeEvent = EventBase.EVENTS[eventId % EventBase.EVENTS.length];
                    WorldDifficultyManager.activeEvent.load(in);
                }
                stream.close();
            }
            else {
                WorldDifficultyManager.worldDifficulty = -PropHelper.GRACE_PERIOD;
            }
        }
        catch (Exception ex) {
            ApocalypseMod.log("Failed to load world data!");
            ex.printStackTrace();
        }
    }

    /** Contains info related to this mod about a world. */
    public static class WorldDifficultyData {

        /** The dimension id of the world. */
        public final int dimension;
        /** The last recorded world time of the world. */
        public long lastWorldTime;

        /** Contructs WorldDifficultyData for a world to store information needed to manage the world difficulty.
         * @param dimension The world's dimension id. */
        public WorldDifficultyData(int dimension) {
            this.dimension = dimension;
        }
    }

}