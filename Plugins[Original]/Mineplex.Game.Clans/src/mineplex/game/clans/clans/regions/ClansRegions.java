package mineplex.game.clans.clans.regions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.Callback;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.spawn.Spawn;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTeleportEvent;

public class ClansRegions extends MiniPlugin
{
	public final static String DEFAULT_WORLD_NAME = "world";
	public final static int SPAWN_RADIUS = 3;			// Radius of spawn claim area (measured in chunks)
	public final static int SHOP_RADIUS = 6;			// Radius of shop claim area (measured in chunks)
	public final static int FIELDS_RADIUS = 7;			// Radius of fields claim area (measured in chunks)
	public final static int BORDERLANDS_RADIUS = 85;	// Radius of borderlands claim area (measured in chunks)
	public static final int BORDER_RADIUS = 1280;
	
	private ClansManager _manager;
	private World _world;
	
	public ClansRegions(JavaPlugin plugin, ClansManager manager, String worldName)
	{
		super("Clans Regions" + worldName, plugin);
		_manager = manager;
		_world = Bukkit.getWorld(worldName);
		initializeBorder();
	}
	
	public ClansRegions(JavaPlugin plugin, ClansManager manager)
	{
		this(plugin, manager, DEFAULT_WORLD_NAME);
	}
	
	public void initializeRegions()
	{
		Location worldCenter = new Location(_world, 0, 0, 0);

		// Initialize Spawn faction and claims
		claimArea("Spawn", SPAWN_RADIUS, 0, false, true, new Location[] { Spawn.getNorthSpawn(), Spawn.getSouthSpawn() });

		claimArea("Shops", 2, 0, true, false, new Location[]{Spawn.getWestTownCenter(), Spawn.getEastTownCenter()});
		claimArea("Shops", SHOP_RADIUS, 3, false, false, new Location[]{Spawn.getWestTownCenter(), Spawn.getEastTownCenter()});

		// Initialize Fields and Borderlands factions and claims
		claimArea("Fields", FIELDS_RADIUS, 0, false, true, worldCenter);
		claimArea("Borderlands", BORDERLANDS_RADIUS, 48, false, true, worldCenter);
		
		debugClan("Spawn");
		debugClan("Shops");
		debugClan("Fields");
		debugClan("Borderlands");
	}

	public void initializeBorder()
	{
		WorldBorder worldBorder = _world.getWorldBorder();
		worldBorder.setCenter(0, 0);
		worldBorder.setSize(BORDER_RADIUS * 2);
	}
	
	public void debugClan(String clanName)
	{
		ClanInfo clan = _manager.getClan(clanName);
		
		if (clan != null)
		{
			System.out.println("Clan " + clanName + " has " + clan.getClaimSet().size() + " claims!");
		}
		else
		{
			System.out.println("NO CLAN FOUND BY NAME " + clanName);
		}
	}
	
	public void resetRegions()
	{
		clearClaims("Spawn");
		clearClaims("Shops");
		clearClaims("Fields");
		clearClaims("Borderlands");
		initializeRegions();
	}
	
	private void clearClaims(String name)
	{
		ClanInfo clan = _manager.getClan(name);
		
		if (clan == null)
		{
			return;
		}
		
		System.out.println("Clearing claims for " + name + " with clan id " + clan + "!");
		
		for (ClaimLocation chunk : clan.getClaimSet())
		{
			_manager.getClaimMap().remove(chunk);
		}
		
		clan.getClaimSet().clear();
		_manager.getClanDataAccess().getRepository().removeTerritoryClaims(clan.getId());
	}
	
	/**
	 * Initializes a server-side clan and claims area according to the
	 * location and radius properties passed in.
	 * @param clanName - the name of the clan to create/claim territory for
	 * @param locations - the center location to begin the claim
	 * @param chunkRadius - the radius (in chunks) to claim
	 * @param claimOffset - the initial offset in claim (creating a 'hole' with chunk offset radius)
	 * @param safe - whether the chunk claimed is considered a 'safe' (pvp-free) region.
	 */
	public void claimArea(final String clanName, final int chunkRadius, final int claimOffset, final boolean safe, final boolean addNegative, final Location... locations)
	{
		final ClanInfo clan = _manager.getClan(clanName);

		if (clan == null)
		{
			_manager.getClanDataAccess().create("ClansRegions", clanName, true, new Callback<ClanInfo>()
			{
				@Override
				public void run(ClanInfo data)
				{
					if (data != null)
					{
						for (Location location : locations)
						{
							claimArea(data, location, chunkRadius, claimOffset, addNegative, safe);
							log(String.format("Initialized %s faction territory and creation!", clanName));
						}

						debugClan(clanName);
					}
					else
					{
						System.out.println("Clans Regions error!");
						System.out.println("Seek out help!");
					}
				}
			});
		}
		else
		{
			for (Location location : locations)
			{
				claimArea(clan, location, chunkRadius, claimOffset, addNegative, safe);
			}
		}
	}
	
	/*private void claimArea(String clanName, int chunkRadius, int claimOffset, boolean safe, Location... locations)
	{
		claimArea(clanName, chunkRadius, claimOffset, 0, safe, locations);
	}*/ 
	
	private void claimArea(ClanInfo clan, Location location, int chunkRadius, int claimOffset, boolean addNegative, boolean safe)
	{
		int chunkX = location.getChunk().getX();
		int chunkZ = location.getChunk().getZ();
		Set<ClaimLocation> chunks = new HashSet<>();

		int start = addNegative ? -chunkRadius - 1 : -chunkRadius;

		for (int xOffset = start; xOffset <= chunkRadius; xOffset++)
		{
			for (int zOffset = start; zOffset <= chunkRadius; zOffset++)
			{
				int x = chunkX + xOffset;
				int z = chunkZ + zOffset;
				ClaimLocation chunk = ClaimLocation.of(location.getWorld().getName(), x, z);

				if (addNegative)
				{
					if (((xOffset < 0 && Math.abs(xOffset) < (claimOffset + 1)) || (xOffset >= 0 && xOffset < claimOffset)) &&
							((zOffset < 0 && Math.abs(zOffset) < (claimOffset + 1)) || (zOffset >= 0 && zOffset < claimOffset)))
						continue;
				}
				else
				{
					if ((Math.abs(xOffset) < claimOffset) && (Math.abs(zOffset) < claimOffset))
						continue;
				}

				if (_manager.getClaimMap().containsKey(chunk))
				{
//					System.out.println("get claim map contains " + chunkStr); // this is really really slowing server startup down. just saying.
					continue;
				}

				chunks.add(chunk);
			}
		}
		
		if (chunks.size() > 0)
		{
			_manager.getClanDataAccess().claimAll(clan.getName(), "ClansRegions", safe, chunks.toArray(new ClaimLocation[chunks.size()]));
		}
	}

	@EventHandler
	public void onSkillTeleport(SkillTeleportEvent event)
	{
		if (!isInsideBorders(event.getDestination()))
		{
			event.setCancelled(true);
		}
	}

	private boolean isInsideBorders(Location location)
	{
		return Math.abs(location.getBlockX()) < BORDER_RADIUS && Math.abs(location.getBlockZ()) < BORDER_RADIUS;
	}
}
