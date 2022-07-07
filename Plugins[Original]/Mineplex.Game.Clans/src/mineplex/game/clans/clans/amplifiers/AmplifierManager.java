package mineplex.game.clans.clans.amplifiers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;

/**
 * Manager for using amplifiers in clans
 */
public class AmplifierManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		AMPLIFIER_COMMAND,
	}

	public static final double AMPLIFIER_RUNE_DROP_MULTIPLIER = 2;
	private static final String AMPLIFIER_NAME = "Rune Amplifier";
	private Amplifier _active;
	
	public AmplifierManager(JavaPlugin plugin)
	{
		super("Rune Amplifiers", plugin);
		
		addCommand(new AmplifierCommand(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.AMPLIFIER_COMMAND, true, true);
	}
	
	/**
	 * Checks whether there is an amplifier active on this server
	 * @return Whether there is an amplifier active on this server
	 */
	public boolean hasActiveAmplifier()
	{
		return _active != null;
	}
	
	/**
	 * Checks how many of a certain amplifier type a player owns
	 * @param player The player to check
	 * @param type The type of amplifier to check for
	 * @return The amount of amplifiers of that type owned
	 */
	public int getAmountOwned(Player player, AmplifierType type)
	{
		return ClansManager.getInstance().getInventoryManager().Get(player).getItemCount(type.getFullItemName());
	}
	
	/**
	 * Makes a player use an amplifier
	 * @param user The player to use the amplifier
	 * @param type The type of amplifier to use
	 */
	public void useAmplifier(Player user, AmplifierType type)
	{
		if (getAmountOwned(user, type) < 1)
		{
			return;
		}
		if (hasActiveAmplifier())
		{
			return;
		}
		ClansManager.getInstance().getInventoryManager().addItemToInventory(user, type.getFullItemName(), -1);
		UtilTextMiddle.display(C.cClansNether + AMPLIFIER_NAME, "Has been activated by " + F.elem(user.getName()));
		Bukkit.broadcastMessage(F.main(getName(), "A " + F.clansNether(AMPLIFIER_NAME) + " has been activated on this server by " + F.elem(user.getName()) + " for " + F.elem(UtilTime.MakeStr(type.getDuration())) + "!"));
		_active = new Amplifier(user, type);
		runSyncLater(() ->
		{
			ClansManager.getInstance().getNetherManager().spawnPortal(type.getDuration());
		}, 60L);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		if (_active != null && _active.isEnded())
		{
			Bukkit.broadcastMessage(F.main(getName(), "The " + F.clansNether(AMPLIFIER_NAME) + " owned by " + F.elem(_active.getOwner().getName()) + " has run out! You can purchase another at http://www.mineplex.com/shop!"));
			_active = null;
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (_active != null)
		{
			runSyncLater(() ->
			{
				if (_active != null)
				{
					UtilPlayer.message(event.getPlayer(), F.main(getName(), "A " + F.clansNether(AMPLIFIER_NAME) + " owned by " + F.elem(_active.getOwner().getName()) + " is active on this server with " + F.elem(UtilTime.MakeStr(_active.getRemainingTime())) + " remaining!"));
				}
			}, 40L);
		}
	}
	
	/**
	 * Enum containing different types of amplifiers recognized by code
	 */
	public static enum AmplifierType
	{
		TWENTY("20", "Twenty Minute Amplifier", UtilTime.convert(20, TimeUnit.MINUTES, TimeUnit.MILLISECONDS)),
		SIXTY("60", "One Hour Amplifier", UtilTime.convert(60, TimeUnit.MINUTES, TimeUnit.MILLISECONDS));
		
		private String _extension, _display;
		private long _duration;
		
		private AmplifierType(String extension, String displayName, long duration)
		{
			_extension = extension;
			_display = displayName;
			_duration = duration;
		}
		
		/**
		 * Gets the full name of this amplifier as recognized by the inventory database
		 * @return The full name of this amplifier as recognized by the inventory database
		 */
		public String getFullItemName()
		{
			return AMPLIFIER_NAME + " " + _extension;
		}
		
		/**
		 * Gets the display name for this amplifier in this GUI
		 * @return The display name for this amplifier in this GUI
		 */
		public String getDisplayName()
		{
			return C.cClansNether + _display;
		}
		
		public String getCleanDisplayName()
		{
			return _display;
		}
		
		/**
		 * Gets the total duration for this type of amplifier
		 * @return The total duration for this type of amplifier
		 */
		public long getDuration()
		{
			return _duration;
		}
	}
}