package nautilus.game.arcade.game.games.battleroyale;

import mineplex.core.common.Pair;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Chicken;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class BattleRoyaleSupplyDrop implements Listener
{

	private static final int DRAGON_Y = 120;
	private static long DROP_WAIT = TimeUnit.MINUTES.toMillis(2);
	private static final ItemStack CHEST = new ItemStack(Material.CHEST);
	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.BALL_LARGE)
			.withColor(Color.YELLOW)
			.withFlicker()
			.build();

	private final BattleRoyale _host;
	private final long _start;
	private final Set<Block> _beaconBlocks;

	private Location _dropLocation;

	private ArmorStand _chest;
	private Chicken _seat;
	private final List<Chicken> _chute;

	private boolean _dropped;
	private boolean _opened;
	private boolean _landed;

	BattleRoyaleSupplyDrop(BattleRoyale host, Location dropLocation)
	{
		_host = host;
		_dropLocation = dropLocation.clone();
		_start = System.currentTimeMillis();
		_beaconBlocks = new HashSet<>();
		_chute = new ArrayList<>();

		// Construct a beacon
		for (Pair<Location, Pair<Material, Byte>> pair : UtilBlock.getBeaconBlocks(_dropLocation, (byte) 0))
		{
			// Look it's like a maze
			_beaconBlocks.add(pair.getLeft().getBlock());
			host.getArcadeManager().GetBlockRestore().add(pair.getLeft().getBlock(), pair.getRight().getLeft().getId(), pair.getRight().getRight(), Long.MAX_VALUE);
		}

		_dropLocation.setY(DRAGON_Y);

		UtilServer.RegisterEvents(this);
	}

	@EventHandler
	public void updateDrop(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		 if (UtilTime.elapsed(_start, DROP_WAIT) && !_dropped)
		{
			_dropped = true;
			_host.CreatureAllowOverride = true;

			UtilFirework.playFirework(_dropLocation, FIREWORK_EFFECT);
			_chest = _dropLocation.getWorld().spawn(_dropLocation, ArmorStand.class);
			_chest.setGravity(false);
			_chest.setVisible(false);
			_chest.setHelmet(CHEST);

			_seat = _dropLocation.getWorld().spawn(_dropLocation, Chicken.class);
			UtilEnt.vegetate(_seat);
			UtilEnt.ghost(_seat, true, true);
			UtilEnt.silence(_seat, true);
			_seat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
			_seat.setPassenger(_chest);

			for (int i = 0; i < 6; i++)
			{
				Chicken chicken = _dropLocation.getWorld().spawn(UtilAlg.getRandomLocation(_dropLocation, 2, 0.5, 2).add(0, 5, 0), Chicken.class);
				UtilEnt.vegetate(chicken);
				UtilEnt.ghost(chicken, true, false);
				chicken.setLeashHolder(_seat);
				_chute.add(chicken);
			}

			_host.CreatureAllowOverride = false;
		}
		else if (_dropped && !_landed && UtilEnt.isGrounded(_seat))
		{
			_landed = true;

			Location chest = _seat.getLocation();
			UtilFirework.playFirework(chest, FIREWORK_EFFECT);
			MapUtil.QuickChangeBlockAt(chest, Material.CHEST);
			_dropLocation = chest;

			ChestLootModule lootModule = _host.getModule(ChestLootModule.class);
			lootModule.addChestLocation("Supply Drop", chest);

			_beaconBlocks.forEach(block -> _host.getArcadeManager().GetBlockRestore().restore(block));

			_chest.remove();
			_seat.remove();
			_chute.forEach(Chicken::remove);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.CHEST)
		{
			return;
		}

		if (UtilMath.offsetSquared(block.getLocation(), _dropLocation) < 4)
		{
			_opened = true;
			cleanup();
		}
	}

	public void cleanup()
	{
		_chute.clear();
		UtilServer.Unregister(this);
	}

	public Location getDropLocation()
	{
		return _dropLocation;
	}

	public boolean isOpened()
	{
		return _opened;
	}

	public String getScoreboardString()
	{
		return _dropped ? null : UtilTime.MakeStr(_start + DROP_WAIT - System.currentTimeMillis());
	}
}
