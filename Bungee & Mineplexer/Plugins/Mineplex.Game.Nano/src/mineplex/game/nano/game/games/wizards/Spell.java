package mineplex.game.nano.game.games.wizards;

import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.lifetimes.Lifetime;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;
import mineplex.core.recharge.Recharge;
import mineplex.game.nano.game.Game.GameState;

public abstract class Spell extends ListenerComponent implements Lifetimed
{

	public enum SpellType
	{
		Attack,
		Defense,
		Utility
	}

	protected final Wizards _game;
	private final String _name;
	private final ItemStack _itemStack;
	private final long _cooldown;

	public Spell(Wizards game, String name, SpellType spellType, ItemStack itemStack, long cooldown)
	{
		_game = game;
		_name = name;
		_itemStack = new ItemBuilder(itemStack)
				.setTitle(C.cGreen + name + C.cGray + " - [" + C.cGold + spellType.toString() + C.cGray + "]")
				.addLore("Right-Click to use this spell!")
				.build();
		_cooldown = cooldown;

		game.getLifetime().register(this, Collections.singletonList(GameState.Live));
	}

	protected abstract void onSpellUse(Player player);

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		ItemStack itemStack = player.getItemInHand();

		if (!_itemStack.equals(itemStack))
		{
			return;
		}

		event.setCancelled(true);

		if (!Recharge.Instance.use(player, _name, _cooldown, true, true))
		{
			return;
		}

		onSpellUse(player);
	}

	public String getName()
	{
		return _name;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	@Override
	public Lifetime getLifetime()
	{
		return _game.getLifetime();
	}
}
