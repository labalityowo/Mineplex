package mineplex.core.preferences;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.donation.DonationManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.preferences.command.PreferencesCommand;
import mineplex.core.preferences.ui.PreferencesShop;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class PreferencesManager extends MiniPlugin implements ILoginProcessor
{
	public enum Perm implements Permission
	{
		VIEW_EXCLUSIVE_MENU,
		PREFERENCES_COMMAND,
	}

	private final PreferencesRepository _repository;
	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final IncognitoManager _incognitoManager;
	private final PreferencesShop _shop;

	private final Set<UserPreferences> _saveBuffer = Sets.newHashSet();
	private final Map<Integer, UserPreferences> _preferences = Maps.newHashMap();

	public PreferencesManager(JavaPlugin plugin, IncognitoManager incognito, CoreClientManager clientManager)
	{
		super("Preferences", plugin);

		_repository = new PreferencesRepository(this);
		_clientManager = clientManager;
		_donationManager = require(DonationManager.class);
		_incognitoManager = incognito;
		_clientManager.addStoredProcedureLoginProcessor(this);
		_shop = new PreferencesShop(this);

		addCommand(new PreferencesCommand(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		for (Preference p : Preference.values())
		{
			if (p.getCategory() == PreferenceCategory.EXCLUSIVE)
			{
				if (p == Preference.INVISIBILITY)
				{
					PermissionGroup.ADMIN.setPermission(p, true, true);
					PermissionGroup.CONTENT.setPermission(p, true, true);
					PermissionGroup.YT.setPermission(p, false, false);
				}
				else if (p == Preference.FORCE_FIELD)
				{
					PermissionGroup.CONTENT.setPermission(p, true, true);
					PermissionGroup.EVENTMOD.setPermission(p, false, true);
				}
				else if (p == Preference.GLOBAL_GWEN_REPORTS)
				{
					PermissionGroup.TRAINEE.setPermission(p, true, true);
				}
				else if (p == Preference.SHOW_USER_REPORTS)
				{
					PermissionGroup.TRAINEE.setPermission(p, true, true);
				}
				else if (p == Preference.IGNORE_VELOCITY)
				{
					PermissionGroup.BUILDER.setPermission(p, true, true);
				}
				else if (p == Preference.UNLOCK_KITS)
				{
					PermissionGroup.CONTENT.setPermission(p, true, true);
					PermissionGroup.BUILDER.setPermission(p, true, true);
				}
				else
				{
					PermissionGroup.ADMIN.setPermission(p, true, true);
				}
			}
			else
			{
				if (p == Preference.COLOR_SUFFIXES)
				{
					PermissionGroup.TITAN.setPermission(p, true, true);
				}
				else
				{
					PermissionGroup.PLAYER.setPermission(p, true, true);
				}
			}
		}
		
		PermissionGroup.CONTENT.setPermission(Perm.VIEW_EXCLUSIVE_MENU, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.VIEW_EXCLUSIVE_MENU, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.PREFERENCES_COMMAND, true, true);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		int accountId = _clientManager.getAccountId(player);
		UserPreferences p = _preferences.remove(accountId);
		if (p != null) _repository.saveUserPreferences(p);
	}

	@EventHandler
	public void storeBuffer(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		if (_saveBuffer.isEmpty())
		{
			return;
		}

		List<UserPreferences> buffer = Lists.newArrayList(_saveBuffer);

		buffer.forEach(_repository::saveUserPreferences);

		buffer.clear();
		_saveBuffer.clear();

	}

	public UserPreferences get(Player player)
	{
		UserPreferences preferences = _preferences.get(_clientManager.getAccountId(player));
		if(preferences == null)
		{
			preferences = new UserPreferences(_clientManager.getAccountId(player));
		}
		return preferences;
	}

	public void save(UserPreferences preferences)
	{
		_saveBuffer.add(preferences);
	}

	public IncognitoManager getIncognitoManager()
	{
		return _incognitoManager;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public void openMenu(Player player)
	{
		_shop.attemptShopOpen(player);
	}

	public void set(int accountId, UserPreferences preferences)
	{
		_preferences.put(accountId, preferences);
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		UserPreferences preferences = new UserPreferences(accountId);
		while (resultSet.next())
		{
			Preference preference = Preference.get(resultSet.getInt("preference"));
			boolean value = resultSet.getInt("value") == 1;
			preferences.set(preference, value);
		}
		_preferences.put(accountId, preferences);
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT * FROM `preferences` WHERE `accountId` = " + accountId + ";";
	}
}