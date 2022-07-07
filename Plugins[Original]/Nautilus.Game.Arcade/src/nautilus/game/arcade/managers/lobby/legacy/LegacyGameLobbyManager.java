package nautilus.game.arcade.managers.lobby.legacy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;

import com.google.common.collect.Lists;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.game.kit.KitAvailability;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.KitSorter;
import nautilus.game.arcade.managers.LobbyEnt;
import nautilus.game.arcade.managers.lobby.LobbyManager;

public class LegacyGameLobbyManager extends LobbyManager
{

	private Location _kitDisplay;
	private Location _teamDisplay;

	public LegacyGameLobbyManager(ArcadeManager manager)
	{
		super(
				manager,
				// Missions
				new Location(WORLD, -3.5, 102, 3.5, -145, 0),
				null,
				// Spawn
				new Location(WORLD, 0, 104, 0),
				// Amp Stand
				new Location(WORLD, 0, 102.5, -15)
		);

		setGameText(new Location(WORLD, 0, 130, 50));
		setKitText(new Location(WORLD, -40, 120, 0));
		setTeamText(new Location(WORLD, 40, 120, 0));
		setAdvText(new Location(WORLD, 0, 140, -60));

		setPodium(new Location(WORLD, 0, 101.5, -15), Material.EMERALD_BLOCK.getId(), (byte) 0);

		_kitDisplay = new Location(WORLD, -17, 101, 0);

		float yaw = UtilAlg.GetYaw(UtilAlg.getTrajectory(_kitDisplay, getSpawn()));
		yaw = Math.round(yaw / 90) * 90;
		_kitDisplay.setYaw(yaw);

		_teamDisplay = new Location(WORLD, 18, 101, 0);

		yaw = UtilAlg.GetYaw(UtilAlg.getTrajectory(_teamDisplay, getSpawn()));
		yaw = Math.round(yaw / 90) * 90;
		_teamDisplay.setYaw(yaw);
	}

	private void generateTeams(List<GameTeam> teams, double space, double offset)
	{
		for (int i = 0; i < teams.size(); i++)
		{
			Location entLoc = _teamDisplay.clone().subtract(0, 0, i * space - offset);

			setKitTeamBlocks(entLoc.clone(), 35, teams.get(i).GetColorData(), getTeamBlocks());

			entLoc.add(0, 1.5, 0);

			entLoc.getChunk().load();

			Sheep ent = (Sheep) _manager.GetCreature().SpawnEntity(entLoc, EntityType.SHEEP);
			ent.setRemoveWhenFarAway(false);
			ent.setCustomNameVisible(true);

			ent.setColor(DyeColor.getByWoolData(teams.get(i).GetColorData()));

			UtilEnt.vegetate(ent, true);
			UtilEnt.setFakeHead(ent, true);
			UtilEnt.ghost(ent, true, false);

			teams.get(i).SetTeamEntity(ent);

			getTeams().put(ent, new LobbyEnt(ent, entLoc, teams.get(i)));
		}
	}

	@Override
	public void createTeams(Game game)
	{
		//Text
		writeTeamLine("Select", 0, 159, (byte) 15);
		writeTeamLine("Team", 1, 159, (byte) 4);

		//Smash
		if (game.HideTeamSheep)
		{
			//Text
			writeTeamLine("Select", 0, 159, (byte) 15);
			writeTeamLine("Kit", 1, 159, (byte) 4);
			return;
		}

		//Standard
		if ((game.GetKits().length > 1 || game.GetTeamList().size() < 6) && game.GetType() != GameType.SurvivalGamesTeams)
		{
			List<GameTeam> teams = game.GetTeamList().stream().filter(GameTeam::GetVisible).collect(Collectors.toList());

			double space = 6;
			double offset = (teams.size() - 1) * space / 2d;
			generateTeams(teams, space, offset);
		}
		//Double
		else
		{
			//Text
			writeKitLine("Select", 0, 159, (byte) 15);
			writeKitLine("Team", 1, 159, (byte) 4);

			//Display
			List<GameTeam> teamsA = Lists.newArrayList();
			List<GameTeam> teamsB = Lists.newArrayList();

			for (int i = 0; i < game.GetTeamList().size(); i++)
			{
				if (i < game.GetTeamList().size() / 2)
				{
					teamsA.add(game.GetTeamList().get(i));
				} else
				{
					teamsB.add(game.GetTeamList().get(i));
				}
			}

			//A
			{
				double space = 6;
				double offset = (teamsA.size() - 1) * space / 2d;
				generateTeams(teamsA, space, offset);
			}
			//B
			{
				double space = 6;
				double offset = (teamsB.size() - 1) * space / 2d;
				generateTeams(teamsB, space, offset);
			}
		}
	}

	@Override
	public void createKits(Game game)
	{
		//Text
		writeKitLine("Select", 0, 159, (byte) 15);
		writeKitLine("Kit", 1, 159, (byte) 4);

		//Display
		List<Kit> kits = Lists.newArrayList();
		for (Kit kit : game.GetKits())
		{
			if (kit == null)
			{
				continue;
			}

			if (kit.GetAvailability() != KitAvailability.Hide)
			{
				kits.add(kit);
			}
		}

		// Break up the kits into chunks with respect to Null Kits
		List<List<Kit>> kitChunks = Lists.newArrayList();

		int lastBreak = 0;
		for (int i = 0; i < kits.size(); i++)
		{
			if (i == kits.size() - 1 || kits.get(i).GetAvailability() == KitAvailability.Null)
			{
				kitChunks.add(kits.subList(lastBreak, i + 1));
				lastBreak = i + 1;
			}
		}

		// Sort each kit chunk
		for (List<Kit> kitList : kitChunks)
		{
			kitList.sort(new KitSorter());
		}
		
		// Create the new sorted list
		kits = Lists.newArrayList();
		
		for (List<Kit> kitList : kitChunks)
		{
			kits.addAll(kitList);
		}

		//Smash
		if (game.ReplaceTeamsWithKits)
		{
			int divide = kits.size() / 2;
			{
				//Positions
				double space = 4;
				double offset = (divide) * space / 2d;

				for (int i = 0; i <= divide; i++)
				{
					Kit kit = kits.get(i);

					if (kit.GetAvailability() == KitAvailability.Null)
					{
						continue;
					}

					Location entLoc = _kitDisplay.clone().subtract(0, 0, i * space - offset);

					byte data = 4;

					if (kit.GetAvailability() == KitAvailability.Gem)
					{
						data = 5;
					} else if (kit.GetAvailability() == KitAvailability.Achievement)
					{
						data = 2;
					}
					
					setKitTeamBlocks(entLoc.clone(), 35, data, getKitBlocks());

					entLoc.add(0, 1.5, 0);

					kit.getGameKit().createNPC(entLoc);

					System.out.println("Creating Kit NPC: " + kit.GetName() + " at " + UtilWorld.locToStrClean(entLoc));
				}
			}
			{
				//Positions
				double space = 4;
				double offset = (divide - 1) * space / 2d;

				for (int i = 1; i < kits.size() - divide; i++)
				{
					Kit kit = kits.get(i + divide);

					if (kit.GetAvailability() == KitAvailability.Null)
					{
						continue;
					}

					Location entLoc = _teamDisplay.clone().subtract(0, 0, i * space - offset);

					byte data = 4;

					if (kit.GetAvailability() == KitAvailability.Gem)
					{
						data = 5;
					} else if (kit.GetAvailability() == KitAvailability.Achievement)
					{
						data = 2;
					}

					setKitTeamBlocks(entLoc.clone(), 35, data, getKitBlocks());

					entLoc.add(0, 1.5, 0);

					kit.getGameKit().createNPC(entLoc);

					System.out.println("Creating Kit NPC: " + kit.GetName() + " at " + UtilWorld.locToStrClean(entLoc));
				}
			}

			return;
		}

		//Positions
		double space = 4;
		double offset = (kits.size() - 1) * space / 2d;

		for (int i = 0; i < kits.size(); i++)
		{
			Kit kit = kits.get(i);

			if (kit.GetAvailability() == KitAvailability.Null)
			{
				continue;
			}

			Location entLoc = _kitDisplay.clone().subtract(0, 0, i * space - offset);

			byte data = 4;

			if (kit.GetAvailability() == KitAvailability.Gem)
			{
				data = 5;
			} else if (kit.GetAvailability() == KitAvailability.Achievement)
			{
				data = 2;
			}

			setKitTeamBlocks(entLoc.clone(), 35, data, getKitBlocks());

			entLoc.add(0, 1.5, 0);

			kit.getGameKit().createNPC(entLoc);
		}
	}

	public void setKitTeamBlocks(Location loc, int id, byte data, Map<Block, Material> blockMap)
	{
		//Coloring
		Block block = loc.clone().add(0.5, 0, 0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		block = loc.clone().add(-0.5, 0, 0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		block = loc.clone().add(0.5, 0, -0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		block = loc.clone().add(-0.5, 0, -0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		//Top
		block = loc.clone().add(0.5, 1, 0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte) 5);

		block = loc.clone().add(-0.5, 1, 0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte) 5);

		block = loc.clone().add(0.5, 1, -0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte) 5);

		block = loc.clone().add(-0.5, 1, -0.5).getBlock();
		blockMap.put(block, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte) 5);

		//Floor
		for (int x = -2; x < 2; x++)
		{
			for (int z = -2; z < 2; z++)
			{
				block = loc.clone().add(x + 0.5, -1, z + 0.5).getBlock();

				blockMap.put(block, block.getType());
				MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);
			}
		}

		//Outline
		for (int x = -3; x < 3; x++)
		{
			for (int z = -3; z < 3; z++)
			{
				block = loc.clone().add(x + 0.5, -1, z + 0.5).getBlock();

				if (blockMap.containsKey(block))
				{
					continue;
				}

				blockMap.put(block, block.getType());
				MapUtil.QuickChangeBlockAt(block.getLocation(), 35, (byte) 15);
			}
		}
	}

	public void setPodium(Location loc, int id, byte data)
	{
		HashSet<Block> blockSet = new HashSet<>();
		//Coloring
		Block block = loc.clone().add( 0.5, 0,  0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		block = loc.clone().add(-0.5, 0,  0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		block = loc.clone().add( 0.5, 0, -0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		block = loc.clone().add(-0.5, 0, -0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);

		//Top
		block = loc.clone().add( 0.5, 1,  0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte)5);

		block = loc.clone().add(-0.5, 1,  0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte)5);

		block = loc.clone().add( 0.5, 1, -0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte)5);

		block = loc.clone().add(-0.5, 1, -0.5).getBlock();
		blockSet.add(block);
		MapUtil.QuickChangeBlockAt(block.getLocation(), 44, (byte)5);

		//Floor
		for (int x=-2 ; x<2 ; x++)
		{
			for (int z=-2 ; z<2 ; z++)
			{
				block = loc.clone().add(x + 0.5, -1,  z + 0.5).getBlock();
				blockSet.add(block);

				MapUtil.QuickChangeBlockAt(block.getLocation(), id, data);
			}
		}

		//Outline
		for (int x=-3 ; x<3 ; x++)
		{
			for (int z=-3 ; z<3 ; z++)
			{
				block = loc.clone().add(x + 0.5, -1,  z + 0.5).getBlock();
				if (blockSet.contains(block)) continue;

				MapUtil.QuickChangeBlockAt(block.getLocation(), 35, (byte)15);
			}
		}
	}
}
