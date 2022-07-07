package nautilus.game.arcade.game.games.castleassault.kits;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.castleassault.CastleAssault;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public abstract class KitPlayer extends Kit
{

	public KitPlayer(ArcadeManager manager, GameKit gameKit, Perk... perks)
	{
		super(manager, gameKit, perks);
	}

	public abstract void awardKillStreak(Player player, int streak);
	
	protected CastleAssault getGame()
	{
		return (CastleAssault) Manager.GetGame();
	}
	
	protected void giveRegeneration(Player player)
	{
		player.getActivePotionEffects().forEach(p -> player.removePotionEffect(p.getType()));
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 8, 4));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 8, 3));
	}
}