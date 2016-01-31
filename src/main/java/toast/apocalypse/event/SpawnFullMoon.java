package toast.apocalypse.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import toast.apocalypse.entity.IFullMoonMob;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * A helper class that handles the spawning of full moon mobs during a full moon event.
 */
public class SpawnFullMoon {

    /** The max number of full moon monsters out at once per 256 loaded chunks. */
    private static final int maxNumberOfCreature = 18;

    /** A map of eligible spawning chunks. */
    private static final HashMap<ChunkCoordIntPair, Boolean> eligibleChunksForSpawning = new HashMap<ChunkCoordIntPair, Boolean>();

    public SpawnFullMoon() {
        FMLCommonHandler.instance().bus().register(this);
    }

    /** @param world The world being spawned into.
     * @param x The x position.
     * @param y The y position.
     * @param z The z position.
     *
     * @return True if a full moon mob can spawn at the given location. */
    public static boolean canMonsterSpawnAtLocation(World world, int x, int y, int z) {
        return !world.getBlock(x, y, z).isCollidable();
    }

    /** Spawns full moon mobs in the world.
     * @param world The world to spawn mobs into.
     *
     * @return The number spawned, for debugging purposes. */
    public static int performSpawning(WorldServer world) {
        SpawnFullMoon.eligibleChunksForSpawning.clear();
        int chunkX, chunkZ;
        for (EntityPlayer entityplayer : (List<EntityPlayer>) world.playerEntities) {
            chunkX = (int) Math.floor(entityplayer.posX / 16.0);
            chunkZ = (int) Math.floor(entityplayer.posZ / 16.0);
            byte spawnRange = 8; // in chunks
            boolean onEdge;
            for (int x = -spawnRange; x <= spawnRange; x++) {
                for (int z = -spawnRange; z <= spawnRange; z++) {
                    onEdge = x == -spawnRange || x == spawnRange || z == -spawnRange || z == spawnRange;
                    ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(x + chunkX, z + chunkZ);
                    if (!onEdge) {
                        SpawnFullMoon.eligibleChunksForSpawning.put(chunkCoords, Boolean.valueOf(false));
                    }
                    else if (!SpawnFullMoon.eligibleChunksForSpawning.containsKey(chunkCoords)) {
                        SpawnFullMoon.eligibleChunksForSpawning.put(chunkCoords, Boolean.valueOf(true));
                    }
                }
            }
        }

        if (world.countEntities(IFullMoonMob.class) <= SpawnFullMoon.maxNumberOfCreature * SpawnFullMoon.eligibleChunksForSpawning.size() / 256) {
            ArrayList<ChunkCoordIntPair> spawnChunks = new ArrayList(SpawnFullMoon.eligibleChunksForSpawning.keySet());
            Collections.shuffle(spawnChunks);
            for (ChunkCoordIntPair chunk : spawnChunks) {
                if (!SpawnFullMoon.eligibleChunksForSpawning.get(chunk).booleanValue()) {
                    ChunkPosition chunkPos = SpawnFullMoon.getRandomSpawningPointInChunk(world, chunk.chunkXPos, chunk.chunkZPos);
                    int x = chunkPos.chunkPosX;
                    int y = chunkPos.chunkPosY;
                    int z = chunkPos.chunkPosZ;
                    Material material = world.getBlock(x, y, z).getMaterial();
                    if (!world.getBlock(x, y, z).isNormalCube() && (material == Material.air || material == Material.water)) {
                        int groupAttempts = 0;
                        while (groupAttempts < 3) {
                            int blockX = x;
                            int blockY = y;
                            int blockZ = z;
                            byte spawnRadius = 6;
                            int mobIndex = EventFullMoon.nextMobIndex();
                            int spawnAttempts = 0;
                            while (true) {
                                if (spawnAttempts < 4) {
                                    blockX += world.rand.nextInt(spawnRadius) - world.rand.nextInt(spawnRadius);
                                    blockZ += world.rand.nextInt(spawnRadius) - world.rand.nextInt(spawnRadius);
                                    float spawnX = blockX + 0.5F;
                                    float spawnY = blockY;
                                    float spawnZ = blockZ + 0.5F;
                                    if (world.getClosestPlayer(spawnX, spawnY, spawnZ, 20.0) == null) {
                                        EntityLiving monster = EventFullMoon.newMob(world, mobIndex);
                                        if (monster == null)
                                            return 0;
                                        monster.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0.0F);
                                        if (monster.getCanSpawnHere()) {
                                            world.spawnEntityInWorld(monster);
                                            EventFullMoon.decreaseCount(mobIndex);
                                            if (!ForgeEventFactory.doSpecialSpawn(monster, world, spawnX, spawnY, spawnZ)) {
                                                monster.onSpawnWithEgg(null);
                                            }
                                            return 1;
                                        }
                                    }
                                    spawnAttempts++;
                                    continue;
                                }
                                groupAttempts++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    /** @param world The world being spawned into.
     * @param chunkX The chunk's x position.
     * @param chunkZ The chunk's z position.
     *
     * @return A randomized position within the given chunk. */
    private static ChunkPosition getRandomSpawningPointInChunk(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int x = (chunkX << 4) + world.rand.nextInt(16);
        int z = (chunkZ << 4) + world.rand.nextInt(16);
        int y = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(x, y, z);
    }
}