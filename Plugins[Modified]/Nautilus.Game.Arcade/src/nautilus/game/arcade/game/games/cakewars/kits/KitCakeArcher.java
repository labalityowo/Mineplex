package nautilus.game.arcade.game.games.cakewars.kits;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitCakeArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(6, 3, true)
			};

	private final Set<Entity> _piercingArrows;

	public KitCakeArcher(ArcadeManager manager)
	{
		super(manager, GameKit.CAKE_WARS_ARCHER, PERKS);

		_piercingArrows = new HashSet<>();
	}

	@Override
	public void GiveItems(Player player)
	{
	}

	@EventHandler
	public void bowShoot(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (!HasKit(player) || event.getForce() != 1)
		{
			return;
		}

		_piercingArrows.add(event.getProjectile());
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (!_piercingArrows.remove(event.getEntity()))
		{
			return;
		}

		Manager.runSyncLater(() ->
		{
			Block block = UtilEnt.getHitBlock(event.getEntity());

			if (block.getType() != Material.WOOL || !((CakeWars) Manager.GetGame()).getCakePlayerModule().getPlacedBlocks().contains(block))
			{
				return;
			}

			block.breakNaturally();
		}, 0);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();

		if (killer == null || !HasKit(killer))
		{
			return;
		}

		for (Perk perk : GetPerks())
		{
			if (perk instanceof PerkFletcher)
			{
				killer.getInventory().addItem(((PerkFletcher) perk).getItem(1));
				return;
			}
		}
	}
}
