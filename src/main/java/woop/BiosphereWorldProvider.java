package woop;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;

public class BiosphereWorldProvider extends WorldProviderSurface
{
	private ModConfig config;

	@Override
	public ChunkCoordinates getRandomizedSpawnPoint()
	{
		ChunkCoordinates coords = super.getRandomizedSpawnPoint();

		TryFixSpawnLocation(coords);

		return coords;
	}

	@Override
	public ChunkCoordinates getSpawnPoint()
	{
		ChunkCoordinates coords = super.getSpawnPoint();

		TryFixSpawnLocation(coords);

		return coords;
	}

	private boolean SpawnedOnTopOfDome(ChunkCoordinates coords)
	{
		// Sanity Checking
		if (coords == null) { return false; }

		World world = super.worldObj;
		if (!BiosphereWorldType.IsBiosphereWorld(world)) { return false; }

		if (config == null || config.World != world)
		{
			config = ModConfig.get(world);
		}

		int domeBlockCount = 0;

		for (double yo = -10; yo <= 10; yo++)
		{

			int y = (int)Math.round(coords.posY + yo);
			Block block = world.getBlock(coords.posX, y, coords.posZ);

			// System.out.println("SPAWN BLOCK [" + x + ", " + y + ", "
			// + z + "](" + yo + "): " + WoopMod.GetNameOrIdForBlock(block));

			if (block == config.getDomeBlock())
			{
				domeBlockCount++;

				if (domeBlockCount > 3)
				{
					// not dome world.
					return false;
				}
			}
			else if (block != Blocks.air)
			{
				if (block != config.getOutsideFillerBlock()) { return false; }
			}
		}

		return domeBlockCount >= 1; // spawned on top of a dome!!
	}

	private boolean ValidSpawnLocation(ChunkCoordinates coords)
	{
		if (coords == null) { return true; }

		World world = this.worldObj;
		if (world == null) { return true; }

		int x = coords.posX;
		int y = coords.posY;
		int z = coords.posZ;

		for (int i = 0; i < 3; i++)
		{
			Block block = world.getBlock(x, y + i, z);

			if (block != Blocks.air)
			{
				// trying to spawn in the middle of non-empty blocks
				return false;
			}
		}

		Block under = world.getBlock(x, y - 1, z);
		if (!under.isOpaqueCube()) { return false; }
		if (under.isAir(world, x, y - 1, z)) { return false; }

		return true;
	}

	public boolean TryFixSpawnLocation(ChunkCoordinates coords)
	{
		if (coords == null) { return false; }

		World world = this.worldObj;
		if (world == null) { return false; }

		boolean locationModified = false;
		int orgPosY = coords.posY;

		while (SpawnedOnTopOfDome(coords))
		{
			locationModified = true;
			coords.posY -= 8;
			
			if (coords.posY < 0)
			{
				coords.posY = orgPosY;
				return false;
			}

			while (!ValidSpawnLocation(coords))
			{
				coords.posY -= 1;
				if (coords.posY < 0)
				{
					coords.posY = orgPosY;
					return false;
				}
			}
		}

		return locationModified;
	}
}