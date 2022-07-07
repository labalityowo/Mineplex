package nautilus.game.arcade.game.games.smash.perks.zombie;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkOvercharge extends SmashPerk
{
	private Map<UUID, Integer> _charge = new HashMap<>();
	private Map<UUID, Long> _chargeLast = new HashMap<>();

	private Map<Arrow, Integer> _arrows = new HashMap<>();

	private int _max;
	private int _tick;
	private boolean _useExp;

	public PerkOvercharge()
	{
		super("Corrupted Arrow", new String[] { C.cYellow + "Charge" + C.cGray + " your Bow to use " + C.cGreen + "Corrupted Arrow" });
	}

	@Override
	public void setupValues()
	{
		_max = getPerkInt("Max");
		_tick = getPerkInt("Tick");
		_useExp = getPerkBoolean("Use Exp");
	}

	@EventHandler
	public void drawBow(PlayerInteractEvent event)
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

		if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.BOW)
		{
			return;
		}

		if (!player.getInventory().contains(Material.ARROW))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		// Start Charge
		_charge.put(player.getUniqueId(), 0);
		_chargeLast.put(player.getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void charge(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player cur : UtilServer.getPlayers())
		{
			UUID uuid = cur.getUniqueId();

			// Not Charging
			if (!_charge.containsKey(uuid))
			{
				continue;
			}

			// Max Charge
			if (_charge.get(uuid) >= _max)
			{
				continue;
			}

			// Charge Interval
			if (_charge.get(uuid) == 0)
			{
				if (!UtilTime.elapsed(_chargeLast.get(uuid), 1000))
				{
					continue;
				}
			}
			else
			{
				if (!UtilTime.elapsed(_chargeLast.get(uuid), _tick))
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

				_charge.remove(uuid);
				_chargeLast.remove(uuid);
				continue;
			}

			// Increase Charge
			_charge.put(uuid, _charge.get(uuid) + 1);

			if (_useExp)
			{
				cur.setExp(Math.min(0.99f, _charge.get(uuid) / _max));
			}

			_chargeLast.put(uuid, System.currentTimeMillis());

			// Effect
			cur.playSound(cur.getLocation(), Sound.CLICK, 1f, 1f + (0.1f * _charge.get(uuid)));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void fireBow(EntityShootBowEvent event)
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

		int charge = _charge.remove(player.getUniqueId());

		if (charge <= 0)
		{
			return;
		}

		// Start Barrage
		_arrows.put((Arrow) event.getProjectile(), charge);

		player.setExp(0f);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damageBonus(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
		{
			return;
		}

		if (!_arrows.containsKey(event.GetProjectile()))
		{
			return;
		}

		int charge = _arrows.remove(event.GetProjectile());

		event.AddMod(GetName(), GetName(), charge * 0.9, true);
	}

	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (Iterator<Arrow> arrowIterator = _arrows.keySet().iterator(); arrowIterator.hasNext();)
		{
			Arrow arrow = arrowIterator.next();

			if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround() || arrow.getTicksLived() > 120)
			{
				arrowIterator.remove();
			}
			else
			{
				UtilParticle.PlayParticle(ParticleType.RED_DUST, arrow.getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
			}
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_charge.remove(player.getUniqueId());
		_chargeLast.remove(player.getUniqueId());
	}
}
