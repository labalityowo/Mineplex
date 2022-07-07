package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.game.kit.GameKit;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBladeVortex;
import nautilus.game.arcade.kit.perks.PerkCleave;

public class KitBarbarian extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkCleave(0.75, false),
					new PerkBladeVortex()
			};

	public KitBarbarian(ArcadeManager manager)
	{
		super(manager, GameKit.SG_BARBARIAN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		Recharge.Instance.useForce(player, GetName(), 45000);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();

		if (killer == null || !HasKit(killer))
		{
			return;
		}

		killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
	}
}
