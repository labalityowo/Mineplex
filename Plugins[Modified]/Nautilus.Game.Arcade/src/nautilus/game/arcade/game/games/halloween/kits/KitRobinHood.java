package nautilus.game.arcade.game.games.halloween.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.perks.skeleton.PerkBarrage;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;
import nautilus.game.arcade.kit.perks.PerkQuickshotRobinHood;

public class KitRobinHood extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(3, 6, true),
					new PerkBarrage(8, 125, true, false),
					new PerkQuickshotRobinHood()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1, F.item("Sword")),
					ItemStackFactory.Instance.CreateStack(Material.BOW, (byte) 0, 1, F.item("Bow")),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP)
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.JACK_O_LANTERN),
			};

	public KitRobinHood(ArcadeManager manager)
	{
		super(manager, GameKit.HALLOWEEN_ROBIN_HOOD, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}

	@EventHandler
	public void Aura(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST)
		{
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (!HasKit(player))
					continue;

				for (Player other : Manager.GetGame().GetPlayers(true))
				{
					if (other.equals(player))
						continue;

					if (UtilMath.offset(player, other) > 8)
						continue;

					Manager.GetCondition().Factory().Regen("Aura", other, player, 1.9, 0, false, false, false);
				}
			}
		}

		if (event.getType() == UpdateType.SLOW)
		{
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (!HasKit(player))
					continue;

				for (Player other : Manager.GetGame().GetPlayers(true))
				{
					if (other.equals(player))
						continue;

					if (UtilMath.offset(player, other) > 8)
						continue;

					UtilPlayer.health(other, 1);
				}
			}
		}
	}
}
