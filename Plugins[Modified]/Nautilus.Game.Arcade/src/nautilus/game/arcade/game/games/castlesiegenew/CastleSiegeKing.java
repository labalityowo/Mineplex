package nautilus.game.arcade.game.games.castlesiegenew;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CastleSiegeKing implements Listener
{

	private static final int MAX_HEALTH = 40;
	private static final int DAMAGE_RATE = 400;
	private static final int KING_PROTECTION_RANGE_SQUARED = 4;
	private static final int KING_FENCE_RANGE_SQUARED = 25;
	private static final int KING_TELEPORT_RANGE_SQUARED = 36;

	private final CastleSiegeNew _host;
	private Location _location;
	private LivingEntity _entity;

	private final Map<Player, Integer> _damagers;
	private Player _lastDamager;

	CastleSiegeKing(CastleSiegeNew host)
	{
		_host = host;
		_damagers = new HashMap<>();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		_location = _host.WorldData.GetDataLocs("YELLOW").get(0);
		spawnEntity();
	}

	private void spawnEntity()
	{
		_host.CreatureAllowOverride = true;

		boolean moppleOnline = UtilPlayer.searchExact("Moppletop") != null;
		_entity = _location.getWorld().spawn(_location, Zombie.class);
		UtilEnt.vegetate(_entity);
		UtilEnt.silence(_entity, true);
		_entity.setCustomName(moppleOnline ? C.cGreenB + "Queen Moppletop" : C.cYellowB + "King Chiss");
		_entity.setCustomNameVisible(true);
		_entity.setRemoveWhenFarAway(false);
		_entity.setMaxHealth(MAX_HEALTH);
		_entity.setHealth(MAX_HEALTH);

		EntityEquipment equipment = _entity.getEquipment();

		equipment.setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
		equipment.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		equipment.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		equipment.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		equipment.setBoots(new ItemStack(Material.DIAMOND_BOOTS));

		reset();

		_host.CreatureAllowOverride = false;
	}

	private void reset()
	{
		_entity.teleport(_location);

		List<Location> lookAts = _host.WorldData.GetDataLocs("ORANGE");
		if (!lookAts.isEmpty())
		{
			UtilEnt.CreatureLook(_entity, lookAts.get(0));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void entityDamage(CustomDamageEvent event)
	{
		if (event.isCancelled() || _entity == null || !_entity.equals(event.GetDamageeEntity()))
		{
			return;
		}

		event.SetCancelled("King");

		Player damager = event.GetDamagerPlayer(true);

		if (damager == null)
		{
			return;
		}

		if (_host.getDefenders().HasPlayer(damager) || !Recharge.Instance.use(damager, "Damage King", DAMAGE_RATE, false, false))
		{
			return;
		}

		// Store the damager
		_damagers.putIfAbsent(damager, 0);
		_damagers.put(damager, _damagers.get(damager) + 1);
		_lastDamager = damager;

		_entity.playEffect(EntityEffect.HURT);
		_entity.getWorld().playEffect(_entity.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		_entity.setHealth(_entity.getHealth() - 1);
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _entity == null)
		{
			return;
		}

		double dist = UtilMath.offsetSquared(_entity.getLocation(), _location);

		if (dist > KING_TELEPORT_RANGE_SQUARED)
		{
			reset();
		}
		else if (dist > KING_PROTECTION_RANGE_SQUARED)
		{
			UtilEnt.CreatureMove(_entity, _location, 1);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (_entity == null)
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (event.isCancelled())
		{
			return;
		}

		for (Block nearby : UtilBlock.getSurrounding(block, false))
		{
			if (nearby.isLiquid())
			{
				event.setCancelled(true);
				player.sendMessage(F.main("Game", "You cannot place " + F.elem("Barricade") + " in water."));
				return;
			}
			else if (nearby.getType() == Material.VINE)
			{
				event.setCancelled(true);
				player.sendMessage(F.main("Game", "You cannot place " + F.elem("Barricade") + " near vines."));
				return;
			}
		}

		if (UtilMath.offsetSquared(_entity.getLocation(), block.getLocation()) < KING_FENCE_RANGE_SQUARED)
		{
			event.setCancelled(true);
			player.sendMessage(F.main("Game", "You cannot place " + F.elem("Barricade") + " near " + _entity.getCustomName() + C.mBody + "."));
		}
	}

	public LivingEntity getEntity()
	{
		return _entity;
	}

	public Map<Player, Integer> getDamagers()
	{
		return _damagers;
	}

	public Player getLastDamager()
	{
		return _lastDamager;
	}
}
