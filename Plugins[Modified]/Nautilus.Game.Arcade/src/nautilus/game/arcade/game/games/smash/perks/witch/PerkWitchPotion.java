package nautilus.game.arcade.game.games.smash.perks.witch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkWitchPotion extends SmashPerk implements IThrown
{

	private int _cooldown;
	private int _range;
	private int _damageDirect;
	private int _damageDistance;
	private int _knockbackMagnitude;

	private List<Projectile> _proj = new ArrayList<>();

	public PerkWitchPotion()
	{
		super("Daze Potion", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Daze Potion" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_range = getPerkInt("Range");
		_damageDirect = getPerkInt("Damage Direct");
		_damageDistance = getPerkInt("Damage Distance");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
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

		if (!UtilItem.isAxe(player.getItemInHand()))
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

		// Start
		ThrownPotion potion = player.launchProjectile(ThrownPotion.class);
		UtilAction.velocity(potion, player.getLocation().getDirection(), 1, false, 0, 0.2, 10, false);

		_proj.add(potion);

		Manager.GetProjectile().AddThrow(potion, player, this, 10000, true, true, true, false, false, 0);

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<Projectile> potionIterator = _proj.iterator();

		while (potionIterator.hasNext())
		{
			Projectile proj = potionIterator.next();

			if (!proj.isValid())
			{
				potionIterator.remove();
				continue;
			}

			UtilParticle.PlayParticle(ParticleType.MOB_SPELL, proj.getLocation(), 0, 0, 0, 0, 1, ViewDist.LONGER, UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Player thrower = (Player) data.getThrower();

		List<Player> players = new ArrayList<>(Manager.GetGame().GetPlayers(true));
		players.removeAll(TeamSuperSmash.getTeam(Manager, thrower, true));

		List<Player> directHit = UtilEnt.getPlayersInsideEntity(data.getThrown(), players);

		for (Player player : directHit)
		{
			Manager.GetDamage().NewDamageEvent(player, thrower, null, DamageCause.CUSTOM, _damageDirect, true, true, false, thrower.getName(), GetName());
			Manager.GetCondition().Factory().Slow(GetName(), player, thrower, 2, 1, true, true, false, false);
		}

		players.removeAll(directHit);

		Vector a = data.getThrown().getLocation().subtract(_range, _range, _range).toVector();
		Vector b = data.getThrown().getLocation().add(_range, _range, _range).toVector();
		for (Player player : players)
		{
			if (!UtilEnt.isInsideBoundingBox(player, a, b)) continue;

			Manager.GetDamage().NewDamageEvent(player, thrower, null, DamageCause.CUSTOM, _damageDistance, true, true, false, thrower.getName(), GetName());
			Manager.GetCondition().Factory().Slow(GetName(), player, thrower, 2, 0, true, true, false, false);
		}
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		Collide(null, null, data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		Collide(null, null, data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}