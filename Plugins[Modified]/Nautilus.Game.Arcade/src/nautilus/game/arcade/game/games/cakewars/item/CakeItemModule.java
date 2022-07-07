package nautilus.game.arcade.game.games.cakewars.item;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeModule;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeItemModule extends CakeModule
{

	private final List<CakeSpecialItem> _items;

	public CakeItemModule(CakeWars game)
	{
		super(game);

		_items = game.generateSpecialItems();
	}

	@Override
	public void cleanup()
	{
		_items.forEach(CakeSpecialItem::cleanup);
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_items.forEach(CakeSpecialItem::setup);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
			return;
		}

		if (itemStack == null)
		{
			return;
		}

		GameTeam team = _game.GetTeam(player);

		if (team == null)
		{
			return;
		}

		CakeTeam cakeTeam = _game.getCakeTeamModule().getCakeTeam(team);

		if (cakeTeam == null)
		{
			return;
		}

		for (CakeSpecialItem item : _items)
		{
			if (item.getItemStack().getType() != itemStack.getType())
			{
				continue;
			}

			event.setCancelled(true);
			boolean inform = item.getCooldown() >= 1000;

			if (!Recharge.Instance.usable(player, item.getName(), inform))
			{
				return;
			}

			if (item.onClick(event, cakeTeam))
			{
				player.setItemInHand(UtilInv.decrement(itemStack));
				Recharge.Instance.useForce(player, item.getName(), item.getCooldown(), inform);
			}

			return;
		}
	}

}
