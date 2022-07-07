package mineplex.game.nano.game.games.quick.challenges;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilMath;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeCraftItem extends Challenge
{

	private final ItemStack _result;

	public ChallengeCraftItem(Quick game)
	{
		super(game, ChallengeType.CRAFT_ITEM);

		_result = new ItemStack(UtilMath.randomElement(new Material[]
				{
						Material.IRON_AXE,
						Material.IRON_SPADE,
						Material.IRON_SWORD,
						Material.IRON_PICKAXE,
						Material.IRON_BLOCK,
						Material.IRON_DOOR,
						Material.DIAMOND_BLOCK,
						Material.DIAMOND_AXE,
						Material.DIAMOND_PICKAXE,
						Material.IRON_BOOTS,
						Material.IRON_LEGGINGS,
						Material.IRON_CHESTPLATE,
						Material.BUCKET
				}));
		_winConditions.setTimeoutAfterFirst(true);
	}

	@Override
	public void challengeSelect()
	{
		for (Recipe recipe : Bukkit.getRecipesFor(_result))
		{
			if (!(recipe instanceof ShapedRecipe))
			{
				return;
			}

			ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;

			shapedRecipe.getIngredientMap().forEach((character, itemStack) ->
			{
				if (itemStack == null)
				{
					return;
				}

				for (Player player : _players)
				{
					player.getInventory().addItem(itemStack);
				}
			});
		}

		for (Player player : _players)
		{
			PlayerInventory inventory = player.getInventory();

			for (int i = 9; i < inventory.getSize(); i++)
			{
				inventory.setItem(i, _result);
			}
		}

		_game.getGreenPoints().forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.WORKBENCH));
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void craftItem(CraftItemEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		ItemStack result = event.getInventory().getResult();

		if (result != null && result.getType().equals(_result.getType()))
		{
			completePlayer(player, false);
		}
		else
		{
			failPlayer(player, false);
		}
	}
}
