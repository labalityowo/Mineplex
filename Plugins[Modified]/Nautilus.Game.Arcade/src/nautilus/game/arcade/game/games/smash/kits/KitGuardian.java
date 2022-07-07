package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.guardian.PerkTargetLazer;
import nautilus.game.arcade.game.games.smash.perks.guardian.PerkThorns;
import nautilus.game.arcade.game.games.smash.perks.guardian.PerkWaterSplash;
import nautilus.game.arcade.game.games.smash.perks.guardian.PerkWhirlpoolBlade;
import nautilus.game.arcade.game.games.smash.perks.guardian.SmashGuardian;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitGuardian extends SmashKit
{

	private static final Perk[] PERKS = {
		new PerkSmashStats(),
		new PerkDoubleJump("Double Jump"),
		new PerkWhirlpoolBlade(),
		new PerkWaterSplash(),
		new PerkTargetLazer(),
		new PerkThorns(),
		new SmashGuardian()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
		ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1, 
				C.cYellowB + "Right-Click" + C.cWhiteB + " - " + C.cGreenB + "Whirlpool Axe",
				new String[] {
					C.Reset + "Fires a Prismarine Shard that deals damage to",
					C.Reset + "the first player it collides with.",
					C.Reset + "The player is then drawn towards you."
				}),
		ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1, 
				C.cYellowB + "Right-Click" + C.cWhiteB + " - " + C.cGreenB + "Water Splash",
				new String[] {
					C.Reset + "You bounce into the air and pull all nearby players",
					C.Reset + "towards you.",
					C.Reset + "Blocking with the sword while bouncing increases the height.",
					C.Reset + "Landing causes a water splash dealing damage and knockback",
					C.Reset + "to nearby players."
				}),
		ItemStackFactory.Instance.CreateStack(Material.IRON_PICKAXE, (byte) 0, 1, 
				C.cYellowB + "Right-Click" + C.cWhiteB + " - " + C.cGreenB + "Target Laser",
				new String[] {
					C.Reset + "You target the nearest player with your laser.",
					C.Reset + "That player takes increased damage and knockback from you.",
					C.Reset + "Your laser breaks if you get too far away or after some time."
				}),
		ItemStackFactory.Instance.CreateStack(Material.PRISMARINE_SHARD, (byte) 0, 1, 
				C.cYellowB + "Passive" + C.cWhiteB + " - " + C.cGreenB + "Thorns",
				new String[] {
					C.Reset + "Takes 66% less damage and knockback from projectiles",
					C.Reset + "when under 10 health.",
				}),
		ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1, 
				C.cYellowB + "Smash Crystal" + C.cWhiteB + " - " + C.cGreenB + "Rise of the Guardian",
				new String[] {
					C.Reset + "You call upon Gwen who begins to charge her laser.",
					C.Reset + "Any player near the laser is drawn to it and cannot",
					C.Reset + "escape from it.",
					C.Reset + "Once the laser has charged all players nearby get hit",
					C.Reset + "with HUGE damage and knockback!"
				}),
	};
	
	private static final ItemStack[] PLAYER_ARMOR = {
		ItemStackFactory.Instance.CreateStack(Material.DIAMOND_BOOTS),
		ItemStackFactory.Instance.CreateStack(Material.DIAMOND_LEGGINGS),
		null,
		null,
	};
	
	public KitGuardian(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_GUARDIAN, PERKS, DisguiseGuardian.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1], PLAYER_ITEMS[2]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[3], PLAYER_ITEMS[4]);	
		
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
		
}
