package nautilus.game.arcade.game.games.castlesiegenew.perks;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.castlesiegenew.CastleSiegeNew;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PerkPaladinBoost extends Perk
{

	private static final int RADIUS = 6;

	private final long _cooldown;
	private final PotionEffect[] _effects;

	public PerkPaladinBoost(long cooldown, PotionEffect... effects)
	{
		super("Morale Royale");

		_cooldown = cooldown;
		_effects = effects;
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!hasPerk(player) || !UtilItem.isSword(itemStack) || !Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		CastleSiegeNew game = (CastleSiegeNew) Manager.GetGame();
		GameTeam team = game.GetTeam(player);

		player.sendMessage(F.main("Game", "You used " + F.skill(GetName()) + "."));

		for (Player nearby : UtilPlayer.getNearby(player.getLocation(), RADIUS))
		{
			if (!team.HasPlayer(nearby))
			{
				continue;
			}

			UtilParticle.PlayParticleToAll(ParticleType.HEART, nearby.getLocation().add(0, 1.2, 0), 0.5F, 0.5F, 0.5F, 0.01F, 8, ViewDist.LONG);
			nearby.playSound(nearby.getLocation(), Sound.ZOMBIE_REMEDY, 1, 0.6F);
			nearby.sendMessage(F.main("Game", "Paladin " + F.name(player.getName()) + " has given you buffs!"));

			boolean wolf = game.isWolf(nearby);

			for (PotionEffect effect : _effects)
			{
				if (effect.getType() == PotionEffectType.DAMAGE_RESISTANCE && wolf)
				{
					effect = new PotionEffect(PotionEffectType.SPEED, effect.getDuration(), 0, effect.isAmbient(), effect.hasParticles());
				}

				nearby.addPotionEffect(effect);
			}
		}
	}
}
