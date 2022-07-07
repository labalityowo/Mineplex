package nautilus.game.arcade.game.games.smash.perks.skeleton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.jooq.util.derby.sys.Sys;

public class PerkBarrage extends SmashPerk
{
	private Map<UUID, Integer> _charge = new HashMap<>();
	private Map<UUID, Long> _chargeLast = new HashMap<>();

	private Set<UUID> _firing = new HashSet<>();
	private Set<Projectile> _arrows = new HashSet<Projectile>();

	private int _max;
	private int _tick;
	private boolean _remove;
	private boolean _noDelay;
	private boolean _useExp;

	public PerkBarrage()
	{
		this(0, 0, false, false);
	}

	public PerkBarrage(int max, int tick, boolean remove, boolean noDelay)
	{
		this(max, tick, remove, noDelay, false);
	}

	public PerkBarrage(int max, int tick, boolean remove, boolean noDelay, boolean useExpAndBar)
	{
		super("Barrage", new String[] { C.cYellow + "Charge" + C.cGray + " your Bow to use " + C.cGreen + "Barrage" });
		_useExp = useExpAndBar;
		_max = max;
		_tick = tick;
		_remove = remove;
		_noDelay = noDelay;
	}

	@Override
	public void setupValues()
	{
		_max = getPerkInt("Max", _max);
		_tick = getPerkInt("Tick", _tick);
		_remove = getPerkBoolean("Remove", _remove);
		_noDelay = getPerkBoolean("No Delay", _noDelay);
		_useExp = getPerkBoolean("Exp Bar", _useExp);
	}

	@EventHandler
	public void BarrageDrawBow(PlayerInteractEvent event)
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

		if (!UtilGear.isBow(player.getItemInHand()))
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
		
		if (!player.getInventory().contains(Material.ARROW))
		{
			return;
		}
		
		if (event.getClickedBlock() != null)
		{
			if (UtilBlock.usable(event.getClickedBlock()))
			{
				return;
			}
		}
		
		// Start Charge
		_charge.put(player.getUniqueId(), 0);
		_chargeLast.put(player.getUniqueId(), System.currentTimeMillis());
		_firing.remove(player.getUniqueId());
	}

	@EventHandler
	public void BarrageCharge(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (Player cur : UtilServer.getPlayers())
		{
			UUID key = cur.getUniqueId();
			
			// Not Charging
			if (!_charge.containsKey(key))
			{
				continue;
			}
			
			if (_firing.contains(key))
			{
				continue;
			}
			
			// Max Charge
			if (_charge.get(key) >= _max)
			{
				continue;
			}
			
			// Charge Interval
			if (_charge.get(key) == 0)
			{
				if (!UtilTime.elapsed(_chargeLast.get(key), 1000))
				{
					continue;
				}
			}	
			else
			{
				if (!UtilTime.elapsed(_chargeLast.get(key), _tick))
				{
					continue;
				}
			}

			// No Longer Holding Bow
			if (cur.getItemInHand() == null || cur.getItemInHand().getType() != Material.BOW)
			{
				if (_useExp)
				{
					cur.setExp(0f);
				}
				_charge.remove(key);
				_chargeLast.remove(key);
				continue;
			}

			// Increase Charge
			_charge.put(key, _charge.get(key) + 1);

			if (_useExp)
			{
				cur.setExp(Math.min(0.9999f, (float) _charge.get(key) / (float) _max));
			}
			_chargeLast.put(key, System.currentTimeMillis());

			// Effect
			cur.playSound(cur.getLocation(), Sound.CLICK, 1f, 1f + (0.1f * _charge.get(key)));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void BarrageFireBow(EntityShootBowEvent event)
	{
		if (event.isCancelled())
		{
			return;

		}
		if (!Manager.GetGame().IsLive())
		{
			return;
		}
		
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		
		if (!(event.getProjectile() instanceof Arrow))
		{
			return;
		}
		
		Player player = (Player) event.getEntity();

		if (!_charge.containsKey(player.getUniqueId()))
		{
			return;
		}
		
		// Start Barrage
		_firing.add(player.getUniqueId());
		_chargeLast.put(player.getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void BarrageArrows(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Set<Player> remove = new HashSet<Player>();

		for (UUID key : _firing)
		{
			Player cur = UtilPlayer.searchExact(key);
			
			if (cur == null)
			{
				continue;
			}
			
			if (!_charge.containsKey(key) || !_chargeLast.containsKey(key))
			{
				remove.add(cur);
				continue;
			}

			if (cur.getItemInHand() == null || cur.getItemInHand().getType() != Material.BOW)
			{
				remove.add(cur);
				continue;
			}

			int arrows = _charge.get(key);
			if (arrows <= 0)
			{
				remove.add(cur);
				continue;
			}

			_charge.put(key, arrows - 1);
			if (_useExp)
			{
				cur.setExp(Math.min(0.9999f, _charge.get(key) / _max));
			}

			// Fire Arrow
			Vector random = new Vector((Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10);
			Projectile arrow = cur.launchProjectile(Arrow.class);
			arrow.setVelocity(cur.getLocation().getDirection().add(random).multiply(3));
			_arrows.add(arrow);
			cur.getWorld().playSound(cur.getLocation(), Sound.SHOOT_ARROW, 1f, 1f);
		}

		for (Player cur : remove)
		{	
			if (_useExp)
			{
				cur.setExp(0f);
			}
			_charge.remove(cur.getUniqueId());
			_chargeLast.remove(cur.getUniqueId());
			_firing.remove(cur.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void BarrageDamageTime(CustomDamageEvent event)
	{
		if (!_noDelay)
		{
			return;
		}
		
		if (event.GetProjectile() == null)
		{
			return;
		}
		
		if (event.GetDamagerPlayer(true) == null)
		{
			return;
		}
		
		if (!(event.GetProjectile() instanceof Arrow))
		{
			return;
		}
		
		Player damager = event.GetDamagerPlayer(true);

		if (!hasPerk(damager))
		{
			return;
		}
		
		event.SetCancelled("Barrage Cancel");

		event.GetProjectile().remove();

		// Damage Event
		Manager.GetDamage().NewDamageEvent(event.GetDamageeEntity(), damager, null, DamageCause.THORNS, event.GetDamage(), true, true, false, damager.getName(), GetName());
	}

	@EventHandler
	public void BarrageProjectileHit(ProjectileHitEvent event)
	{
		if (_remove)
		{
			if (_arrows.remove(event.getEntity()))
			{
				event.getEntity().remove();
			}
		}
	}

	@EventHandler
	public void BarrageClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_arrows.removeIf(arrow -> arrow.isDead() || !arrow.isValid());
	}

	@EventHandler
	public void Quit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		clean(player);
	}

	public void clean(Player player)
	{
		_charge.remove(player.getUniqueId());
		_chargeLast.remove(player.getUniqueId());
		_firing.remove(player.getUniqueId());
	}
}
