package mineplex.game.nano.game.games.copycat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.components.player.NightVisionComponent;
import mineplex.game.nano.game.games.copycat.CopyCat.SealBreakerRoom;
import mineplex.game.nano.game.roomed.Room;
import mineplex.game.nano.game.roomed.RoomedSoloGame;

public class CopyCat extends RoomedSoloGame<SealBreakerRoom>
{

	private static final int X_SIZE = 3, Y_SIZE = 3;

	private final Set<Block> _blocks;
	private final Material[] _material =
			{
					Material.DIRT,
					Material.COBBLESTONE,
					Material.WOOL,
					Material.EMERALD_BLOCK
			};

	public CopyCat(NanoManager manager)
	{
		super(manager, GameType.COPY_CAT, new String[]
				{
						C.cYellow + "Copy" + C.Reset + " the pattern on the " + C.cGreen + "Left" + C.Reset + ".",
						"And replicate it on the " + C.cGreen + "Right" + C.Reset + "!",
						"Use the " + C.cAqua + "Markings" + C.Reset + " on the wall to help.",
						C.cYellow + "Most patterns copied" + C.Reset + " wins!"
				});

		_blocks = new HashSet<>();

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_worldComponent.setBlockPlace(true);
		_worldComponent.setBlockBreak(true);

		_endComponent.setTimeout(TimeUnit.SECONDS.toMillis(90));

		new NightVisionComponent(this);
	}

	@Override
	public void disable()
	{
		super.disable();

		_blocks.clear();
	}

	@Override
	protected SealBreakerRoom addPlayer(Player player, Location location, Map<String, Location> localPoints)
	{
		return new SealBreakerRoom(player, location, localPoints);
	}

	@EventHandler
	public void updateLevels(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isLive())
		{
			return;
		}

		_rooms.forEach((player, room) ->
		{
			if (!player.isFlying())
			{
				UtilTextMiddle.display(null, C.cYellow + "Double Tap Space To Fly", 0, 40, 0, player);
			}

			player.setAllowFlight(true);
			player.setFlying(true);

			if (room.next())
			{
				UtilTextMiddle.display(C.cYellowB + "Level " + room.Level, C.cRed + room.Blocks + " Blocks", 0, 40, 0, player);
				player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

				player.getInventory().clear();

				room.Colours.forEach((material, amount) -> player.getInventory().addItem(new ItemStack(material, amount)));

				if (room.Level == 10)
				{
					addStat(player, "CopyCatLevel10", 1, true, false);
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getBlock();
		SealBreakerRoom room = _rooms.get(player);

		if (room != null && room.isInPasteArea(block))
		{
			_blocks.add(block);
		}
		else
		{
			player.sendMessage(F.main(getManager().getName(), "You can only place blocks in the designated area."));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void blockDamage(BlockDamageEvent event)
	{
		if (!isLive())
		{
			return;
		}

		Block block = event.getBlock();

		if (_blocks.remove(block))
		{
			Player player = event.getPlayer();

			player.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
			player.getInventory().addItem(new ItemStack(block.getType()));
			block.setType(Material.AIR);
		}
		else
		{
			event.setCancelled(true);
		}
	}

	class SealBreakerRoom extends Room
	{

		int Level;
		int Blocks = 3;
		Map<Material, Integer> Colours;
		Block CopyCenter;
		Block PasteCenter;

		SealBreakerRoom(Player player, Location center, Map<String, Location> dataPoints)
		{
			super(player, center, dataPoints);

			Colours = new HashMap<>();
			CopyCenter = dataPoints.get("RED").getBlock();
			PasteCenter = dataPoints.get("ORANGE").getBlock();
		}

		boolean next()
		{
			for (int x = -X_SIZE; x <= X_SIZE; x++)
			{
				for (int y = -Y_SIZE; y <= Y_SIZE; y++)
				{
					Block copy = CopyCenter.getRelative(x, y, 0);
					Block paste = PasteCenter.getRelative(x, y, 0);

					if (copy.getType() != paste.getType())
					{
						return false;
					}
				}
			}

			Level++;
			Blocks++;
			incrementScore(getPlayer(), 1);

			List<Block> blocks = new ArrayList<>();

			for (int x = -X_SIZE; x <= X_SIZE; x++)
			{
				for (int y = -Y_SIZE; y <= Y_SIZE; y++)
				{
					Block block = CopyCenter.getRelative(x, y, 0);
					block.setType(Material.AIR);
					blocks.add(block);

					PasteCenter.getRelative(x, y, 0).setType(Material.AIR);
				}
			}

			Colours.clear();

			for (int i = 0; i < Blocks; i++)
			{
				Block random = UtilAlg.Random(blocks);

				if (random == null)
				{
					return false;
				}

				blocks.remove(random);

				Material material = UtilMath.randomElement(_material);

				random.setType(material);
				Colours.put(material, Colours.getOrDefault(material, 0) + 1);
			}

			return true;
		}

		boolean isInPasteArea(Block block)
		{
			return block.getZ() == PasteCenter.getZ() && block.getX() >= PasteCenter.getX() - X_SIZE && block.getX() <= PasteCenter.getX() + X_SIZE;
		}
	}
}
