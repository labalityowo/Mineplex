package nautilus.game.arcade.game.games.smash.perks.villager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;

import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.game.games.smash.kits.KitVillager;
import nautilus.game.arcade.game.games.smash.kits.KitVillager.VillagerType;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashVillager extends SmashUltimate
{

	public SmashVillager()
	{
		super("Perfection", new String[0], Sound.VILLAGER_YES, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		for (VillagerType type : VillagerType.values())
		{
			Recharge.Instance.useForce(player, type.getName(), getLength());
		}

		((KitVillager) Kit).updateDisguise(player, Profession.PRIEST);
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		player.setWalkSpeed(0.2F);
		((KitVillager) Kit).updateDisguise(player, Profession.FARMER);
	}
}
