package mineplex.core.gadget.gadgets.wineffect;

import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.animation.AnimationPoint;
import mineplex.core.common.animation.Animator;
import mineplex.core.common.animation.AnimatorEntity;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectTornado extends WinEffectGadget
{

	private static final double THETA_MAX = 2 * Math.PI * 6;
	private static final double THETA_INCREMENTATION = Math.PI / 8;
	private static final int ARRAY_SIZE = (int) (THETA_MAX / THETA_INCREMENTATION) + 1;
	private static final double DELTA_Y = 0.5 / (2 * Math.PI);
	private static final double DELTA_R = DELTA_Y / 4;
	private static final List<Location> LOCATION_DELTAS;
	private static final ItemStack[] BLOCKS =
			{
					new ItemStack(Material.DIRT),
					new ItemStack(Material.GRASS)
			};

	static
	{
		LOCATION_DELTAS = new ArrayList<>(ARRAY_SIZE);
		buildTornado();
	}

	private static void buildTornado()
	{
		Location location = new Location(null, 0, 0, 0);
		double radius = 2;
		double y = 0;

		for (double theta = 0; theta < THETA_MAX; theta += THETA_INCREMENTATION)
		{
			double x = radius * Math.cos(theta);
			double z = radius * Math.sin(theta);

			location.add(x, y, z);

			LOCATION_DELTAS.add(location.clone());

			location.subtract(x, y, z);

			y += DELTA_Y;
			radius += DELTA_R;
		}
	}

	private DisguisePlayer _npc;
	private Animator _animator;
	private Map<ArmorStand, Integer> _stands;
	private boolean _spawnBlocks;

	public WinEffectTornado(GadgetManager manager)
	{
		super(manager, "Tornado",
				UtilText.splitLineToArray(
						C.cGray + "Capable of reaching EF-5 on the Fujita scale! This will really show your fellow players how strong you really are.", LineFormat.LORE
				),
				CostConstants.POWERPLAY_BONUS, Material.DIRT, (byte) 0);

		_schematicName = "TornadoPodium";
		setPPCYearMonth(YearMonth.of(2017, Month.SEPTEMBER));
	}

	@Override
	public void play()
	{
		for (Location delta : LOCATION_DELTAS)
		{
			delta.setWorld(_player.getWorld());
		}

		_npc = getNPC(_player, getBaseLocation());

		AnimatorEntity animator = new AnimatorEntity(Manager.getPlugin(), _npc.getEntity().getBukkitEntity());

		animator.addPoint(new AnimationPoint(5, new Vector(0, 0, 0), new Vector(-1, 0.3, 0)));
		animator.addPoint(new AnimationPoint(10, new Vector(0, 0, 0), new Vector(0, 0.3, -1)));
		animator.addPoint(new AnimationPoint(15, new Vector(0, 0, 0), new Vector(1, 0.3, 0)));
		animator.addPoint(new AnimationPoint(20, new Vector(0, 0, 0), new Vector(0, 0.3, 1)));

		animator.setRepeat(true);
		_animator = animator;

		Location location = _npc.getEntity().getBukkitEntity().getLocation();
		location.setDirection(new Vector(0, 0.5, 1));
		Manager.runSyncLater(() ->
		{
			animator.start(location);
			_spawnBlocks = true;
		}, 20);

		_stands = new HashMap<>(ARRAY_SIZE);
	}

	@EventHandler
	public void spawnBlocks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isRunning() || !_spawnBlocks)
		{
			return;
		}

		if (_stands.size() < LOCATION_DELTAS.size())
		{
			_stands.put(spawnStand(), -1);
		}

		for (Entry<ArmorStand, Integer> entry : _stands.entrySet())
		{
			ArmorStand stand = entry.getKey();
			int index = entry.getValue() + 1;

			if (index == LOCATION_DELTAS.size())
			{
				index = 0;
			}

			stand.teleport(getBaseLocation().add(LOCATION_DELTAS.get(index)));
			_stands.put(stand, index);
		}
	}

	private ArmorStand spawnStand()
	{
		ArmorStand stand = _player.getWorld().spawn(getBaseLocation(), ArmorStand.class);
		stand.setGravity(false);
		stand.setVisible(false);
		stand.setHelmet(UtilMath.randomElement(BLOCKS));

		return stand;
	}

	@Override
	public void finish()
	{
		Manager.getDisguiseManager().undisguise(_npc);
		_npc = null;
		_animator.stop();
		_animator = null;
		_stands.clear();
		_stands = null;
		_spawnBlocks = false;
	}

}
