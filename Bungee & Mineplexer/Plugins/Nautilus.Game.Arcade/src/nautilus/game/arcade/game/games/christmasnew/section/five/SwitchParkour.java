package nautilus.game.arcade.game.games.christmasnew.section.five;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.hologram.Hologram;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;

class SwitchParkour extends SectionChallenge
{

	private static final ItemStack STAY_ON_RED = new ItemBuilder(Material.WOOL, (byte) 14)
			.setTitle(C.cRedB + "STAY ON RED")
			.build();
	private static final ItemStack STAY_ON_GREEN = new ItemBuilder(Material.WOOL, (byte) 13)
			.setTitle(C.cDGreenB + "STAY ON GREEN")
			.build();
	private static final int[] STAY_ON_SLOTS = {5, 6, 7};

	private final List<Location> _black;
	private final List<Location> _white;

	private boolean _enabled;
	private boolean _blackSolid;
	private boolean _removeTick;

	private final Location _checkpointTrigger;
	private final Hologram _checkpointHologram;
	private final List<Location> _checkpoint;

	private boolean _hasCheckpointed;

	SwitchParkour(ChristmasNew host, Location present, Section section)
	{
		super(host, present, section);

		_black = _worldData.GetDataLocs("BLACK");
		_white = _worldData.GetDataLocs("WHITE");

		_checkpointTrigger = _worldData.GetCustomLocs(String.valueOf(Material.LAPIS_BLOCK.getId())).get(0).getBlock().getLocation();
		_checkpointHologram = new Hologram(host.getArcadeManager().getHologramManager(), _checkpointTrigger.clone().add(0.5, 1, 0.5), C.cGoldB + "Checkpoint")
				.setInteraction((player, clickType) -> activateCheckpoint(player));
		_checkpoint = _worldData.GetCustomLocs(String.valueOf(Material.LAPIS_ORE.getId()));
		_checkpoint.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));

		MapUtil.QuickChangeBlockAt(_checkpointTrigger, Material.LEVER, (byte) 3);
	}

	@Override
	public void onPresentCollect()
	{
		_enabled = false;

		for (Player player : _host.GetPlayers(true))
		{
			player.getInventory().remove(Material.WOOL);
		}
	}

	@Override
	public void onRegister()
	{
		_host.getArcadeManager().runSyncLater(() ->
		{
			_checkpointHologram.start();
			_enabled = true;
		}, Section5.TICKS_TO_DELAY);
	}

	@Override
	public void onUnregister()
	{
		_white.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
		_black.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));

		_checkpointHologram.stop();
	}

	@EventHandler
	public void updateSolids(UpdateEvent event)
	{
		if (!_enabled || event.getType() != UpdateType.SLOW)
		{
			return;
		}

		if (_removeTick)
		{
			if (_blackSolid)
			{
				_white.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
			}
			else
			{
				_black.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
			}
		}
		else
		{
			if (_blackSolid)
			{
				_white.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.WOOL, (byte) 14));
			}
			else
			{
				_black.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.WOOL, (byte) 13));
			}

			for (Player player : _host.GetPlayers(true))
			{
				Inventory inventory = player.getInventory();

				inventory.remove(Material.WOOL);
				for (int slot : STAY_ON_SLOTS)
				{
					inventory.setItem(slot, _blackSolid ? STAY_ON_RED : STAY_ON_GREEN);
				}
			}

			_blackSolid = !_blackSolid;
		}

		_removeTick = !_removeTick;
	}

	@EventHandler
	public void checkpointInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.LEVER)
		{
			return;
		}

		activateCheckpoint(player);
	}

	private void activateCheckpoint(Player player)
	{
		if (_hasCheckpointed || UtilPlayer.isSpectator(player))
		{
			return;
		}

		_hasCheckpointed = true;

		_checkpointTrigger.getWorld().playSound(_checkpointTrigger, Sound.LEVEL_UP, 2, 0.5F);
		MapUtil.QuickChangeBlockAt(_checkpointTrigger, Material.AIR);
		_checkpointHologram.stop();

		_host.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{

			int y = _checkpointTrigger.getBlockY();

			@Override
			public void run()
			{
				_checkpoint.removeIf(location ->
				{
					if (location.getY() > y)
					{
						location.getWorld().playSound(location, Sound.PISTON_RETRACT, 2, 0.6F);
						MapUtil.QuickChangeBlockAt(location, Material.COBBLESTONE_STAIRS, (byte) 1);
						return true;
					}

					return false;
				});

				if (_checkpoint.isEmpty())
				{
					cancel();
				}
				else
				{
					y--;
				}
			}
		}, 0, 6);
	}
}
