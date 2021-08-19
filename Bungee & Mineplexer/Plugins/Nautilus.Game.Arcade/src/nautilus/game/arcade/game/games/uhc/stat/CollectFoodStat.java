package nautilus.game.arcade.game.games.uhc.stat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;

import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.game.games.uhc.UHC;
import nautilus.game.arcade.stats.StatTracker;

public class CollectFoodStat extends StatTracker<UHC>
{

	private static final Set<Material> FOOD_TO_EAT;

	private Map<UUID, Set<Material>> _eaten = new HashMap<>();

	static
	{
		FOOD_TO_EAT = Sets.newHashSet(Material.APPLE, Material.MUSHROOM_SOUP, Material.BREAD, Material.GRILLED_PORK, Material.GOLDEN_APPLE, Material.COOKED_FISH, Material.COOKIE, Material.MELON,
				Material.COOKED_CHICKEN, Material.CARROT_ITEM, Material.BAKED_POTATO, Material.PUMPKIN_PIE, Material.COOKED_RABBIT, Material.COOKED_MUTTON);
	}

	public CollectFoodStat(UHC game)
	{
		super(game);
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event)
	{
		Player player = event.getPlayer();
		Material material = event.getItem().getType();

		if (FOOD_TO_EAT.contains(material))
		{
			Set<Material> eaten = _eaten.get(player.getUniqueId());

			if (eaten == null)
			{
				eaten = new HashSet<>();
				_eaten.put(player.getUniqueId(), eaten);
			}

			if (!eaten.contains(material))
			{
				eaten.add(material);
			}

			if (eaten.size() == FOOD_TO_EAT.size())
			{
				getGame().addUHCAchievement(player, "Food");
			}
		}
	}
	
	@EventHandler
	public void debugCommands(PlayerCommandPreprocessEvent event)
	{
		if (!UtilServer.isTestServer() || !event.getMessage().startsWith("/testfoodstat"))
		{
			return;
		}
		
		for (Material material : FOOD_TO_EAT)
		{
			event.getPlayer().getInventory().addItem(new ItemStack(material));
		}
	}

}
