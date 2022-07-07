package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.perks.skeleton.PerkBarrage;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;
import nautilus.game.arcade.kit.perks.PerkQuickshot;

public class KitArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(20, 3, true),
					new PerkBarrage(5, 250, true, false),
					new PerkQuickshot("Quick Shot", 2, 30000, true)
			};

	public static final ItemStack BOW = new ItemBuilder(Material.BOW)
			.setTitle(C.cYellow + "Archer's Bow")
			.setUnbreakable(true)
			.build();

	public KitArcher(ArcadeManager manager)
	{
		super(manager, GameKit.SG_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerDeath(PlayerDeathEvent event)
	{
		if (!HasKit(event.getEntity()))
		{
			return;
		}

		event.getDrops().removeIf(itemStack -> itemStack.equals(BOW));
	}
}
