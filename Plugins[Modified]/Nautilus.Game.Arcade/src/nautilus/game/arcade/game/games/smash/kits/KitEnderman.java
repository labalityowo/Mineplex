package nautilus.game.arcade.game.games.smash.kits;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseEnderman;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.enderman.PerkBlink;
import nautilus.game.arcade.game.games.smash.perks.enderman.PerkBlockToss;
import nautilus.game.arcade.game.games.smash.perks.enderman.PerkEndermanTeleport;
import nautilus.game.arcade.game.games.smash.perks.enderman.SmashEnderman;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.event.PerkBlockGrabEvent;
import nautilus.game.arcade.kit.perks.event.PerkBlockThrowEvent;

public class KitEnderman extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkBlink("Blink"),
	  new PerkBlockToss(),
	  new PerkEndermanTeleport(),
	  new SmashEnderman()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold/Release Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Block Toss",
		new String[]{
		  ChatColor.RESET + "Picks up a block from the ground, and",
		  ChatColor.RESET + "then hurls it at opponents, causing huge",
		  ChatColor.RESET + "damage and knockback if it hits.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "The longer you hold the block, the harder",
		  ChatColor.RESET + "you throw it. You will hear a 'tick' sound",
		  ChatColor.RESET + "when it is fully charged.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Blink",
		new String[]{
		  ChatColor.RESET + "Instantly teleport in the direction",
		  ChatColor.RESET + "you are looking.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "You cannot pass through blocks.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Dragon Rider",
		new String[]{
		  ChatColor.RESET + "Summon a dragon from The End to fly into",
		  ChatColor.RESET + "your opponents, dealing devastating damage.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
	};

	private final Map<Player, Pair<Integer, Byte>> _heldBlock;

	public KitEnderman(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_ENDERMAN, PERKS, DisguiseEnderman.class);

		_heldBlock = new HashMap<>(4);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);

		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			player.getInventory().addItem(PLAYER_ITEMS[2]);
		}
		
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}

	@EventHandler
	public void BlockGrab(PerkBlockGrabEvent event)
	{
		setBlock(event.GetPlayer(), event.GetId(), event.GetData(), true);
	}

	@EventHandler
	public void BlockThrow(PerkBlockThrowEvent event)
	{
		setBlock(event.GetPlayer(), 0, (byte) 0, true);
	}

	@EventHandler
	public void Death(PlayerDeathEvent event)
	{
		setBlock(event.getEntity(), 0, (byte) 0, true);
	}

	@EventHandler
	public void updateHeldBlock(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !Manager.GetGame().IsLive())
		{
			return;
		}

		_heldBlock.forEach((player, pair) -> setBlock(player, pair.getLeft(), pair.getRight(), false));
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_heldBlock.remove(event.getPlayer());
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		Player player = event.GetDamageePlayer();

		if (player == null)
		{
			return;
		}

		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);

		if (disguise != null && disguise instanceof DisguiseEnderman)
		{
			// Endermen drop their held block when damaged. This means the client renders it this way, so we need to resend the correct data on the next tick.
			Manager.runSyncLater(() -> Manager.GetDisguise().updateDisguise(disguise), 1);
		}
	}

	public void setBlock(Player player, int id, byte data, boolean save)
	{
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);

		if (player != null && disguise != null && disguise instanceof DisguiseEnderman)
		{
			DisguiseEnderman disguiseEnderman = (DisguiseEnderman) disguise;
			disguiseEnderman.SetCarriedId(id);
			disguiseEnderman.SetCarriedData(data);

			Manager.GetDisguise().updateDisguise(disguiseEnderman);

			if (save)
			{
				_heldBlock.put(player, Pair.create(id, data));
			}
		}
	}
}
