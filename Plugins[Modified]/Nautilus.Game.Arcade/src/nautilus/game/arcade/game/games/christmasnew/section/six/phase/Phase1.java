package nautilus.game.arcade.game.games.christmasnew.section.six.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.six.GWENAnimation;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackFire;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackShootArrows;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackThrowMobs;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackThrowTNT;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.BossAttack;

public class Phase1 extends BossPhase
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(55);
	private static final ItemStack HELMET = new ItemStack(Material.JACK_O_LANTERN);
	private static final int SHIELD_STANDS = 16;
	private static final double INITIAL_THETA = 2 * Math.PI / SHIELD_STANDS;
	private static final double DELTA_THETA = Math.PI / 40;
	private static final double DELTA_THETA_Y = Math.PI / 35;
	private static final double RADIUS = 3.5;
	private static final int DAMAGE_RADIUS = 4;
	private static final long GWEN_DURATION = TimeUnit.SECONDS.toMillis(3);

	private final List<ArmorStand> _shield;
	private double _theta;
	private double _thetaY;

	private GWENAnimation _animation;
	private boolean _completing;
	private boolean _complete;

	public Phase1(ChristmasNew host, Section section)
	{
		super(host, section);

		_shield = new ArrayList<>();

		addAttacks(
				new AttackThrowMobs(this, 4, 7),
				new AttackThrowTNT(this, 4),
				new AttackShootArrows(this),
				new AttackFire(this, 5, 3)
		);
	}

	@Override
	public boolean isComplete()
	{
		return _complete;
	}

	@Override
	public void onAttack(BossAttack attack)
	{
		UtilEnt.CreatureMove(_boss, UtilAlg.getRandomLocation(getBossSpawn(), 25, 0, 15), 1.4F);
	}

	@Override
	public void onRegister()
	{
		Location location = _boss.getLocation();

		for (int i = 0; i < SHIELD_STANDS; i++)
		{
			ArmorStand stand = spawn(location, ArmorStand.class);

			stand.setVisible(false);
			stand.setGravity(false);
			stand.setHelmet(HELMET);
			stand.setRemoveWhenFarAway(false);

			UtilEnt.vegetate(stand);

			_shield.add(stand);
		}

		_host.getArcadeManager().runSyncLater(() -> _host.sendSantaMessage("He has a shield that’s protecting him from your attacks. Just try and survive while I think of a way to destroy it.", ChristmasNewAudio.SANTA_SHIELD), 80);
		_host.getArcadeManager().runSyncLater(() -> _host.sendBossMessage("That’s right! I’ve learned about all your tricks that you’ve used before. I won’t be falling for them again!", ChristmasNewAudio.PK_LEARNED), 240);
	}

	@Override
	public void onUnregister()
	{

	}

	@EventHandler
	public void updateShield(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Location base = _boss.getLocation();
		int index = 0;

		for (ArmorStand stand : _shield)
		{
			double theta = index++ * INITIAL_THETA + _theta;
			double x = RADIUS * Math.cos(theta);
			double y = Math.sin(theta + _thetaY);
			double z = RADIUS * Math.sin(theta);

			base.add(x, y, z);
			base.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory2d(_boss.getLocation(), base)));

			stand.teleport(base);
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, base.clone().add(0, 1.7, 0), 0.2F, 0.2F, 0.2F, 0, 1, ViewDist.LONG);

			base.subtract(x, y, z);
		}

		for (Player player : UtilPlayer.getNearby(_boss.getLocation(), DAMAGE_RADIUS))
		{
			if (!Recharge.Instance.use(player, "Shield Attack", 500, false, false))
			{
				continue;
			}

			Vector direction = UtilAlg.getTrajectory(_boss, player).multiply(1.7);
			direction.setY(0.6);

			UtilAction.velocity(player, direction);
			_host.getArcadeManager().GetDamage().NewDamageEvent(player, _boss, null, DamageCause.CUSTOM, 4, false, true, true, _boss.getName(), "Pumpkin Shield");
			_host.sendSantaMessage(player, "Watch out! " + player.getName() + " that shield is protecting him!", null);
		}

		_theta += DELTA_THETA;
		_thetaY += DELTA_THETA_Y;
	}

	@EventHandler
	public void damageBoss(CustomDamageEvent event)
	{
		LivingEntity damagee = event.GetDamageeEntity();

		if (_boss.equals(damagee))
		{
			event.SetCancelled("Damage Shield");
			return;
		}

		for (ArmorStand stand : _shield)
		{
			if (stand.equals(damagee))
			{
				event.SetCancelled("Shield Entity");
				return;
			}
		}
	}

	@EventHandler
	public void updateObjective(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_section.setObjective("Survive the Pumpkin King's attacks", (double) (System.currentTimeMillis() - _start) / DURATION);
	}

	@EventHandler
	public void updateEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !UtilTime.elapsed(_start, DURATION) || _complete)
		{
			return;
		}

		if (!_completing)
		{
			clearAttacks();
			stopBossMovement();
			_host.CreatureAllowOverride = true;
			_animation = new GWENAnimation(_host.getArcadeManager(), _boss.getLocation(), 4, (int) GWEN_DURATION);
			_host.CreatureAllowOverride = false;
			_completing = true;
			_host.sendSantaMessage("GWEN? Is that you?", ChristmasNewAudio.SANTA_GWEN);
			_host.getArcadeManager().runSyncLater(() -> _host.sendBossMessage("No! This is a false ban!", ChristmasNewAudio.PK_FALSE_BAN), 80);
		}

		if (_animation.update())
		{
			Location location = _boss.getLocation();

			UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location.add(0, 1, 0), null, 0.1F, 1, ViewDist.LONG);
			_boss.getWorld().playSound(location, Sound.EXPLODE, 3, 0.6F);

			_animation.clean();
			destroyShield();
			_complete = true;
		}
	}

	private void destroyShield()
	{
		_entities.clear();

		_shield.forEach(stand ->
		{
			stand.setGravity(true);
			UtilAction.velocity(stand, new Vector(Math.random() - 0.5, 1.8, Math.random() - 0.5));
		});

		_host.getArcadeManager().runSyncLater(() ->
		{
			_shield.forEach(Entity::remove);
			_shield.clear();
		}, 30);
	}
}
