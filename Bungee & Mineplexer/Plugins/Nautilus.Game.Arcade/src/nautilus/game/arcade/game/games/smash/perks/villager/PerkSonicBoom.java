package nautilus.game.arcade.game.games.smash.perks.villager;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.particleeffects.SpiralEffect;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkSonicBoom extends SmashPerk
{

	private int _cooldown;
	private int _distance;
	private double _damage;
	private float _hitBox, _velocityFactor;

	public PerkSonicBoom()
	{
		super("Sonic Hurr", new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " Axe to use " + C.cGreen + "Sonic Hurr",
				});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_distance = getPerkInt("Distance");
		_damage = getPerkDouble("Damage");
		_hitBox = getPerkFloat("Hitbox");
		_velocityFactor = getPerkFloat("Velocity Factor");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R) || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!UtilItem.isAxe(itemStack) || !hasPerk(player) || !Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		Location location = player.getEyeLocation();
		location.add(location.getDirection());

		player.sendMessage(F.main("Game", "You used " + F.skill(GetName()) + "."));
		player.getWorld().playSound(location, Sound.VILLAGER_IDLE, 1.5F, 1.2F);

		new SpiralEffect(1, 2, _distance * 10, location)
		{
			@Override
			public void playParticle(Location location)
			{
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, null, 0, 1, ViewDist.NORMAL);

				if (Math.random() < 0.05)
				{
					UtilParticle.PlayParticleToAll(ParticleType.ANGRY_VILLAGER, location, 0.3F, 0.3F, 0.3F, 0, 1, ViewDist.NORMAL);
				}
			}
		}.start();

		Location center = location.clone().add(location.getDirection().multiply(_distance / 2D)).subtract(0, 0.5, 0);

		for (Player nearby : UtilPlayer.getNearby(center, _hitBox))
		{
			if (player.equals(nearby) || isTeamDamage(player, nearby))
			{
				return;
			}

			double scale = 1 - UtilMath.offset(player, nearby) / _distance / 2;

			Manager.GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, scale * _damage, false, true, false, player.getName(), GetName());

			UtilAction.velocity(nearby, UtilAlg.getTrajectory(player, nearby), scale * _velocityFactor, false, 0, 0.5, 0.8, true);
		}
	}
}
