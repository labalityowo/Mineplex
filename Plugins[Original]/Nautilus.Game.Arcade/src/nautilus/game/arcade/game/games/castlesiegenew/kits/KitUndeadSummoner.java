package nautilus.game.arcade.game.games.castlesiegenew.kits;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.castlesiegenew.perks.MobPotion;
import nautilus.game.arcade.game.games.castlesiegenew.perks.PerkMobPotions;
import nautilus.game.arcade.kit.Perk;

public class KitUndeadSummoner extends KitCastleSiege
{

	private static final MobPotion SILVER_FISH = new MobPotion(
			new ItemBuilder(Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SLIME))
					.setTitle(C.cGreen + "Slime Egg")
					.build(), EntityType.SLIME, 3);
	private static final MobPotion ZOMBIE = new MobPotion(
			new ItemBuilder(Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.ZOMBIE))
					.setTitle(C.cGreen + "Zombie Egg")
					.build(), EntityType.ZOMBIE, 1);
	private static final MobPotion SPIDER = new MobPotion(
			new ItemBuilder(Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SPIDER))
					.setTitle(C.cGreen + "Spider Egg")
					.build(), EntityType.SPIDER, 1);

	private static final Perk[] PERKS =
			{
					new PerkMobPotions(TimeUnit.SECONDS.toMillis(28), SILVER_FISH, ZOMBIE, SPIDER),
			};

	public KitUndeadSummoner(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_UNDEAD_SUMMONER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		giveItems(player);
		disguise(player, DisguiseSkeleton.class);

		for (Perk perk : GetPerks())
		{
			for (MobPotion potion : ((PerkMobPotions) perk).getMobPotions())
			{
				player.getInventory().addItem(potion.getItemStack());
			}
		}
	}
}
