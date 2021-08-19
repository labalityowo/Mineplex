package nautilus.game.pvp.modules;

import mineplex.core.itemstack.ItemStackFactory;
import me.chiss.Core.Module.AModule;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class Recipes extends AModule
{
	public Recipes(JavaPlugin plugin) 
	{
		super("Recipes", plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void ReplaceDoor(PrepareItemCraftEvent event)
	{
		if (event.getRecipe().getResult() == null)
			return;

		Material type = event.getRecipe().getResult().getType();

		if (type != Material.WOOD_DOOR && type != Material.WOODEN_DOOR)
			return;

		if (!(event.getInventory() instanceof CraftingInventory))
			return;

		CraftingInventory inv = (CraftingInventory)event.getInventory();

		//Feedback
		ItemStack result = ItemStackFactory.Instance.CreateStack(Material.IRON_DOOR);
		inv.setResult(result);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DenySword(PrepareItemCraftEvent event)
	{
		if (event.getRecipe().getResult() == null)
			return;

		Material type = event.getRecipe().getResult().getType();

		if (type != Material.DIAMOND_SWORD && type != Material.GOLD_SWORD &&
				type != Material.DIAMOND_AXE && type != Material.GOLD_AXE)
			return;

		if (!(event.getInventory() instanceof CraftingInventory))
			return;

		CraftingInventory inv = (CraftingInventory)event.getInventory();

		for (ItemStack cur : inv.getMatrix())
			if (cur != null)
				if (cur.getType() == Material.GOLD_BLOCK || cur.getType() == Material.DIAMOND_BLOCK)
					return;

		String name = ItemStackFactory.Instance.GetName(event.getRecipe().getResult(), true);
		String matName = "Gold";
		if (type == Material.DIAMOND_AXE || type == Material.DIAMOND_SWORD)
			matName = "Diamond";

		//Feedback
		ItemStack result = ItemStackFactory.Instance.CreateStack(36, (byte)0, 1, "§r" + C.cGray + "Recipe changed for " + F.item(name) + ".",
					new String[] {C.cGray + "Use " + F.item(matName + " Blocks") + " instead of " + F.item(matName + " Ingots") + "."});

		inv.setResult(result);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DenyGeneral(PrepareItemCraftEvent event)
	{
		if (event.getRecipe().getResult() == null)
			return;

		Material type = event.getRecipe().getResult().getType();

		if (
				type != Material.GOLDEN_APPLE &&
				type != Material.GOLDEN_CARROT &&
				type != Material.ENDER_CHEST &&
				type != Material.ENCHANTMENT_TABLE &&
				type != Material.BREWING_STAND &&
				type != Material.TNT)
			return;

		if (!(event.getInventory() instanceof CraftingInventory))
			return;

		CraftingInventory inv = (CraftingInventory)event.getInventory();

		String name = ItemStackFactory.Instance.GetName(event.getRecipe().getResult(), true);

		//Feedback
		ItemStack result = ItemStackFactory.Instance.CreateStack(36, (byte)0, 1, 
				"§r" + C.cGray + "Crafting of " + F.item(name) + " is disabled.", new String[] {});

		inv.setResult(result);
	}

	@Override
	public void enable() 
	{

		ShapedRecipe goldAxe = new ShapedRecipe(new ItemStack(Material.GOLD_AXE, 1));
		goldAxe.shape("#MM","#SM","#S#");
		goldAxe.setIngredient('M', Material.GOLD_BLOCK);
		goldAxe.setIngredient('S', Material.STICK);
		UtilServer.getServer().addRecipe(goldAxe);

		ShapedRecipe diamondAxe = new ShapedRecipe(new ItemStack(Material.DIAMOND_AXE, 1));
		diamondAxe.shape("#MM","#SM","#S#");
		diamondAxe.setIngredient('M', Material.DIAMOND_BLOCK);
		diamondAxe.setIngredient('S', Material.STICK);
		UtilServer.getServer().addRecipe(diamondAxe);

		ShapedRecipe goldSword = new ShapedRecipe(new ItemStack(Material.GOLD_SWORD, 1));
		goldSword.shape("M","M","S");
		goldSword.setIngredient('M', Material.GOLD_BLOCK);
		goldSword.setIngredient('S', Material.STICK);
		UtilServer.getServer().addRecipe(goldSword);

		ShapedRecipe diamondSword = new ShapedRecipe(new ItemStack(Material.DIAMOND_SWORD, 1));
		diamondSword.shape("M","M","S");
		diamondSword.setIngredient('M', Material.DIAMOND_BLOCK);
		diamondSword.setIngredient('S', Material.STICK);
		UtilServer.getServer().addRecipe(diamondSword);

		//Iron Door
		ShapedRecipe ironDoor = new ShapedRecipe(new ItemStack(Material.IRON_DOOR, 1));
		ironDoor.shape("I","I");
		ironDoor.setIngredient('I', Material.IRON_INGOT);
		UtilServer.getServer().addRecipe(ironDoor);

		//Chain Helm
		ShapedRecipe chainHelm = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_HELMET, 1));

		chainHelm.shape("SIS","I#I");

		chainHelm.setIngredient('I', Material.IRON_INGOT);
		chainHelm.setIngredient('S', Material.GOLD_INGOT);

		UtilServer.getServer().addRecipe(chainHelm);

		//Chain Chest
		ShapedRecipe chainChest = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));

		chainChest.shape("I#I","SIS","ISI");

		chainChest.setIngredient('I', Material.IRON_INGOT);
		chainChest.setIngredient('S', Material.GOLD_INGOT);

		UtilServer.getServer().addRecipe(chainChest);

		//Chain Legs
		ShapedRecipe chainLegs = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));

		chainLegs.shape("ISI","S#S","I#I");

		chainLegs.setIngredient('I', Material.IRON_INGOT);
		chainLegs.setIngredient('S', Material.GOLD_INGOT);

		UtilServer.getServer().addRecipe(chainLegs);

		//Chain Boots
		ShapedRecipe chainBoots = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_BOOTS, 1));

		chainBoots.shape("S#S","I#I");

		chainBoots.setIngredient('I', Material.IRON_INGOT);
		chainBoots.setIngredient('S', Material.GOLD_INGOT);

		UtilServer.getServer().addRecipe(chainBoots);

		//Chain Helm
		ShapedRecipe chainHelm2 = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_HELMET, 1));

		chainHelm2.shape("SIS","I#I");

		chainHelm2.setIngredient('I', Material.GOLD_INGOT);
		chainHelm2.setIngredient('S', Material.IRON_INGOT);

		UtilServer.getServer().addRecipe(chainHelm2);

		//Chain Chest
		ShapedRecipe chainChest2 = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));

		chainChest2.shape("I#I","SIS","ISI");

		chainChest2.setIngredient('I', Material.GOLD_INGOT);
		chainChest2.setIngredient('S', Material.IRON_INGOT);

		UtilServer.getServer().addRecipe(chainChest2);

		//Chain Legs
		ShapedRecipe chainLegs2 = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));

		chainLegs2.shape("ISI","S#S","I#I");

		chainLegs2.setIngredient('I', Material.GOLD_INGOT);
		chainLegs2.setIngredient('S', Material.IRON_INGOT);

		UtilServer.getServer().addRecipe(chainLegs2);

		//Chain Boots
		ShapedRecipe chainBoots2 = new ShapedRecipe(new ItemStack(Material.CHAINMAIL_BOOTS, 1));

		chainBoots2.shape("S#S","I#I");

		chainBoots2.setIngredient('I', Material.GOLD_INGOT);
		chainBoots2.setIngredient('S', Material.IRON_INGOT);

		UtilServer.getServer().addRecipe(chainBoots2);
	}

	@Override
	public void disable() 
	{

	}

	@Override
	public void config()
	{

	}

	@Override
	public void commands() 
	{

	}

	@Override
	public void command(Player caller, String cmd, String[] args)
	{

	}
}
