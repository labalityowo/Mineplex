package nautilus.game.arcade.game.modules;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class AbsorptionFix extends Module
{

	@EventHandler
	public void convertAbsorption(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : getGame().GetPlayers(true))
		{
			for (PotionEffect effect : player.getActivePotionEffects())
			{
				if (effect.getType().toString().equalsIgnoreCase(PotionEffectType.ABSORPTION.toString()))
				{
					player.removePotionEffect(PotionEffectType.ABSORPTION);
					player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles()));
					UtilPlayer.health(player, 4);
				}
			}
		}
	}
}