package nautilus.game.arcade.game.games.evolution.mobs;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseSpider;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.mobs.perks.PerkWebEVO;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkConstructor;
import nautilus.game.arcade.kit.perks.PerkNoFallDamage;
import nautilus.game.arcade.kit.perks.PerkSpeed;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class KitSpider extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
	public KitSpider(ArcadeManager manager)
	{
		super(manager, "Spider", new String[]
		{
				C.cYellow + "Ability: " + C.cWhite + "Web Shot", C.Line,
				C.cGreen + "Speed 1", C.cGreen + "No Fall Damage"
		}, 32, 5, new Perk[]
		{
				new PerkWebEVO(),
				new PerkConstructor("Web Weaver", 3.0, 4, Material.WEB,
						"Spiderweb", false), new PerkSpeed(0),
						new PerkNoFallDamage()

		}, EntityType.SPIDER);
	}

	@Override
	protected void giveItems(Player player)
	{
		player.getInventory().setItem(0, new ItemBuilder(Material.SPIDER_EYE).setTitle(C.cYellow + C.Bold + "Right Click" + C.cWhite + " - " + C.cGreen + C.Bold + "Web Shot").build());

		player.getWorld().playSound(player.getLocation(), Sound.SPIDER_IDLE, 4f, 1f);
		
		//Disguise
		DisguiseSpider disguise = new DisguiseSpider(player);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		
		Manager.GetDisguise().undisguise(player);
		Manager.GetDisguise().disguise(disguise);		
	}
}
