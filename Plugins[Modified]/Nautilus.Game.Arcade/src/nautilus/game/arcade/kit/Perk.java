package nautilus.game.arcade.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.modules.perks.PerkSpreadsheetModule;

public abstract class Perk implements Listener
{

	public ArcadeManager Manager;
	public Kit Kit;

	private final String _perkName;
	private String[] _perkDesc;

	private final boolean _display;
	private int _upgradeLevel;

	protected PerkSpreadsheetModule _spreadsheet;

	public Perk(String name)
	{
		this(name, new String[0], true);
	}

	public Perk(String name, String[] perkDesc)
	{
		this(name, perkDesc, true);
	}

	public Perk(String name, String[] perkDesc, boolean display)
	{
		_perkName = name;
		_perkDesc = perkDesc;
		_display = display;
	}

	public boolean hasPerk(Player player)
	{
		boolean hasKit = Kit.HasKit(player);

		if (Kit instanceof LinearUpgradeKit)
		{
			return hasKit && ((LinearUpgradeKit) Kit).getUpgradeLevel(player) == _upgradeLevel;
		}

		return hasKit;
	}

	public void setUpgradeLevel(int upgradeLevel)
	{
		_upgradeLevel = upgradeLevel;
	}

	public void SetHost(Kit kit)
	{
		Manager = kit.Manager;
		Kit = kit;
	}

	public final void setSpreadsheet(PerkSpreadsheetModule spreadsheet)
	{
		_spreadsheet = spreadsheet;
		setupValues();
	}

	public void setupValues()
	{
	}

	public String GetName()
	{
		return _perkName;
	}

	public void setDesc(String... desc)
	{
		_perkDesc = desc;
	}

	public String[] GetDesc()
	{
		return _perkDesc;
	}

	public boolean IsVisible()
	{
		return _display;
	}

	public void Apply(Player player)
	{
		//Null Default
	}

	public void registeredEvents()
	{
		// When listener has been registered
	}

	public void unregisteredEvents()
	{
		// When listener has been registered
	}

	private String getPerkObject(String id)
	{
		String key = _spreadsheet.getKey(Kit, this, id);
		return _spreadsheet.getValue(key);
	}

	protected boolean getPerkBoolean(String id)
	{
		return Boolean.valueOf(getPerkObject(id));
	}

	protected int getPerkInt(String id)
	{
		return Integer.parseInt(getPerkObject(id));
	}

	protected double getPerkDouble(String id)
	{
		return Double.parseDouble(getPerkObject(id));
	}

	protected float getPerkFloat(String id)
	{
		return Float.parseFloat(getPerkObject(id));
	}

	protected double getPerkPercentage(String id)
	{
		String value = getPerkObject(id);
		value = value.substring(0, value.length() - 1);
		double doubleValue = Double.parseDouble(value);
		return doubleValue / 100;
	}

	protected int getPerkTime(String id)
	{
		return getPerkInt(id) * 1000;
	}

	protected boolean getPerkBoolean(String id, boolean defaultV)
	{
		String v = getPerkObject(id);
		return v != null ? Boolean.valueOf(v) : defaultV;
	}

	protected int getPerkInt(String id, int defaultV)
	{
		String v = getPerkObject(id);
		return v != null ? Integer.valueOf(v) : defaultV;
	}

	protected double getPerkDouble(String id, double defaultV)
	{
		String v = getPerkObject(id);
		return v != null ? Double.valueOf(v) : defaultV;
	}

	protected double getPerkFloat(String id, float defaultV)
	{
		String v = getPerkObject(id);
		return v != null ? Float.valueOf(v) : defaultV;
	}

	protected double getPerkPercentage(String id, double defaultV)
	{
		String value = getPerkObject(id);

		if (value == null)
		{
			return defaultV;
		}

		value = value.substring(0, value.length() - 1);
		double doubleValue = Double.parseDouble(value);
		return doubleValue / 100;
	}

	protected int getPerkTime(String id, int defaultV)
	{
		int v = getPerkInt(id, Integer.MIN_VALUE);
		return v == Integer.MIN_VALUE ? defaultV : v * 1000;
	}
}