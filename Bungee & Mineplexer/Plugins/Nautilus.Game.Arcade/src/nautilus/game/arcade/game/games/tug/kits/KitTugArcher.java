package nautilus.game.arcade.game.games.tug.kits;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitTugArcher extends Kit
{

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.STONE_SWORD)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.BOW)
							.setUnbreakable(true)
							.build()
			};

	private static final ItemStack[] PLAYER_ARMOUR =
			{
					new ItemBuilder(Material.CHAINMAIL_BOOTS)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.CHAINMAIL_LEGGINGS)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.CHAINMAIL_HELMET)
							.setUnbreakable(true)
							.build()
			};

	private static final Perk[] PERKS =
			{
					new PerkFletcher(3, 2, true),
			};

	public KitTugArcher(ArcadeManager manager)
	{
		super(manager, GameKit.TUG_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOUR);
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		ProjectileSource shooter = event.getEntity().getShooter();

		if (!(shooter instanceof Player))
		{
			return;
		}

		Player player = (Player) shooter;

		if (!HasKit(player))
		{
			return;
		}

		Location location = event.getEntity().getLocation().add(0, 1, 0);
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, null, 0, 1, ViewDist.LONG);
		location.getWorld().playSound(location, Sound.EXPLODE, 1.3F, 1);

		UtilEnt.getInRadius(event.getEntity().getLocation(), 6).forEach((nearby, scale) ->
		{
			Manager.GetDamage().NewDamageEvent(nearby, player, event.getEntity(), DamageCause.CUSTOM, 8 * scale, true, true, false, player.getName(), "Explosive Arrow");
		});
	}
}
