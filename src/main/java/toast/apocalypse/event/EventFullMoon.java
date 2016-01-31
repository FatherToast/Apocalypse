package toast.apocalypse.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.WorldServer;
import toast.apocalypse.ApocalypseMod;
import toast.apocalypse.PropHelper;
import toast.apocalypse.Properties;
import toast.apocalypse.WorldDifficultyManager;
import toast.apocalypse.entity.EntityBreecher;
import toast.apocalypse.entity.EntityDestroyer;
import toast.apocalypse.entity.EntityGhost;
import toast.apocalypse.entity.EntityGrump;
import toast.apocalypse.entity.EntitySeeker;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * The full moon event. This event can occur every 8 days in game and will interrupt any other event and can not be interrupted
 * by any other event.<br>
 * These are often referred to as "full moon sieges" in other parts of the code and in the properties file.
 */
public class EventFullMoon extends EventBase {

    /** Spawn weight for each mob type. */
    public static final int[] SPAWN_WEIGHTS = {
        Properties.getInt(Properties.FULL_MOONS, "weight_breecher"),
        Properties.getInt(Properties.FULL_MOONS, "weight_grump"),
        Properties.getInt(Properties.FULL_MOONS, "weight_seeker"),
        Properties.getInt(Properties.FULL_MOONS, "weight_ghost"),
        Properties.getInt(Properties.FULL_MOONS, "weight_destroyer")
    };

    /** @param mobIndex The mob type to check.
     * @return True if the full moon mob can spawn. */
    public static boolean canSpawn(int mobIndex) {
        return mobIndex >= 0 && ((EventFullMoon) EventBase.fullMoon).mobs[mobIndex] > 0;
    }

    /** @param world The world to generate a mob in.
     * @param mobIndex The type of mob to generate.
     * @return A new instance of the mob type. */
    public static EntityLiving newMob(WorldServer world, int mobIndex) {
        switch (mobIndex) {
            case 0:
                return new EntityBreecher(world);
            case 1:
                return new EntityGrump(world);
            case 2:
                return new EntitySeeker(world);
            case 3:
                return new EntityGhost(world);
            case 4:
                return new EntityDestroyer(world);
        }
        return null;
    }

    /** Decreases the number of a mob that can spawn during the current full moon.
     * @param mobIndex The mob to decrease the count of. */
    public static void decreaseCount(int mobIndex) {
        if (mobIndex >= 0) {
            ((EventFullMoon) EventBase.fullMoon).mobs[mobIndex]--;
        }
    }

    /** @param entity The mob to check.
     * @return The index in mobs[] for a given full moon mob. */
    public static int getMobIndex(EntityLiving entity) {
        if (entity instanceof EntityBreecher)
            return 0;
        else if (entity instanceof EntityGrump)
            return 1;
        else if (entity instanceof EntitySeeker)
            return 2;
        else if (entity instanceof EntityGhost)
            return 3;
        else if (entity instanceof EntityDestroyer)
            return 4;
        return -1;
    }

    /** @return A random index in mobs[] that is valid for spawning, or -1 if there are no valid indexes. */
    public static int nextMobIndex() {
        int totalWeight = 0;
        for (int mobIndex = 0; mobIndex < EventFullMoon.SPAWN_WEIGHTS.length; mobIndex++) {
            if (EventFullMoon.SPAWN_WEIGHTS[mobIndex] > 0 && EventFullMoon.canSpawn(mobIndex)) {
                totalWeight += EventFullMoon.SPAWN_WEIGHTS[mobIndex];
            }
        }
        if (totalWeight > 0) {
            int choice = ApocalypseMod.random.nextInt(totalWeight);
            for (int mobIndex = 0; mobIndex < EventFullMoon.SPAWN_WEIGHTS.length; mobIndex++) {
                if (EventFullMoon.SPAWN_WEIGHTS[mobIndex] > 0 && EventFullMoon.canSpawn(mobIndex)) {
                    choice -= EventFullMoon.SPAWN_WEIGHTS[mobIndex];
                    if (choice < 0)
                        return mobIndex;
                }
            }
        }
        return -1;
    }

    /** Time until mobs can start spawning. */
    private int gracePeriod, baseGracePeriod;
    /** The individual mobs this event can create. */
    public int[] mobs = new int[5];

    public EventFullMoon(int id) {
        super(id);
    }

    @Override
    public String getStartMessage() {
        return "event.Apocalypse.fullMoon";
    }

    @Override
    public boolean canBeInterrupted(EventBase event) {
        return false;
    }

    @Override
    public void onStart() {
        this.gracePeriod = 320;
        long worldDifficulty = WorldDifficultyManager.getWorldDifficulty();
        double effectiveDifficulty;
        int playerCount = 0;
        WorldServer[] worlds = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers;
        for (WorldServer world : worlds) {
            if (world != null) {
                playerCount += world.playerEntities.size();
            }
        }
        if (playerCount == 0) {
            for (int mobIndex = 0; mobIndex < this.mobs.length; mobIndex++) {
                this.mobs[mobIndex] = 0;
            }
            return;
        }

        if (PropHelper.START_BREECHERS >= 0L && PropHelper.START_BREECHERS <= worldDifficulty) {
            effectiveDifficulty = (double) (worldDifficulty - PropHelper.START_BREECHERS) / (double) PropHelper.FULL_MOON_TIME;
            this.mobs[0] = PropHelper.MOON_BREECHERS_MIN + (int) (PropHelper.MOON_BREECHERS * effectiveDifficulty);
        }
        else {
            this.mobs[0] = 0;
        }

        if (PropHelper.START_GRUMPS >= 0L && PropHelper.START_GRUMPS <= worldDifficulty) {
            effectiveDifficulty = (double) (worldDifficulty - PropHelper.START_GRUMPS) / (double) PropHelper.FULL_MOON_TIME;
            this.mobs[1] = PropHelper.MOON_GRUMPS_MIN + (int) (PropHelper.MOON_GRUMPS * effectiveDifficulty);
        }
        else {
            this.mobs[1] = 0;
        }

        if (PropHelper.START_SEEKERS >= 0L && PropHelper.START_SEEKERS <= worldDifficulty) {
            effectiveDifficulty = (double) (worldDifficulty - PropHelper.START_SEEKERS) / (double) PropHelper.FULL_MOON_TIME;
            this.mobs[2] = PropHelper.MOON_SEEKERS_MIN + (int) (PropHelper.MOON_SEEKERS * effectiveDifficulty);
        }
        else {
            this.mobs[2] = 0;
        }

        if (PropHelper.START_GHOSTS >= 0L && PropHelper.START_GHOSTS <= worldDifficulty) {
            effectiveDifficulty = (double) (worldDifficulty - PropHelper.START_GHOSTS) / (double) PropHelper.FULL_MOON_TIME;
            this.mobs[3] = PropHelper.MOON_GHOSTS_MIN + (int) (PropHelper.MOON_GHOSTS * effectiveDifficulty);
        }
        else {
            this.mobs[3] = 0;
        }

        if (PropHelper.START_DESTROYERS >= 0L && PropHelper.START_DESTROYERS <= worldDifficulty) {
            effectiveDifficulty = (double) (worldDifficulty - PropHelper.START_DESTROYERS) / (double) PropHelper.FULL_MOON_TIME;
            this.mobs[4] = PropHelper.MOON_DESTROYERS_MIN + (int) (PropHelper.MOON_DESTROYERS * effectiveDifficulty);
        }
        else {
            this.mobs[4] = 0;
        }

        int totalMobs = 0;
        for (int mobIndex = 0; mobIndex < this.mobs.length; mobIndex++) {
            this.mobs[mobIndex] *= playerCount;
            totalMobs += this.mobs[mobIndex];
        }
        if (totalMobs == 0)
            return;

        this.baseGracePeriod = 5000 / totalMobs / playerCount;
    }

    @Override
    public void update() {
        if (this.gracePeriod > 0) {
            this.gracePeriod -= WorldDifficultyManager.TICKS_PER_UPDATE;
        }
        for (int mobIndex = 0; mobIndex < this.mobs.length; mobIndex++) {
            if (this.mobs[mobIndex] > 0 && EventFullMoon.SPAWN_WEIGHTS[mobIndex] > 0)
                return;
        }
        WorldDifficultyManager.endEvent();
    }

    @Override
    public void update(WorldServer world) {
        if (world.provider.dimensionId == 0 && !WorldDifficultyManager.isFullMoon(world)) {
            WorldDifficultyManager.endEvent();
            return;
        }
        if (this.gracePeriod <= 0) {
            if (SpawnFullMoon.performSpawning(world) > 0) {
                this.gracePeriod = this.baseGracePeriod + world.rand.nextInt(this.baseGracePeriod >> 1);
            }
        }
    }

    @Override
    public void save(DataOutputStream out) throws IOException {
        out.writeInt(this.gracePeriod);
        out.writeInt(this.baseGracePeriod);
        for (int mobType : this.mobs) {
            out.writeInt(mobType);
        }
    }
    @Override
    public void load(DataInputStream in) throws IOException {
        this.gracePeriod = in.readInt();
        this.baseGracePeriod = in.readInt();
        for (int mobIndex = 0; mobIndex < this.mobs.length; mobIndex++) {
            this.mobs[mobIndex] = in.readInt();
        }
    }
}
