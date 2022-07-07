package mineplex.core.preferences;

import com.google.common.collect.Maps;

import java.util.Map;

public class UserPreferences
{
	private final int _accountId;
	private Map<Preference, Boolean> _preferences;

	public UserPreferences(int accountId)
	{
		_accountId = accountId;
		_preferences = Maps.newHashMap();
	}

	public boolean isActive(Preference preference)
	{
		return _preferences.getOrDefault(preference, preference.getDefaultValue());
	}

	public void toggle(Preference preference)
	{
		boolean enabled = _preferences.getOrDefault(preference, preference.getDefaultValue());
		_preferences.put(preference, !enabled);
	}

	public void enable(Preference preference)
	{
		_preferences.put(preference, true);
	}

	public void disable(Preference preference)
	{
		_preferences.put(preference, false);
	}

	public void set(Preference preference, boolean value)
	{
		_preferences.put(preference, value);
	}

	public int getAccountId()
	{
		return _accountId;
	}
}