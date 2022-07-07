package nautilus.game.arcade.game.games.snowfight.kits;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.game.kit.GameKit;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;

public class KitMedic extends KitSnowFight
{

	private static final Potion POTION = new Potion(PotionType.INSTANT_HEAL).splash();
	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(12);

	static
	{
		POTION.setLevel(2);
	}

	public KitMedic(ArcadeManager manager)
	{
		super(manager, GameKit.SNOW_FIGHT_MEDIC);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(1, getPotion());

		super.GiveItems(player);
	}

	@EventHandler
	public void kitItems(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !Manager.GetGame().InProgress())
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (HasKit(player) && !player.getInventory().contains(Material.POTION) && Recharge.Instance.usable(player, GetName()))
			{
				player.getInventory().setItem(1, getPotion());
			}
		}
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || itemStack.getType() != Material.POTION || !HasKit(player))
		{
			return;
		}

		Recharge.Instance.useForce(player, GetName(), COOLDOWN);
	}

	private ItemStack getPotion()
	{
		ItemStack stack = POTION.toItemStack(1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(C.cYellow + "Warmth Potion");
		stack.setItemMeta(meta);
		return stack;
	}
}
