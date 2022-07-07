package mineplex.game.nano.game.games.microbattle.components;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;

public class TeamArmourComponent extends GameComponent<Game>
{

	public TeamArmourComponent(Game game)
	{
		super(game, GameState.Prepare, GameState.Live);
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();
		GameTeam team = event.getTeam();
		Color colour = team.getColour();
		String name = team.getChatColour() + "Team Armor";

		player.getInventory().setArmorContents(new ItemStack[]
				{
						new ItemBuilder(Material.LEATHER_BOOTS)
								.setTitle(name)
								.setColor(colour)
								.setUnbreakable(true)
								.build(),
						new ItemBuilder(Material.LEATHER_LEGGINGS)
								.setTitle(name)
								.setColor(colour)
								.setUnbreakable(true)
								.build(),
						new ItemBuilder(Material.LEATHER_CHESTPLATE)
								.setTitle(name)
								.setColor(colour)
								.setUnbreakable(true)
								.build(),
						new ItemBuilder(Material.LEATHER_HELMET)
								.setTitle(name)
								.setColor(colour)
								.setUnbreakable(true)
								.build()
				});
		player.getInventory().setItem(8, new ItemBuilder(Material.LEATHER_CHESTPLATE)
				.setTitle(team.getChatColour() + C.Bold + team.getName())
				.setColor(colour)
				.build());
	}

	@EventHandler(ignoreCancelled = true)
	public void inventoryClick(InventoryClickEvent event)
	{
		if (UtilItem.isLeatherProduct(event.getCurrentItem()))
		{
			event.setCancelled(true);
		}
	}
}
