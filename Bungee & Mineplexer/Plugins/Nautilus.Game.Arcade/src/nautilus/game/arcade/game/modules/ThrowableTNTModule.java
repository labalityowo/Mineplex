package nautilus.game.arcade.game.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.recharge.Recharge;

public class ThrowableTNTModule extends Module
{

	private static final int EXPLOSION_RADIUS = 14;

	private final Map<Entity, Player> _throwers;

	private ItemStack _tntItem;
	private int _fuseTicks = 60;
	private boolean _throwAndDrop;
	private double _throwStrength = 1.3;

	public ThrowableTNTModule()
	{
		_throwers = new HashMap<>();
	}

	@Override
	protected void setup()
	{
		ItemBuilder builder = new ItemBuilder(Material.TNT);

		if (_throwAndDrop)
		{
			builder.setTitle(C.cYellowB + "Left Click - Throw" + C.cWhite + " / " + C.cYellowB + "Right Click - Drop");
		}
		else
		{
			builder.setTitle(C.cYellow + "Throwable TNT");
		}

		_tntItem = builder.build();
	}

	public ThrowableTNTModule setFuseTicks(int fuseTicks)
	{
		_fuseTicks = fuseTicks;
		return this;
	}

	public ThrowableTNTModule setThrowAndDrop(boolean throwAndDrop)
	{
		_throwAndDrop = throwAndDrop;
		return this;
	}

	public ThrowableTNTModule setThrowStrength(double throwStrength)
	{
		_throwStrength = throwStrength;
		return this;
	}

	public ItemStack getTntItem()
	{
		return _tntItem;
	}

	@EventHandler
	public void playerThrowTNT(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL || !getGame().IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (UtilPlayer.isSpectator(player) || itemStack == null || itemStack.getType() != Material.TNT || UtilBlock.usable(event.getClickedBlock()) || !Recharge.Instance.use(player, "Throw TNT", 500, false, true))
		{
			return;
		}

		player.setItemInHand(UtilInv.decrement(itemStack));
		event.setCancelled(true);

		Location location = player.getEyeLocation();
		location.add(location.getDirection());

		TNTPrimed tntPrimed = location.getWorld().spawn(location, TNTPrimed.class);
		tntPrimed.setFuseTicks(_fuseTicks);

		if (!_throwAndDrop || UtilEvent.isAction(event, ActionType.L))
		{
			UtilAction.velocity(tntPrimed, location.getDirection(), _throwStrength, false, 0, 0.3, 1, false);
		}

		_throwers.put(tntPrimed, player);
		getGame().getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.GAME_THROW_TNT, getGame().GetType().getDisplay(), null);
	}

	@EventHandler
	public void onExplosionPrime(ExplosionPrimeEvent event)
	{
		Entity entity = event.getEntity();
		Player player = _throwers.get(entity);

		if (player != null)
		{
			for (Player other : UtilPlayer.getNearby(entity.getLocation(), EXPLOSION_RADIUS))
			{
				getGame().getArcadeManager().GetCondition().Factory().Explosion("Throwable TNT", other, player, 50, 0.1, false, false);
			}
		}
	}
}
