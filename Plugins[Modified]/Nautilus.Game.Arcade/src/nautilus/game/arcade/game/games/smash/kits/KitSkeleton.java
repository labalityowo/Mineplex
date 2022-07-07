package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.skeleton.PerkBarrage;
import nautilus.game.arcade.game.games.smash.perks.skeleton.PerkBoneExplosion;
import nautilus.game.arcade.game.games.smash.perks.skeleton.SmashSkeleton;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkFletcher;
import nautilus.game.arcade.kit.perks.PerkKnockbackArrow;
import nautilus.game.arcade.kit.perks.PerkRopedArrow;

public class KitSkeleton extends SmashKit
{

	private static final double ARROW_DAMAGE = 6;

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkFletcher(),
	  new PerkKnockbackArrow(),
	  new PerkBoneExplosion(),
	  new PerkRopedArrow("Roped Arrow"),
	  new PerkBarrage(),
	  new SmashSkeleton()
	};

	private static final ItemStack IN_HAND = new ItemStack(Material.BOW);

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bone Explosion",
		new String[]
		  {
			ChatColor.RESET + "Releases an explosion of bones from",
			ChatColor.RESET + "your body, repelling all nearby enemies.",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.BOW, (byte) 0, 1,
		C.cYellow + C.Bold + "Left-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Roped Arrow",
		new String[]
		  {
			ChatColor.RESET + "Instantly fires an arrow. When it ",
			ChatColor.RESET + "collides with something, you are pulled",
			ChatColor.RESET + "towards it, with great power.",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.ARROW, (byte) 0, 1,
		C.cYellow + C.Bold + "Charge Bow" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Barrage",
		new String[]
		  {
			ChatColor.RESET + "Slowly load more arrows into your bow.",
			ChatColor.RESET + "When you release, you will quickly fire",
			ChatColor.RESET + "all the arrows in succession.",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Arrow Storm",
		new String[]
		  {
			ChatColor.RESET + "Fire hundreds of arrows in quick succession",
			ChatColor.RESET + "which deal damage and knockback to enemies.",
		  })
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
	};

	public KitSkeleton(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_SKELETON, PERKS, DisguiseSkeleton.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);
		}

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void arrowDamage(CustomDamageEvent event)
	{
		Player player = event.GetDamagerPlayer(true);
		Projectile proj = event.GetProjectile();
		
		if (player == null || proj == null)
		{
			return;
		}
		
		if (!(proj instanceof Arrow))
		{
			return;
		}
		
		if (!HasKit(player))
		{
			return;
		}
		
		//Try making arrows deal static damage
		double diff = ARROW_DAMAGE - event.GetDamage();
		
		event.AddMod("Arrow Nerf", diff);
	}
}
