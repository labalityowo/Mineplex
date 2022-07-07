package nautilus.game.arcade.game.games.zombiesurvival.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkIronSkin;
import nautilus.game.arcade.kit.perks.PerkLeap;
import nautilus.game.arcade.kit.perks.PerkRegeneration;
import nautilus.game.arcade.kit.perks.PerkStrength;

public class KitUndeadZombie extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1, 1, 8000),
					new PerkStrength(1),
					new PerkIronSkin(1),
					new PerkRegeneration(0)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.STONE_AXE),
			};

	public KitUndeadZombie(ArcadeManager manager)
	{
		super(manager, GameKit.ZOMBIE_SURVIVAL_UNDEAD, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		Manager.GetDisguise().disguise(disguise);
	}
}

