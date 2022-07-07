package mineplex.core.gadget.gadgets.gamemodifiers.minestrike;

import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.game.GameDisplay;

public class GameModifierMineStrikeSkin extends GameModifierGadget
{

	private final String _weapon;
	private final Material _skinMat;
	private final byte _skinData;

	public GameModifierMineStrikeSkin(GadgetManager manager, GameCosmeticCategory category, String name, String[] lore, String weaponName, Material newSkin, byte newSkinData, int cost, Material displayMat, int displayData)
	{
		super(manager, category, name, UtilText.splitLinesToArray(lore, LineFormat.LORE), cost, displayMat, (byte) displayData);
		_weapon = weaponName;
		_skinMat = newSkin;
		_skinData = newSkinData;
	}

	public GameModifierMineStrikeSkin(GadgetManager manager, GameCosmeticCategory category, MineStrikeSkin skin, int cost)
	{
		this(manager, category, skin, new String[]{C.cGray + "Weapon: " + C.cYellow + skin.getWeaponName()}, cost);
	}

	public GameModifierMineStrikeSkin(GadgetManager manager, GameCosmeticCategory category, MineStrikeSkin skin, String[] lore, int cost)
	{
		this(manager, category, skin.getSkinName(), lore, skin.getWeaponName(), skin.getSkinMaterial(), skin.getSkinData(), cost, skin.getSkinMaterial(), skin.getSkinData());
	}

	public String getWeaponName()
	{
		return _weapon;
	}

	public Material getSkinMaterial()
	{
		return _skinMat;
	}

	public byte getSkinData()
	{
		return _skinData;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		Manager.getGameCosmeticManager().getGadgetsFrom(GameDisplay.MineStrike).stream().filter(getWeaponFilter(_weapon).and(weapon -> weapon != this)).forEach(g -> g.disable(player));

		super.enableCustom(player, message);
	}

	/**
	 * A simple {@link GameModifierGadget} filter which filters out {@link GameModifierMineStrikeSkin} gadgets depending on weapon name
	 *
	 * @param weaponName Exact weapon name to test for
	 * @return Returns a weapon filter which will filter out any {@link GameModifierGadget}
	 * which is not instance of {@link GameModifierMineStrikeSkin} and which does not match the provided weapon name
	 */
	public static Predicate<GameModifierGadget> getWeaponFilter(String weaponName)
	{
		return g -> g instanceof GameModifierMineStrikeSkin && ((GameModifierMineStrikeSkin) g).getWeaponName().equals(weaponName);
	}
}
