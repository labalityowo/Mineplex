package nautilus.game.arcade.game.games.smash.perks.slime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkSlimeRocket extends SmashPerk implements IThrown
{

	private int _cooldown;
	private float _energyTick;
	private int _knockbackMagnitude;
	private int _maxEnergyTime;
	private int _maxHoldTime;

	private Map<UUID, Long> _charge = new HashMap<>();
	private Map<Slime, UUID> _owner = new HashMap<>();
	private Map<Slime, Long> _lastAttack = new HashMap<>();

	public PerkSlimeRocket()
	{
		super("Slime Rocket", new String[]{C.cYellow + "Hold/Release Block" + C.cGray + " to use " + C.cGreen + "Slime Rocket"});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_energyTick = getPerkFloat("Energy Per Tick");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
		_maxEnergyTime = getPerkTime("Max Energy Time");
		_maxHoldTime = getPerkTime("Max Hold Time");
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

			if (isSuperActive(player))
			{
				continue;
			}

			int size = 1;
			if (player.getExp() > 0.8)
			{
				size = 3;
			}
			else if (player.getExp() > 0.55)
			{
				size = 2;
			}

			DisguiseSlime slime = (DisguiseSlime) Manager.GetDisguise().getActiveDisguise(player);

			if (slime != null && slime.GetSize() != size)
			{
				slime.SetSize(size);
				Manager.GetDisguise().updateDisguise(slime);
			}

			if (player.isBlocking() && !Recharge.Instance.usable(player, GetName()))
			{
				continue;
			}

			player.setExp((float) Math.min(0.999, player.getExp() + _energyTick));
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

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		UtilPlayer.message(player, F.main("Skill", "You are charging " + F.skill(GetName()) + "."));

		_charge.put(player.getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void ChargeRelease(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<UUID> chargeIterator = _charge.keySet().iterator();

		while (chargeIterator.hasNext())
		{
			UUID key = chargeIterator.next();
			Player player = UtilPlayer.searchExact(key);

			if (player == null)
			{
				chargeIterator.remove();
				continue;
			}

			long time = _charge.get(key);

			// Charge
			if (player.isBlocking())
			{
				// Energy Depleted 
				if (player.getExp() < 0.1)
				{
					FireRocket(player);
					chargeIterator.remove();
				}
				else
				{
					double elapsed = Math.min(3, (double) (System.currentTimeMillis() - time) / 1000d);

					// Use Energy
					if (!UtilTime.elapsed(time, _maxEnergyTime))
					{
						player.setExp(Math.max(0, player.getExp() - 0.01f));
					}

					// AutoFire
					if (UtilTime.elapsed(time, _maxHoldTime))
					{
						FireRocket(player);
						chargeIterator.remove();
					}

					float offset = (float) (elapsed / 6d);

					// Effect
					player.getWorld().playSound(player.getLocation(), Sound.SLIME_WALK, 0.5f, (float) (0.5 + 1.5 * (elapsed / 3d)));
					UtilParticle.PlayParticle(ParticleType.SLIME, player.getLocation().add(0, 1, 0), offset, offset, offset, 0, (int) (elapsed * 5), ViewDist.LONGER, UtilServer.getPlayers());
				}
			}
			// Release
			else
			{
				FireRocket(player);
				chargeIterator.remove();
			}
		}
	}

	public void FireRocket(Player player)
	{
		double charge = Math.min(3, (double) (System.currentTimeMillis() - _charge.get(player.getUniqueId())) / 1000d);

		// Spawn Slime
		Manager.GetGame().CreatureAllowOverride = true;
		Slime slime = player.getWorld().spawn(player.getEyeLocation(), Slime.class);
		slime.setSize(1);
		Manager.GetGame().CreatureAllowOverride = false;

		// Size
		slime.setSize(Math.max(1, (int) charge));

		slime.setMaxHealth(5 + charge * 7);
		slime.setHealth(slime.getMaxHealth());

		_owner.put(slime, player.getUniqueId());

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You released " + F.skill(GetName()) + "."));

		slime.leaveVehicle();
		player.eject();

		UtilAction.velocity(slime, player.getLocation().getDirection(), 1 + charge / 2d, false, 0, 0.2, 10, true);

		Manager.GetProjectile().AddThrow(slime, player, this, -1, true, true, true, true, null, 0, 0, null, 0, UpdateType.FASTEST, 1f);
	}

	@EventHandler
	public void SlimeTarget(EntityTargetEvent event)
	{
		UUID uuid = _owner.get(event.getEntity());

		if (uuid == null)
		{
			return;
		}

		Player owner = UtilPlayer.searchExact(uuid);

		if (owner == null || event.getTarget() == null)
		{
			return;
		}

		if (event.getTarget() instanceof Player && isTeamDamage((Player) event.getTarget(), owner) || event.getTarget().equals(owner))
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target == null || !(data.getThrown() instanceof Slime))
		{
			return;
		}

		Slime slime = (Slime) data.getThrown();

		if (target instanceof Player && data.getThrower() instanceof Player)
		{
			Player targetPlayer = (Player) target;
			Player throwerPlayer = (Player) data.getThrower();

			if (isTeamDamage(targetPlayer, throwerPlayer) || !Recharge.Instance.use(targetPlayer, GetName() + slime.getUniqueId(), 2000, false, false))
			{
				return;
			}
		}

		// Damage Event
		_lastAttack.put(slime, System.currentTimeMillis());
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.PROJECTILE, 3 + slime.getSize() * 3, true, true, false, UtilEnt.getName(data.getThrower()), GetName());
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude * (event.GetDamageInitial() / 3));
	}

	@Override
	public void Idle(ProjectileUser data)
	{

	}

	@Override
	public void Expire(ProjectileUser data)
	{

	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@EventHandler
	public void SlimeDamage(CustomDamageEvent event)
	{
		if (!(event.GetDamagerEntity(false) instanceof Slime))
		{
			return;
		}

		Slime slime = (Slime) event.GetDamagerEntity(false);

		// Attack Rate
		if (_lastAttack.containsKey(slime) && !UtilTime.elapsed(_lastAttack.get(slime), 500))
		{
			event.SetCancelled("Slime Attack Rate");
			return;
		}

		_lastAttack.put(slime, System.currentTimeMillis());

		// Get Owner
		UUID key = _owner.get(slime);
		Player owner = UtilPlayer.searchExact(key);

		if (owner == null)
		{
			return;
		}

		if (isTeamDamage(owner, event.GetDamageePlayer()))
		{
			event.SetCancelled("Team Damage");
			return;
		}

		if (owner.equals(event.GetDamageeEntity()))
		{
			event.SetCancelled("Owner Damage");
		}
		else
		{
			event.AddMod("Slime Damage", "Negate", -event.GetDamageInitial(), false);
			event.AddMod("Slime Damage", "Attack", 2 * slime.getSize(), true);
			event.AddKnockback("Slime Knockback", 2);
		}
	}

	@EventHandler
	public void SlimeClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<Slime> slimeIterator = _owner.keySet().iterator();

		while (slimeIterator.hasNext())
		{
			Slime slime = slimeIterator.next();

			// Shrink
			if (slime.getVehicle() == null)
			{
				if (slime.getTicksLived() > 120)
				{
					slime.setTicksLived(1);

					Manager.GetBlood().Effects(null, slime.getLocation(), 6 + 6 * slime.getSize(), 0.2 + 0.1 * slime.getSize(), null, 1f, 1f, Material.SLIME_BALL, (byte) 0, 15, false);

					if (slime.getSize() <= 1)
					{
						slime.remove();
					}
					else
					{
						slime.setSize(slime.getSize() - 1);
					}
				}
			}

			if (!slime.isValid())
			{
				slimeIterator.remove();
			}
		}

		slimeIterator = _lastAttack.keySet().iterator();

		while (slimeIterator.hasNext())
		{
			Slime slime = slimeIterator.next();

			if (!slime.isValid())
			{
				slimeIterator.remove();
			}
		}
	}
}