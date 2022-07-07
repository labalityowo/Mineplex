package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.pig.PerkPigBaconBomb;
import nautilus.game.arcade.game.games.smash.perks.pig.PerkPigBaconBounce;
import nautilus.game.arcade.game.games.smash.perks.pig.PerkPigZombie;
import nautilus.game.arcade.game.games.smash.perks.pig.SmashPig;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitPig extends SmashKit
{

	private static final float MAX_ENERGY = 0.99F;
	private static final float ENERGY_PER_TICK_NORMAL = 0.005F;
	private static final float ENERGY_PER_TICK_SMASH = 0.02F;
	
	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkPigBaconBounce(),
	  new PerkPigBaconBomb(),
	  new PerkPigZombie(),
	  new SmashPig()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bouncy Bacon",
		new String[]{
		  ChatColor.RESET + "Bouncy Bacon launches a piece of bacon,",
		  ChatColor.RESET + "dealing damage and knockback to enemies.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Eat the bacon to restore some Energy.",
		  ChatColor.RESET + "Bacon that hits an enemy will restore Health.",

		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Baby Bacon Bombs",
		new String[]{
		  ChatColor.RESET + "Give birth to a baby pig, giving",
		  ChatColor.RESET + "yourself a boost forwards. ",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Your baby pig will run to annoy",
		  ChatColor.RESET + "nearby enemies, exploding on them.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.PORK, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Nether Pig",
		new String[]{
		  ChatColor.RESET + "When your health drops below 4, you morph",
		  ChatColor.RESET + "into a Nether Pig. This gives you Speed I,",
		  ChatColor.RESET + "8 Armor and reduces Energy costs by 33%.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "When your health returns to 6, you return",
		  ChatColor.RESET + "back to Pig Form.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Pig Stink",
		new String[]{
		  ChatColor.RESET + "Unleash your inner pig, causing all enemies",
		  ChatColor.RESET + "to get nausea for a duration, while you become",
		  ChatColor.RESET + "a powerful Nether Pig!",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null,
	};


	public KitPig(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_PIG, PERKS, DisguisePig.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}

	@EventHandler
	public void EnergyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if(Manager.GetGame() == null)
		{
			return;
		}
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!HasKit(player))
			{
				continue;
			}
			
			player.setExp(Math.min(MAX_ENERGY, player.getExp() + (isSmashActive(player) ? ENERGY_PER_TICK_SMASH : ENERGY_PER_TICK_NORMAL)));
		}
	}
}
