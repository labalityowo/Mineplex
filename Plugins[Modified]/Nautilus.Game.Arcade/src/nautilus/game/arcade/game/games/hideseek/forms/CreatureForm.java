package nautilus.game.arcade.game.games.hideseek.forms;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.disguise.disguises.*;
import nautilus.game.arcade.game.games.hideseek.HideSeek;
import net.minecraft.server.v1_8_R3.Entity;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreatureForm extends Form
{
	private EntityType _type;

	private DisguiseBase _disguise;

	public CreatureForm(HideSeek host, Player player, EntityType entityType) 
	{
		super(host, player);

		_type = entityType;

		Apply();
	}

	@Override
	public void Apply() 
	{
		Material icon = Material.PORK;
		
		if (_type == EntityType.CHICKEN)			{_disguise = new DisguiseChicken(Player);	icon = Material.FEATHER;}
		else if (_type == EntityType.COW)			{_disguise = new DisguiseCow(Player);		icon = Material.LEATHER;}
		else if (_type == EntityType.SHEEP)			{_disguise = new DisguiseSheep(Player);		icon = Material.MUTTON;}
		else if (_type == EntityType.PIG)			{_disguise = new DisguisePig(Player);		icon = Material.PORK;}

		_disguise.setSoundDisguise(new DisguiseCat(Player));
		_disguise.setLockPitch(true);
		Host.Manager.GetDisguise().disguise(_disguise);

		((CraftEntity)Player).getHandle().getDataWatcher().watch(0, (byte) 0, Entity.META_ENTITYDATA, (byte) 0);

		//Inform
		UtilPlayer.message(Player, F.main("Game", C.cWhite + "You are now a " + F.elem(UtilEnt.getName(_type)) + "!"));

		//Give Item
		Player.getInventory().setItem(8, new ItemStack(Host.GetItemEquivilent(icon)));
		UtilInv.Update(Player);

		//Sound
		Player.playSound(Player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);
	}

	@Override
	public void Remove() 
	{
		Host.Manager.GetDisguise().undisguise(Player);

		((CraftEntity)Player).getHandle().getDataWatcher().watch(0, (byte) 0, Entity.META_ENTITYDATA, (byte) 0);
	}
}
