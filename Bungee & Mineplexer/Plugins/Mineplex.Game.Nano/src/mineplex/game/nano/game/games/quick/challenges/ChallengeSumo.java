package mineplex.game.nano.game.games.quick.challenges;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ChallengeSumo extends Challenge
{

	private final Map<Player, Integer> _knockback;

	public ChallengeSumo(Quick game)
	{
		super(game, ChallengeType.SUMO);

		_knockback = new HashMap<>();

		_timeout = TimeUnit.MINUTES.toMillis(1);
		_pvp = true;
		_winConditions.setLastThree(true);
	}

	@Override
	public void challengeSelect()
	{
		ItemStack itemStack = new ItemBuilder(Material.STICK)
				.setTitle(C.cGoldB + "Knockback Stick")
				.setGlow(true)
				.build();

		for (Player player : _players)
		{
			PlayerInventory inventory = player.getInventory();

			for (int i = 0; i < 9; i++)
			{
				inventory.setItem(i, itemStack);
			}
		}
	}

	@Override
	public void disable()
	{
	}

	@EventHandler(ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(false);

		if (damagee == null || damager == null)
		{
			return;
		}

		UtilPlayer.hunger(damager, 4);

		int knockback = _knockback.getOrDefault(damagee, 4);
		_knockback.put(damagee, knockback + 1);
		event.AddKnockback("Knockback Stick", knockback);
		event.AddMod(_game.getGameType().getName(), -event.GetDamage() + 0.1);
	}

	@EventHandler
	public void updateHunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC)
		{
			return;
		}

		_players.forEach(player -> UtilPlayer.hunger(player, -1));
	}
}
