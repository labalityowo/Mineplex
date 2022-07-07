package nautilus.game.arcade.game.games.moba.kit.bardolf;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.common.SkillSword;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeroBardolf extends HeroKit
{

	private static final int MIN_DIST_SQUARED = 2;
	private static final int MAX_DIST_SQUARED = 100;

	private final List<WolfData> _data;

	private static final Perk[] PERKS = {
			new SkillSword(0),
			new SkillSummonWolf(1),
			new SkillWolfPounce(2),
			new SkillFullMoon(3)
	};

	public HeroBardolf(ArcadeManager manager)
	{
		super(manager, "Bardolf", PERKS, MobaRole.ASSASSIN, SkinData.BARDOLF, 10);

		_data = new ArrayList<>(2);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_data.removeIf(data -> data.getOwner().equals(event.getPlayer()));
	}

	@EventHandler
	public void updateWolves(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (WolfData data : _data)
		{
			data.getWolves().removeIf(wolf -> wolf.isDead() || !wolf.isValid());

			data.getWolves().forEach(wolf ->
			{
				if (data.getOverrideTarget().containsKey(wolf))
				{
					UtilEnt.CreatureMoveFast(wolf, data.getOverrideTarget().get(wolf), data.isUltimate() ? 2F : 1.5F);
					return;
				}

				double ownerOffset = UtilMath.offsetSquared(data.getOwner(), wolf);

				if (wolf.getTarget() != null)
				{
					LivingEntity target = wolf.getTarget();

					if (UtilPlayer.isSpectator(target) || target.isDead() || !target.isValid())
					{
						wolf.setTarget(null);
						return;
					}

					UtilEnt.CreatureMoveFast(wolf, wolf.getTarget().getLocation(), data.isUltimate() ? 2F : 1.5F);

					if (UtilMath.offsetSquared(wolf.getTarget(), wolf) < 9 && Recharge.Instance.use(data.getOwner(), "Wolf" + wolf.getTarget().getUniqueId(), 500, false, false))
					{
						Manager.GetDamage().NewDamageEvent(wolf.getTarget(), data.getOwner(), null, DamageCause.CUSTOM, 1, true, true, false, data.getOwner().getName(), "Wolf");
					}
				}
				else if (ownerOffset > MAX_DIST_SQUARED)
				{
					wolf.teleport(data.getOwner());
				}
				else if (ownerOffset > MIN_DIST_SQUARED)
				{
					UtilEnt.CreatureMoveFast(wolf, data.getOwner().getLocation(), data.isUltimate() ? 2F : 1.5F);
				}
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void updateTarget(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);
		WolfData data = getWolfData(damager);

		if (damager == null || data == null)
		{
			return;
		}

		for (Wolf wolf : data.getWolves())
		{
			wolf.setTarget(event.GetDamageeEntity());
		}
	}

	@EventHandler
	public void preventTeamDamage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(true);
		WolfData data = getWolfData(damagee);

		if (data == null || !(damager instanceof Player))
		{
			return;
		}
		GameTeam team = Manager.GetGame().GetTeam((Player) damager);

		if (team != null && !Manager.GetGame().DamageTeamSelf && MobaUtil.isTeamEntity(damagee, team))
		{
			event.SetCancelled("Team Wolf");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityDeath(EntityDeathEvent event)
	{
		for (WolfData data : _data)
		{
			if (data.getWolves().contains(event.getEntity()))
			{
				event.setDroppedExp(0);
				event.getDrops().clear();
			}
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		WolfData data = getWolfData(event.getEntity());

		if (data == null)
		{
			return;
		}

		data.getWolves().forEach(Entity::remove);
	}

	public WolfData getWolfData(Player player)
	{
		if (player == null)
		{
			return null;
		}

		for (WolfData data : _data)
		{
			if (data.getOwner().equals(player))
			{
				return data;
			}
		}

		if (Manager.GetGame().GetKit(player).equals(this))
		{
			WolfData data = new WolfData(player);
			_data.add(data);
			return data;
		}

		return null;
	}

	public WolfData getWolfData(LivingEntity entity)
	{
		if (entity == null)
		{
			return null;
		}

		for (WolfData data : _data)
		{
			if (data.getOwner().equals(entity))
			{
				return data;
			}

			for (Wolf wolf : data.getWolves())
			{
				if (wolf.equals(entity))
				{
					return data;
				}
			}
		}

		return null;
	}

	class WolfData
	{

		private Player _owner;
		private List<Wolf> _wolves;
		private final Map<Wolf, Location> _overrideTarget;
		private boolean _ultimate;
		private float _lastSpeedIncrease;

		WolfData(Player owner)
		{
			_owner = owner;
			_wolves = new ArrayList<>(5);
			_overrideTarget = new HashMap<>(5);
		}

		public Player getOwner()
		{
			return _owner;
		}

		public List<Wolf> getWolves()
		{
			return _wolves;
		}

		public Map<Wolf, Location> getOverrideTarget()
		{
			return _overrideTarget;
		}

		public void setUltimate(boolean ultimate)
		{
			_ultimate = ultimate;
		}

		public boolean isUltimate()
		{
			return _ultimate;
		}

		public void setLastSpeedIncrease(float increase)
		{
			_lastSpeedIncrease = increase;
		}

		public float getLastSpeedIncrease()
		{
			return _lastSpeedIncrease;
		}
	}

}
