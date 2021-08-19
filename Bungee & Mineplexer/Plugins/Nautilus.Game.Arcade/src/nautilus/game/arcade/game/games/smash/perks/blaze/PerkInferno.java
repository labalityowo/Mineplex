package nautilus.game.arcade.game.games.smash.perks.blaze;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkInferno extends SmashPerk
{

	private static final float MAX_ENERGY = 0.999F;
	private static final ItemStack POWDER = new ItemStack(Material.BLAZE_POWDER);

	private float _energyTick = 0.025F;
	private float _energyItem = 0.035F;

	private double _itemExpireTime = 0.7;
	private double _itemBurnTime = 0.5;
	private double _itemDamage = 0.25;
	private float _itemVelocityMagnitude = 1.6F;

	private Map<UUID, Long> _active = new HashMap<>();

	public PerkInferno()
	{
		super("Inferno", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Inferno" });
	}

	@Override
	public void setupValues()
	{
		_energyTick = getPerkFloat("Energy Tick");
		_energyItem = getPerkFloat("Energy Item");
		_itemExpireTime = getPerkDouble("Expire Time");
		_itemBurnTime = getPerkDouble("Burn Time");
		_itemDamage = getPerkDouble("Damage");
		_itemVelocityMagnitude = getPerkFloat("Velocity Magnitude");
	}

	@EventHandler
	public void EnergyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
			{
				continue;
			}

			if (!player.isBlocking())
			{
				player.setExp(Math.min(MAX_ENERGY, player.getExp() + _energyTick));
			}
		}
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isSword(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (isSuperActive(player))
		{
			return;
		}

		_active.put(player.getUniqueId(), System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player cur : UtilServer.getPlayers())
		{
			UUID key = cur.getUniqueId();

			if (!_active.containsKey(key))
			{
				continue;
			}

			if (!cur.isBlocking())
			{
				_active.remove(key);
				continue;
			}

			cur.setExp(cur.getExp() - _energyItem);

			if (cur.getExp() <= 0)
			{
				_active.remove(key);
				continue;
			}

			// Fire
			Location location = cur.getEyeLocation();
			Item fire = cur.getWorld().dropItem(location, POWDER);
			Manager.GetFire().Add(fire, cur, _itemExpireTime, 0, _itemBurnTime, _itemDamage, GetName(), false);

			fire.setVelocity(location.getDirection().multiply(_itemVelocityMagnitude));

			// Effect
			cur.getWorld().playSound(location, Sound.GHAST_FIREBALL, 0.1f, 1f);
		}
	}
	
//	private Vector getRandomVector()
//	{
//		double x = 0.07 - (UtilMath.r(14) / 100);
//		double y = 0.07 - (UtilMath.r(14) / 100);
//		double z = 0.07 - (UtilMath.r(14) / 100);
//
//		return new Vector(x, y, z);
//	}
}
