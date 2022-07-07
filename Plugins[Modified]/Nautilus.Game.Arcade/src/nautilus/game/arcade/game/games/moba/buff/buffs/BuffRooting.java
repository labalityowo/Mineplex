package nautilus.game.arcade.game.games.moba.buff.buffs;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.events.EntityVelocityChangeEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.buff.Buff;
import nautilus.game.arcade.game.games.moba.kit.HeroSkillUseEvent;

public class BuffRooting extends Buff<Player>
{

	public BuffRooting(Moba host, Player entity, long duration)
	{
		super(host, entity, duration);
	}

	@Override
	public void onApply()
	{
		UtilAction.zeroVelocity(_entity);
		_entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (_duration / 50D), 3));
		UtilTextMiddle.display("", C.cRed + "Rooted", 10, 20, 10, (Player) _entity);
	}

	@Override
	public void onExpire()
	{

	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, _entity.getLocation().add(0, 1, 0), 0.5F, 0.2F, 0.5F, 0.1F, 3, ViewDist.LONG);
	}

	@EventHandler
	public void velocityApply(EntityVelocityChangeEvent event)
	{
		if (event.getEntity().equals(_entity))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void useMovementSkill(HeroSkillUseEvent event)
	{
		Player player = event.getPlayer();

		if (player.equals(_entity) && event.getSkill().isSneakActivate())
		{
			player.sendMessage(F.main("Game", "You cannot use movement abilities while " + F.name("Rooted") + "."));
			event.setCancelled(true);
		}
	}
}
