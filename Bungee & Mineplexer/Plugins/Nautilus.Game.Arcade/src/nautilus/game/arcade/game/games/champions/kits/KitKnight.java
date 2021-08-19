package nautilus.game.arcade.game.games.champions.kits;

import java.util.HashMap;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.ChampionsKit;

public class KitKnight extends ChampionsKit
{

	private HashMap<Player, ClientClass> _class = new HashMap<Player, ClientClass>();

	public KitKnight(ArcadeManager manager)
	{
		super(manager, GameKit.CHAMPIONS_KNIGHT);
	}

	@Override
	public void Deselected(Player player)
	{
		_class.remove(player);
	}

	@Override
	public void Selected(Player player)
	{
		Manager.Clear(player);

		AddPlayer(player);

		Manager.openClassShop(player);
	}

	public void AddPlayer(Player player)
	{
		_class.put(player, Manager.getClassManager().Get(player));
		ClientClass clientClass = _class.get(player);
		IPvpClass pvpClass = Manager.getClassManager().GetClass("Knight");

		clientClass.SetGameClass(pvpClass);
		pvpClass.ApplyArmor(player);
		clientClass.ClearDefaults();
		clientClass.EquipCustomBuild(clientClass.GetCustomBuilds(pvpClass).get(0));

		if (!Manager.GetGame().InProgress())
		{
			clientClass.SetActiveCustomBuild(pvpClass, pvpClass.getDefaultBuild());
		}
	}

	@Override
	public void GiveItems(Player player)
	{
		_class.get(player).ResetToDefaults(true, true);
	}
}
