package mineplex.game.nano.game.games.quick.challenges;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ChallengeEnchantItem extends Challenge
{

	public ChallengeEnchantItem(Quick game)
	{
		super(game, ChallengeType.ENCHANT_ITEM);

		_pvp = true;
	}

	@Override
	public void challengeSelect()
	{
		ItemStack[] itemStacks =
				{
						new ItemStack(Material.STONE_SWORD),
						new ItemStack(Material.EXP_BOTTLE, 64),
						new ItemStack(Material.EYE_OF_ENDER),
						new ItemStack(Material.SPECKLED_MELON),
						new ItemStack(Material.COOKED_BEEF),
						new ItemStack(Material.WOOD, 32),
						new ItemStack(Material.MELON_BLOCK),
						new ItemStack(Material.COOKED_BEEF, 2)
				};

		UtilAlg.shuffle(itemStacks);

		for (Player player : _players)
		{
			player.getInventory().addItem(itemStacks);
		}

		_game.getGreenPoints().forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.ENCHANTMENT_TABLE));
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void enchant(EnchantItemEvent event)
	{
		completePlayer(event.getEnchanter(), false);
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer(), damager = event.GetDamagerPlayer(false);

		if (damagee == null || damager == null)
		{
			return;
		}

		ItemStack itemStack = damager.getItemInHand();

		if (itemStack != null && itemStack.getItemMeta().hasEnchants())
		{
			return;
		}

		event.SetCancelled("Non-Enchanted Weapon");
	}
}
