package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.Managers;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class MobaHitArrowAmmoEffect extends MobaItemEffect
{

	@Override
	protected void onDamage(CustomDamageEvent event)
	{
		if (!(event.GetProjectile() instanceof Arrow))
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);

		Moba host = (Moba) Managers.get(ArcadeManager.class).GetGame();
		HeroKit kit = host.getMobaData(damager).getKit();

		kit.giveAmmo(damager, 1);
	}

	@Override
	public String getDescription()
	{
		return "Hitting a player with an arrow gives you a new arrow.";
	}
}
