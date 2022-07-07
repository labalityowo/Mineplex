package nautilus.game.pvp.modules;

import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.Rank;
import me.chiss.Core.Combat.CombatLog;
import me.chiss.Core.Combat.Event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.itemstack.ItemStackFactory;
import me.chiss.Core.Module.AModule;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilEvent.ActionType;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Gameplay extends AModule
{
	private HashMap<Block, Long> _bucketWater = new HashMap<Block, Long>();
	private HashMap<String, Byte> _woolColors = new HashMap<String, Byte>();

	//private HashSet<Item> _torch = new HashSet<Item>();

	public Gameplay(JavaPlugin plugin) 
	{
		super("PvP Gameplay", plugin);
	}

	//Module Functions
	@Override
	public void enable() 
	{
		_woolColors = new HashMap<String, Byte>();
		_woolColors.put("White", 		(byte)0);
		_woolColors.put("Orange", 		(byte)1);
		_woolColors.put("Magenta", 		(byte)2);
		_woolColors.put("LightBlue", 	(byte)3);
		_woolColors.put("Yellow", 		(byte)4);
		_woolColors.put("Lime", 		(byte)5);
		_woolColors.put("Pink", 		(byte)6);
		_woolColors.put("Gray", 		(byte)7);
		_woolColors.put("LightGray", 	(byte)8);
		_woolColors.put("Cyan", 		(byte)9);
		_woolColors.put("Purple", 		(byte)10);
		_woolColors.put("Blue", 		(byte)11);
		_woolColors.put("Brown", 		(byte)12);
		_woolColors.put("Green", 		(byte)13);
		_woolColors.put("Red", 			(byte)14);
		_woolColors.put("Black", 		(byte)15);
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
		AddCommand("gm");
		AddCommand("die");
		AddCommand("wool");
		AddCommand("leather");
	}

	@Override
	public void command(Player caller, String cmd, String[] args) 
	{
		if (cmd.equals("die"))
		{
			Damage().NewDamageEvent(caller, null, null, DamageCause.SUICIDE, 5000, false, true, true, null, null);
			return;
		}
		
		if (cmd.equals("gm"))
		{
			if (Clients().Get(caller).Rank().Has(Rank.ADMIN, true))
			{
				if (caller.getGameMode() == GameMode.SURVIVAL)
					caller.setGameMode(GameMode.CREATIVE);
				else
					caller.setGameMode(GameMode.CREATIVE);
				
				UtilPlayer.message(caller, F.main("Admin", "Toggled Game Mode."));
			}
		}
		
		if (cmd.equals("leather"))
		{
			try
			{
				int r = Integer.valueOf(args[0]);
				int g = Integer.valueOf(args[1]);
				int b = Integer.valueOf(args[2]);
				
				LeatherArmorMeta meta;
				
				meta = (LeatherArmorMeta)caller.getInventory().getHelmet().getItemMeta();
				meta.setColor(Color.fromRGB(r, g, b));
				caller.getInventory().getHelmet().setItemMeta(meta);
				
				meta = (LeatherArmorMeta)caller.getInventory().getChestplate().getItemMeta();
				meta.setColor(Color.fromRGB(r, g, b));
				caller.getInventory().getChestplate().setItemMeta(meta);
				
				meta = (LeatherArmorMeta)caller.getInventory().getLeggings().getItemMeta();
				meta.setColor(Color.fromRGB(r, g, b));
				caller.getInventory().getLeggings().setItemMeta(meta);
				
				meta = (LeatherArmorMeta)caller.getInventory().getBoots().getItemMeta();
				meta.setColor(Color.fromRGB(r, g, b));
				caller.getInventory().getBoots().setItemMeta(meta);
				
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main("Game", "Invalid Input."));
			}
		}

		if (cmd.equals("wool"))
		{
			if (args.length == 0)
			{
				ListWool(caller);
			}
			else
			{
				SetWool(caller, args[0]);
			}

			return;
		}

		if (!Clients().Get(caller).Rank().Has(Rank.ADMIN, true))
			return;
	}

	private void SetWool(Player caller, String search) 
	{
		if (caller.getItemInHand() == null || caller.getItemInHand().getType() != Material.WOOL)
		{
			UtilPlayer.message(caller, F.main("Wool", "You must be holding wool."));
			return;
		}

		HashSet<String> match = new HashSet<String>();

		for (String cur : _woolColors.keySet())
			if (cur.toLowerCase().contains(search.toLowerCase()))
				match.add(cur);

		try
		{
			byte id = Byte.parseByte(search);
			for (String cur : _woolColors.keySet())
				if (_woolColors.get(cur) == id)
					match.add(cur);
		}
		catch (Exception e)
		{

		}

		//No / Non-Unique
		if (match.size() != 1)
		{
			//Inform
			UtilPlayer.message(caller, F.main("Wool Search", "" +
					C.mCount + match.size() +
					C.mBody + " matches for [" +
					C.mElem + search +
					C.mBody + "]."));

			if (match.size() > 0)
			{
				String matchString = "";
				for (String cur : match)
					matchString += F.elem(cur) + ", ";
				if (matchString.length() > 1)
					matchString = matchString.substring(0 , matchString.length() - 2);

				UtilPlayer.message(caller, F.main("Wool Search", "" +
						C.mBody + "Matches [" +
						C.mElem + matchString +
						C.mBody + "]."));
			}

			return;
		}

		for (String cur : match)
		{
			caller.setItemInHand(ItemStackFactory.Instance.CreateStack(Material.WOOL, _woolColors.get(cur), caller.getItemInHand().getAmount()));
			UtilPlayer.message(caller, F.main("Wool", "Set color to " + F.elem(cur) + "."));
		}			
	}

	private void ListWool(Player caller) 
	{
		UtilPlayer.message(caller, F.main("Wool", "Listing Colors;"));
		String colors = "";
		for (String cur : _woolColors.keySet())
			colors += _woolColors.get(cur) + "." + cur + " ";

		UtilPlayer.message(caller, colors);
	}

	@EventHandler
	public void BucketEmpty(PlayerBucketEmptyEvent event)
	{
		event.setCancelled(true);

		Block block = event.getBlockClicked().getRelative(event.getBlockFace());

		if (event.getBucket() == Material.WATER_BUCKET)
		{
			block.setTypeIdAndData(8, (byte)1, true);
			BlockRestore().Add(event.getBlockClicked().getRelative(event.getBlockFace()), 8, (byte)0, 1000);
			_bucketWater.put(block, System.currentTimeMillis());
		}

		if (event.getBucket() == Material.LAVA_BUCKET)
		{
			block.setTypeIdAndData(10, (byte)6, true);
			BlockRestore().Add(event.getBlockClicked().getRelative(event.getBlockFace()), 10, (byte)0, 2000);
		}


		event.getPlayer().setItemInHand(ItemStackFactory.Instance.CreateStack(Material.BUCKET));	
		UtilInv.Update(event.getPlayer());
	}

	@EventHandler
	public void BucketFill(PlayerBucketFillEvent event)
	{
		event.setCancelled(true);

		if (event.getItemStack().getType() == Material.WATER_BUCKET)
			if (!_bucketWater.containsKey(event.getBlockClicked()))
				event.getPlayer().setItemInHand(ItemStackFactory.Instance.CreateStack(Material.WATER_BUCKET));

		UtilInv.Update(event.getPlayer());
	}

	@EventHandler
	public void BucketWaterExpire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		HashSet<Block> remove = new HashSet<Block>();

		for (Block cur : _bucketWater.keySet())
			if (UtilTime.elapsed(_bucketWater.get(cur), 2000))
				remove.add(cur);

		for (Block cur : remove)
			_bucketWater.remove(cur);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void ObsidianCancel(BlockPlaceEvent event)
	{
		if (event.getBlock().getType() == Material.OBSIDIAN)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.item("Obsidian") + "."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void CommandPlace(BlockPlaceEvent event)
	{
		if (event.getBlock().getType() == Material.COMMAND || 
			event.getBlock().getType() == Material.NOTE_BLOCK || 
			event.getBlock().getType() == Material.REDSTONE_LAMP_ON)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.item("Proximity Devices") + "."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void WebBreak(BlockDamageEvent event)
	{
		if (event.isCancelled())
			return;
	
		if (event.getBlock().getType() == Material.WEB)
			event.setInstaBreak(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void IronBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getBlock().getType() != Material.IRON_BLOCK)
			return;
		
		for (int x=-1 ; x<=1 ; x++)
			for (int z=-1 ; z<=1 ; z++)
				if (event.getBlock().getRelative(x, 1, z).getType() == Material.BEACON)
				{
					event.setCancelled(true);
					return;
				}			
	}

	@EventHandler
	public void LapisPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getBlock().getType() != Material.LAPIS_BLOCK)
			return;
		
		event.setCancelled(true);	
		
		UtilInv.remove(event.getPlayer(), Material.LAPIS_BLOCK, (byte)0, 1);
		
		final Block block = event.getBlock();
		
		_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
		{
			public void run()
			{
				block.setTypeId(9);
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 8);
				block.getWorld().playSound(block.getLocation(), Sound.SPLASH, 2f, 1f);
			}
		}, 0);
	}
	
	@EventHandler
	public void EnderChestBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getBlock().getType() != Material.ENDER_CHEST)
			return;
	
		event.setCancelled(true);
		
		event.getBlock().setTypeId(0);
		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.ENDER_CHEST));
	}

	@EventHandler
	public void IronDoor(PlayerInteractEvent event)
	{
		if (!Util().Event().isAction(event, ActionType.R_BLOCK))
			return;

		if (event.getClickedBlock().getTypeId() != 71)
			return;

		Block block = event.getClickedBlock();

		//Knock
		if (event.isCancelled())
		{
			if (!Recharge().use(event.getPlayer(), "Door Knock", 500, false))
				return;

			block.getWorld().playEffect(block.getLocation(), Effect.ZOMBIE_CHEW_WOODEN_DOOR, 0);
		}

		//Open
		else
		{
			if (block.getData() >= 8)
				block = block.getRelative(BlockFace.DOWN);

			if (block.getData() < 4)	block.setData((byte)(block.getData()+4), true);
			else						block.setData((byte)(block.getData()-4), true);

			//Effect
			block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
		}
	}

	@EventHandler
	public void BrewingDisable(PlayerInteractEvent event)
	{
		if (!Util().Event().isAction(event, ActionType.R_BLOCK))
			return;

		if (event.getClickedBlock().getTypeId() != 117)
			return;

		event.setCancelled(true);
	}
	
	@EventHandler
	public void BrewingBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getBlock().getType() != Material.BREWING_STAND)
			return;
	
		event.setCancelled(true);
		
		event.getBlock().setTypeId(0);
		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.BREWING_STAND));
	}
	
	

	@EventHandler
	public void EnchantDisable(PlayerInteractEvent event)
	{
		if (!Util().Event().isAction(event, ActionType.R_BLOCK))
			return;

		if (event.getClickedBlock().getType() != Material.ENCHANTMENT_TABLE)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void AnvilDisable(PlayerInteractEvent event)
	{
		if (!Util().Event().isAction(event, ActionType.R_BLOCK))
			return;

		if (event.getClickedBlock().getType() != Material.ANVIL)
			return;

		event.setCancelled(true);
	}

	/**
	@EventHandler
	public void ThrowTorch(PlayerInteractEvent event)
	{	
		if (!Util().Event().isAction(event, ActionType.L))
			return;

		Player player = event.getPlayer();

		if (player.getItemInHand() == null)
			return;

		if (player.getItemInHand().getType() != Material.REDSTONE_TORCH_ON)
			return;

		if (Clans().CUtil().getAccess(player, player.getLocation()) != ClanRelation.SELF)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot use " +
					F.skill("Throw Torch") +
					" in " + 
					Clans().CUtil().getOwnerStringRel(player.getLocation(), event.getPlayer().getName()) +
					"."));

			return;
		}

		if (player.getItemInHand().getAmount() > 1)
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		else
			player.setItemInHand(null);

		//Throw
		Item item = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.REDSTONE_TORCH_ON));
		UtilAction.velocity(item, event.getPlayer().getLocation().getDirection(), 0.6, false, 0, 0.2, 10, false);

		//Save
		_torch.add(item);

		event.setCancelled(true);
	}

	@EventHandler
	public void ThrowTNT(PlayerInteractEvent event)
	{	
		if (!Util().Event().isAction(event, ActionType.L))
			return;

		Player player = event.getPlayer();

		if (player.getItemInHand() == null)
			return;

		if (player.getItemInHand().getType() != Material.TNT)
			return;

		if (Clans().CUtil().getAccess(player, player.getLocation()) != ClanRelation.SELF)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot use " +
					F.skill("Throw TNT") +
					" in " + 
					Clans().CUtil().getOwnerStringRel(player.getLocation(), event.getPlayer().getName()) +
					"."));

			return;
		}

		if (player.getItemInHand().getAmount() > 1)
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		else
			player.setItemInHand(null);

		//Throw
		FallingBlock fall = event.getPlayer().getWorld().spawnFallingBlock(event.getPlayer().getEyeLocation(), 46, (byte)0);
		UtilAction.velocity(fall, event.getPlayer().getLocation().getDirection(), 0.3, false, 0, 0.2, 10, false);

		event.setCancelled(true);
	}

	@EventHandler
	public void TorchLight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Item item : _torch)
			for (Block block : UtilBlock.getInRadius(item.getLocation(), 1.5).keySet())
				if (block.getTypeId() == 46)
				{
					block.setTypeIdAndData(0, (byte)0, true);
					block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), TNTPrimed.class);	
				}
	}

	@EventHandler
	public void TorchExpire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		HashSet<Item> remove = new HashSet<Item>();

		for (Item cur : _torch)
			if (cur.isDead() || !cur.isValid())
				remove.add(cur);

		for (Item cur : remove)
		{
			_torch.remove(cur);
			cur.remove();
		}
	}
	 **/

	@EventHandler
	public void BonemealCancel(PlayerInteractEvent event)
	{	
		if (!Util().Event().isAction(event, ActionType.R))
			return;

		Player player = event.getPlayer();

		if (player.getItemInHand() == null)
			return;

		if (player.getItemInHand().getType() != Material.INK_SACK)
			return;

		if (player.getItemInHand().getData() == null)
			return;

		if (player.getItemInHand().getData().getData() != 15)
			return;

		event.setCancelled(true);
	}
	
	@EventHandler
	public void WildfireSpread(BlockBurnEvent event)
	{
		if (event.isCancelled())
			return;
		
		event.setCancelled(true);
		
		for (int x=-1 ; x<=1 ; x++)
			for (int y=-1 ; y<=1 ; y++)
				for (int z=-1 ; z<=1 ; z++)
				{
					//Self
					if (x == 0 && y == 0 && z == 0)
					{
						event.getBlock().setType(Material.FIRE);
						
						if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS)
							event.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIRT);
						
						return;
					}
					
					Block block = event.getBlock().getRelative(x, y, z);
					
					if (block.getRelative(BlockFace.DOWN).getType() == Material.GRASS)
						block.getRelative(BlockFace.DOWN).setType(Material.DIRT);
					
					//Surroundings
					if (!(
							(x == 0 && y == 0) ||
							(x == 0 && z == 0) ||
							(y == 0 && z == 0)
							))
						continue;

					if (block.getTypeId() == 0)
						block.setType(Material.FIRE);
				}
	}
	
	@EventHandler
	public void WildfireDirt(BlockIgniteEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS)
			event.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIRT);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void WildfireCancel(BlockIgniteEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getBlock().getBiome() == Biome.JUNGLE || event.getBlock().getBiome() == Biome.JUNGLE_HILLS)
			if (event.getCause() == IgniteCause.SPREAD)
				event.setCancelled(true);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void MoneyLossSteal(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.GetEvent().getEntity();
		
		int balance = Clients().Get(player).Game().GetEconomyBalance();
		
		int lose = (int) (0.04 * balance);
		
		//Balance
		Clients().Get(player).Game().SetEconomyBalance(balance - lose);

		CombatLog log = event.GetLog();
		if (log.GetKiller() != null)
		{
			//Inform
			UtilPlayer.message(UtilPlayer.searchExact(log.GetKiller().getName()), F.main("Death", "You stole " + F.count((lose) + " Coins") + " from " + F.name(player.getName()) + "."));
			
			//Inform
			UtilPlayer.message(player, F.main("Death", "You lost " + F.count((lose) + " Coins") + " to " + F.name(log.GetKiller().getName()) + "."));
		}
		else
		{
			//Inform
			UtilPlayer.message(player, F.main("Death", "You lost " + F.count((lose) + " Coins") + " for dying."));
		}
	}

	/*
	@EventHandler
	public void MaterialSwap(PlayerInteractEvent event)
	{	
		ItemStack stack = event.getPlayer().getItemInHand();

		if (stack == null)
			return;

		if (stack.getType() == Material.IRON_INGOT)
		{
			event.getPlayer().setItemInHand(ItemStackFactory.Instance.CreateStack(Material.GOLD_INGOT, stack.getAmount()));
			UtilPlayer.message(event.getPlayer(), F.main("Material", "You swapped " + F.elem("Iron") + " for " + F.elem("Gold") + "."));
		}

		else if (stack.getType() == Material.GOLD_INGOT)
		{
			event.getPlayer().setItemInHand(ItemStackFactory.Instance.CreateStack(Material.DIAMOND, stack.getAmount()));
			UtilPlayer.message(event.getPlayer(), F.main("Material", "You swapped " + F.elem("Gold") + " for " + F.elem("Diamond") + "."));
		}

		else if (stack.getType() == Material.DIAMOND)
		{
			event.getPlayer().setItemInHand(ItemStackFactory.Instance.CreateStack(Material.LEATHER, stack.getAmount()));
			UtilPlayer.message(event.getPlayer(), F.main("Material", "You swapped " + F.elem("Diamond") + " for " + F.elem("Leather") + "."));
		}

		else if (stack.getType() == Material.LEATHER)
		{
			event.getPlayer().setItemInHand(ItemStackFactory.Instance.CreateStack(Material.IRON_INGOT, stack.getAmount()));
			UtilPlayer.message(event.getPlayer(), F.main("Material", "You swapped " + F.elem("Leather") + " for " + F.elem("Iron") + "."));
		}
	}
	 */
	
	@EventHandler
	public void SpawnDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.FALL)
			return;

		if (!Clans().CUtil().isSpecial(event.GetDamageeEntity().getLocation(), "Spawn"))
			return;

		event.SetCancelled("Spawn Fall");
	}

	@EventHandler
	public void Repair(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
			return;

		if (event.getClickedBlock().getType() != Material.ANVIL)
			return;
		
		if (!Util().Event().isAction(event, ActionType.R_BLOCK))
			return;

		Player player = event.getPlayer();
		
		if (UtilMath.offset(player.getLocation(), event.getClickedBlock().getLocation()) > 2)
		{
			UtilPlayer.message(player, F.main("Repair", "You are too far from the " + F.item("Anvil") + "."));
			return;
		}

		if (player.getItemInHand() == null)
			return;

		ItemStack item = player.getItemInHand();
		
		if (item.getDurability() <= 0)
		{
			UtilPlayer.message(player, F.main("Repair", "Your " + F.item(item == null ? ChatColor.YELLOW + "Hand" : item.getItemMeta().getDisplayName()) + " does not need repairs."));
			return;
		}
		
		if (!Util().Gear().isRepairable(item))
		{
			UtilPlayer.message(player, F.main("Repair", "You cannot repair " + F.item(item.getItemMeta().getDisplayName()) + "."));
			return;
		}

		String 	creator = ItemStackFactory.Instance.GetLoreVar(item, "Owner");

		if (creator != null)
		{
			if (creator.length() > 2)
				creator = creator.substring(2, creator.length());
			
			if (!creator.equals(player.getName()))
			{
				UtilPlayer.message(player, F.main("Repair", "You cannot repair " + F.item(item.getItemMeta().getDisplayName()) + " by " + F.name(creator) + "."));	
				return;
			}
		}

		//Repair!
		UtilPlayer.message(player, F.main("Repair", "You repaired " + F.item(item.getItemMeta().getDisplayName()) + "."));
		item.setDurability((short)0);
		UtilInv.Update(player);

		//Break
		if (Math.random() > 0.85)
			event.getClickedBlock().setData((byte) (event.getClickedBlock().getData() + 4));
		
		if (event.getClickedBlock().getData() >= 12)
		{
			player.getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, 145);
			event.getClickedBlock().setTypeIdAndData(0, (byte)0, true);
		}

		//Record
		int repairs = 1 + ItemStackFactory.Instance.GetLoreVar(item, "Repaired", 0);

		ItemStackFactory.Instance.SetLoreVar(item, "Repaired", "" + repairs);
		
		//Effect
		player.playSound(player.getLocation(), Sound.ANVIL_USE, 1f, 1f);
	}
}
