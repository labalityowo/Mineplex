package mineplex.game.nano.game.games.findores;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.components.player.GiveItemComponent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;

public class FindOres extends ScoredSoloGame
{

	private enum OreType
	{
		COAL(Material.COAL_ORE, Material.COAL, 1, 600),
		IRON(Material.IRON_ORE, Material.IRON_INGOT, 1, 600),
		GOLD(Material.GOLD_ORE, Material.GOLD_INGOT, 2, 300),
		DIAMOND(Material.DIAMOND_ORE, Material.DIAMOND, 3, 200),
		EMERALD(Material.EMERALD_ORE, Material.EMERALD, 5, 100);

		final Material BlockType;
		final Material DropType;
		final int Points;
		final int MaxVeins;

		OreType(Material blockType, Material dropType, int points, int maxVeins)
		{
			BlockType = blockType;
			DropType = dropType;
			Points = points;
			MaxVeins = maxVeins;
		}
	}

	private final BlockFace[] _faces =
			{
					BlockFace.NORTH,
					BlockFace.EAST,
					BlockFace.SOUTH,
					BlockFace.WEST,
					BlockFace.UP,
					BlockFace.DOWN
			};

	private Location _cornerA, _cornerB;

	public FindOres(NanoManager manager)
	{
		super(manager, GameType.FIND_ORES, new String[]
				{
						C.cYellow + "Mine" + C.Reset + " ores to earn points.",
						C.cAqua + "Diamonds" + C.Reset + " and " + C.cGreen + "Emeralds" + C.Reset + " give more points!",
						C.cYellow + "Most points" + C.Reset + " wins."
				});

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_worldComponent.setBlockBreak(true);

		_endComponent.setTimeout(TimeUnit.SECONDS.toMillis(100));

		new GiveItemComponent(this)
				.setItems(new ItemStack[]
						{
								new ItemBuilder(Material.DIAMOND_PICKAXE)
										.setTitle(C.cAquaB + "Super Pick 5000")
										.setGlow(true)
										.setUnbreakable(true)
										.build()
						});
	}

	@Override
	protected void parseData()
	{
		List<Location> corners = _mineplexWorld.getIronLocations("YELLOW");

		_cornerA = corners.get(0);
		_cornerB = corners.get(1);

		corners.forEach(location ->
		{
			Block block = location.getBlock();
			Block oneUp = block.getRelative(BlockFace.UP);
			Block twoUp = location.getBlock().getRelative(0, 2, 0);

			block.setType(Material.BEDROCK);
			oneUp.setType(twoUp.getType());
			oneUp.setData(twoUp.getData());
		});

		List<Block> blocks = UtilBlock.getInBoundingBox(_cornerA, _cornerB);

		for (OreType oreType : OreType.values())
		{
			for (int i = 0; i < oreType.MaxVeins; i++)
			{
				Block block = UtilAlg.Random(blocks);

				if (block == null)
				{
					break;
				}

				blocks.remove(block);

				int max = UtilMath.rRange(3, 7);

				for (int j = 0; j < max; j++)
				{
					block = block.getRelative(UtilMath.randomElement(_faces));

					if (!isInMap(block))
					{
						break;
					}

					MapUtil.QuickChangeBlockAt(block.getLocation(), oreType.BlockType);
				}
			}
		}
	}

	@EventHandler
	public void playerRespawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockBreak(BlockDamageEvent event)
	{
		if (!isLive())
		{
			return;
		}

		Player player = event.getPlayer();

		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || itemStack.getType() != Material.DIAMOND_PICKAXE)
		{
			player.sendMessage(F.main(getManager().getName(), "Use your pickaxe to mine blocks!"));
			event.setCancelled(true);
			return;
		}

		Block block = event.getBlock();

		if (!isInMap(block))
		{
			player.sendMessage(F.main(getManager().getName(), "You cannot break this block."));
			event.setCancelled(true);
			return;
		}

		for (OreType oreType : OreType.values())
		{
			if (block.getType() != oreType.BlockType)
			{
				continue;
			}

			incrementScore(player, oreType.Points);
			player.getInventory().addItem(new ItemStack(oreType.DropType));
			UtilTextBottom.display(C.cYellow + "+" + oreType.Points + C.cGreen + " Point" + (oreType.Points == 1 ? "" : "s"), player);
			break;
		}

		event.setCancelled(true);
		player.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		block.setType(Material.AIR);
	}

	private boolean isInMap(Block block)
	{
		return UtilAlg.inBoundingBox(block.getLocation().add(0.5, -0.5, 0.5), _cornerA, _cornerB);
	}
}
