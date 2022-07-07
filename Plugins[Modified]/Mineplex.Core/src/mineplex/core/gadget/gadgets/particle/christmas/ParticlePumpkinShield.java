package mineplex.core.gadget.gadgets.particle.christmas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.arcadeevents.CoreGameStartEvent;
import mineplex.core.arcadeevents.CoreGameStopEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticlePumpkinShield extends ParticleGadget
{

	private static final ItemStack HELMET = new ItemStack(Material.JACK_O_LANTERN);
	private static final int SHIELD_STANDS = 3;
	private static final double INITIAL_THETA = 2 * Math.PI / 16;
	private static final double DELTA_THETA = Math.PI / 40;
	private static final double DELTA_THETA_Y = Math.PI / 35;
	private static final int RADIUS = 2;

	private final Map<Player, List<ArmorStand>> _shield;
	private double _theta;
	private double _thetaY;
	private boolean _inGame;

	public ParticlePumpkinShield(GadgetManager manager)
	{
		super(manager, "Pumpkin Shield",
				UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "You exert a eerie aura around you...",
								C.blankLine,
								C.cBlue + "Earned by defeating the Pumpkin King",
								C.cBlue + "in the 2017 Christmas Chaos II."
						}, LineFormat.LORE),
				CostConstants.NO_LORE, Material.JACK_O_LANTERN, (byte) 0);

		_shield = new HashMap<>();
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		super.enableCustom(player, message);

		spawnArmourStands(player);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		removeArmourStands(player);
	}

	private void spawnArmourStands(Player player)
	{
		if (!isActive(player))
		{
			return;
		}

		removeArmourStands(player);

		Location location = player.getLocation();
		List<ArmorStand> stands = new ArrayList<>(SHIELD_STANDS);

		for (int i = 0; i < SHIELD_STANDS; i++)
		{
			ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);

			stand.setVisible(false);
			stand.setGravity(false);
			stand.setHelmet(HELMET);
			stand.setRemoveWhenFarAway(false);

			UtilEnt.vegetate(stand);

			stands.add(stand);
		}

		_shield.put(player, stands);
	}

	private void removeArmourStands(Player player)
	{
		List<ArmorStand> stands = _shield.remove(player);

		if (stands != null)
		{
			stands.forEach(Entity::remove);
		}
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (_inGame)
		{
			Location location = player.getLocation().add(0, 1, 0);

			if (Manager.isMoving(player))
			{
				UtilParticle.PlayParticleToAll(ParticleType.FLAME, location, 0, 0.2F, 0, 0, 2, ViewDist.NORMAL);
			}
			else if (Math.random() < 0.3)
			{
				UtilParticle.PlayParticleToAll(ParticleType.LAVA, location, 1, 0.8F, 1F, 0, 1, ViewDist.NORMAL);
			}
		}
	}

	@Override
	@EventHandler
	public void Caller(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		super.Caller(event);

		_shield.forEach((player, stands) ->
		{
			Location base = player.getLocation();
			int index = 0;

			for (ArmorStand stand : stands)
			{
				double theta = index++ * INITIAL_THETA + _theta;
				double x = RADIUS * Math.cos(theta);
				double y = Math.sin(theta + _thetaY) * 0.7;
				double z = RADIUS * Math.sin(theta);

				base.add(x, y, z);
				base.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory2d(player.getLocation(), base)));

				stand.teleport(base);
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, base.clone().add(0, 1.7, 0), 0.2F, 0.2F, 0.2F, 0, 1, ViewDist.LONG);

				base.subtract(x, y, z);
			}
		});

		_theta += DELTA_THETA;
		_thetaY += DELTA_THETA_Y;
	}

	@EventHandler
	public void gameStart(CoreGameStartEvent event)
	{
		_inGame = true;
		_active.forEach(this::removeArmourStands);
	}

	@EventHandler
	public void gameStop(CoreGameStopEvent event)
	{
		_inGame = false;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerTeleport(PlayerChangedWorldEvent event)
	{
		if (_inGame)
		{
			return;
		}

		Player player = event.getPlayer();

		if (!isActive(player))
		{
			return;
		}

		Manager.runSyncLater(() ->
		{
			if (isActive(player))
			{
				spawnArmourStands(player);
			}
		}, 20);
	}
}
