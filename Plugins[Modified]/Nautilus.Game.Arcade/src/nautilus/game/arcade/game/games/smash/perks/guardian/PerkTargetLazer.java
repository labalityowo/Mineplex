package nautilus.game.arcade.game.games.smash.perks.guardian;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkTargetLazer extends SmashPerk
{

	private static final int ACTIVE_ITEM_SLOT = 2;

	private int _cooldown;
	private int _maxRange;
	private int _maxTime;
	private int _damageIncrease;
	private int _knockbackIncrease;

	private Set<TargetLazerData> _data = new HashSet<>();

	public PerkTargetLazer()
	{
		super("Target Laser", new String[] {C.cYellow + "Right-Click" + C.cGray + " with Pickaxe to use " + C.cGreen + "Target Laser"});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_maxRange = getPerkInt("Max Range");
		_maxTime = getPerkTime("Max Time");
		_damageIncrease = getPerkInt("Damage Increase");
		_knockbackIncrease = getPerkInt("Knockback Increase");
	}

	@Override
	public void unregisteredEvents()
	{
		_data.clear();
	}

	@EventHandler
	public void activate(PlayerInteractEvent event)
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

		if (!UtilItem.isPickaxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!UtilEnt.isGrounded(player))
		{
			player.sendMessage(F.main("Game", "You must be on the ground to use " + F.skill(GetName())) + ".");
			return;
		}

		if (!Recharge.Instance.usable(player, GetName()))
		{
			return;
		}

		Player best = null;
		double bestD = Double.MAX_VALUE;

		for (Player other : UtilServer.getPlayers())
		{
			if (player.equals(other) || UtilPlayer.isSpectator(other) || isTeamDamage(player, other))
			{
				continue;
			}

			double d = UtilMath.offset(player, other);

			if (d > _maxRange)
			{
				continue;
			}

			if (best == null || d < bestD)
			{
				best = other;
				bestD = d;
			}
		}

		boolean contained = false;

		for (TargetLazerData data : _data)
		{
			if (data.getAttacker().equals(player))
			{
				if (data.getTimeElapsed() < _maxTime)
				{
					return;
				}

				contained = true;
				data.setTarget(best);
			}
		}

		if (best == null)
		{
			player.sendMessage(F.main("Game", "There are no targets within range."));
			return;
		}

		player.sendMessage(F.main("Game", "You targeted " + F.name(best.getName())) + " with " + F.skill(GetName()) + ".");
		best.sendMessage(F.main("Game", F.name(player.getName()) + " targeted you with their " + F.skill(GetName()) + "."));

		if (!contained)
		{
			TargetLazerData data = new TargetLazerData(player);

			_data.add(data);
			data.setTarget(best);
		}

		setLazerTarget(player, best);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		Iterator<TargetLazerData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			TargetLazerData data = iterator.next();

			if (data.getTarget() == null)
			{
				continue;
			}

			UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT, data.getTarget().getLocation().add(0, 0.5, 0.5), 1F, 0.5F, 1F, 0.1F, 10, ViewDist.LONG, data.getAttacker());

			if (UtilMath.offset(data.getTarget(), data.getAttacker()) > _maxRange || data.getTimeElapsed() > _maxTime)
			{
				long time = data.getTimeElapsed() / 1000;
				double damage = 0.5 * time;
				Player attacker = data.getAttacker();
	
				setLazerTarget(attacker, null);

				attacker.sendMessage(F.main("Game", "Your laser broke, dealing damage to " + F.name(data.getTarget().getName())) + ".");
				Manager.GetDamage().NewDamageEvent(data.getTarget(), attacker, null, DamageCause.CUSTOM, damage, false, true, false, data.getAttacker().getName(), GetName());

				Recharge.Instance.use(attacker, GetName(), _cooldown, true, true);
				Recharge.Instance.Get(attacker).get(GetName()).Item = attacker.getInventory().getItem(ACTIVE_ITEM_SLOT);

				iterator.remove();
			}
		}
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (event.GetDamagerPlayer(true) == null || event.GetDamageePlayer() == null)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);
		Player damagee = event.GetDamageePlayer();

		for (TargetLazerData data : _data)
		{
			if (data.getTarget() == null)
			{
				continue;
			}

			if (data.getAttacker().equals(damager) && data.getTarget().equals(damagee))
			{
				event.AddMod(GetName(), _damageIncrease);
				event.AddKnockback(GetName(), _knockbackIncrease);
				damagee.playEffect(damagee.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
			}
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		_data.removeIf(data ->
		{
			if (data.getTarget().equals(player) || data.getAttacker().equals(player))
			{
				setLazerTarget(data.getAttacker(), null);
				return true;
			}

			return false;
		});
	}

	private void setLazerTarget(Player disguised, Player target)
	{
		DisguiseManager disguiseManager = Manager.GetDisguise();
		DisguiseGuardian disguise = (DisguiseGuardian) disguiseManager.getActiveDisguise(disguised);
		int entityId = 0;

		if (target != null)
		{
			entityId = disguiseManager.getActiveDisguise(target).getEntityId();
		}

		disguise.setTarget(entityId);
		disguiseManager.updateDisguise(disguise);
	}
}
