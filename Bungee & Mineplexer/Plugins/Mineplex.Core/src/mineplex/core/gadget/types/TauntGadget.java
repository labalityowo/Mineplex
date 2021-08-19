package mineplex.core.gadget.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.game.GameDisplay;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/**
 * Handles Taunts
 */
public abstract class TauntGadget extends Gadget
{

	/** Sets if this specific taunt can be used while in PvP */
	private boolean _canPlayWithPvp = false;
	/** Sets the cooldown for pvp */
	private long _pvpCooldown = 0;
	/** Sets if this taunt needs to run on updates */
	private boolean _shouldPlay = false;
	/** Sets when the taunt will run, if set above */
	private UpdateType _updateType = UpdateType.TICK;
	/** List of games where this item is disabled */
	private final List<GameDisplay> _disabledGames = new ArrayList<>();
	/** The ticks that passed since the player started the effect */
	private final Map<Player, Integer> _ticksPerPlayer = new HashMap<>();

	/**
	 * @param manager The normal GadgetManager
	 * @param name The name of the item
	 * @param desc The lore/description of the item
	 * @param cost The cost of the item
	 * @param mat The display material of the item
	 * @param data The display data of the item
	 * @param alternativeSalesPackageNames Possible alternative names for this package
	 */
	public TauntGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data,
					   String... alternativeSalesPackageNames)
	{
		super(manager, GadgetType.TAUNT, name, desc, cost, mat, data, 1, alternativeSalesPackageNames);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);
		finish(player);
	}

	public void start(Player player)
	{
		if (onStart(player))
		{
			_ticksPerPlayer.put(player, 0);
		}
	}

	public abstract boolean onStart(Player player);

	public void play(Player player)
	{
		int ticks = getPlayerTicks(player) + 1;
		_ticksPerPlayer.put(player, ticks);
		onPlay(player);
	}

	public abstract void onPlay(Player player);

	public void finish(Player player)
	{
		onFinish(player);
		_ticksPerPlayer.remove(player);
	}

	public abstract void onFinish(Player player);

	protected void setCanPlayWithPvp(boolean canPlayWithPvp)
	{
		_canPlayWithPvp = canPlayWithPvp;
	}

	protected void setPvpCooldown(long pvpCooldown)
	{
		_pvpCooldown = pvpCooldown;
	}

	protected void setShouldPlay(boolean shouldPlay)
	{
		_shouldPlay = shouldPlay;
	}

	protected void setEventType(UpdateType updateType)
	{
		_updateType = updateType;
	}

	protected void addDisabledGames(GameDisplay... disabledGames)
	{
		_disabledGames.addAll(Arrays.asList(disabledGames));
	}

	public boolean canPlayWithPvp()
	{
		return _canPlayWithPvp;
	}

	public boolean isGameDisabled(GameDisplay gameDisplay)
	{
		return _disabledGames.contains(gameDisplay);
	}

	public long getPvpCooldown()
	{
		return _pvpCooldown;
	}

	protected int getPlayerTicks(Player player)
	{
		return _ticksPerPlayer.getOrDefault(player, -1);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!_shouldPlay || event.getType() != _updateType)
		{
			return;
		}

		for (Player player : getActive())
		{
			if (_ticksPerPlayer.containsKey(player))
			{
				play(player);
			}
		}
	}
}