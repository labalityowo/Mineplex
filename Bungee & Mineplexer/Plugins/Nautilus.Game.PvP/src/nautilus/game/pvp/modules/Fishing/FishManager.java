package nautilus.game.pvp.modules.Fishing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.Rank;
import mineplex.core.itemstack.ItemStackFactory;
import me.chiss.Core.Module.AModule;
import me.chiss.Core.Scheduler.IScheduleListener;
import me.chiss.Core.Scheduler.Scheduler;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.util.TimeSpan;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.minecraft.account.event.AsyncClientLoadEvent;
import nautilus.game.pvp.repository.PvPRepository;
import nautilus.minecraft.core.webserver.token.Account.FishToken;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class FishManager extends AModule implements IScheduleListener
{
	public static FishManager Instance;
	
	private PvPRepository _repository;
	
	private HashSet<String> _debug = new HashSet<String>();

	private HashMap<String, HashMap<Fish, FishData>> _personal = new HashMap<String, HashMap<Fish, FishData>>();
	private HashMap<Fish, FishData> _allTimeHigh = new HashMap<Fish, FishData>();
	private HashMap<Fish, FishData> _day = new HashMap<Fish, FishData>();

	private ArrayList<Fish> _fishScale = new ArrayList<Fish>();
	private ArrayList<Age> _fishAge = new ArrayList<Age>();
	private ArrayList<Size> _fishSize = new ArrayList<Size>();	
	private ArrayList<Monster> _monsterList = new ArrayList<Monster>();
	private ArrayList<Loot> _lootList = new ArrayList<Loot>();
	
	public static void Initialize(JavaPlugin plugin, Scheduler scheduler, PvPRepository repository)
	{
		if (Instance == null)
			Instance = new FishManager(plugin, scheduler, repository);
	}
	
	protected FishManager(JavaPlugin plugin, Scheduler scheduler, PvPRepository repository)
	{
		super("Fishing", plugin);

		_repository = repository;
		
		for (FishToken token : GetRepository().GetFishingAllTimeHigh())
		{
			_allTimeHigh.put(Enum.valueOf(Fish.class, token.Name), new FishData(token.Size, token.Catcher));
		}
		
		for (FishToken token : GetRepository().GetFishingDayHigh())
		{
			_day.put(Enum.valueOf(Fish.class, token.Name), new FishData(token.Size, token.Catcher));
		}
		
		Scheduler.Initialize(plugin, repository);
		scheduler.ScheduleDailyRecurring(this, -5 * TimeSpan.HOUR);
	}

	@Override
	public PvPRepository GetRepository()
	{
		return _repository;
	}
	
	@Override
	public void enable() 
	{

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
		AddCommand("fish");
		AddCommand("fishing");
	}

	@Override
	public void command(Player caller, String cmd, String[] args)
	{
		if (args.length == 0)
		{
			// LOLWUT
		}
		else if (args[0].equals("ath"))
			DisplayScores(caller, _allTimeHigh, "All Time High", true);

		else if (args[0].equals("day"))
			DisplayScores(caller, _day, "Catch of the Day", true);

		else if (args[0].equals("pb"))
			DisplayScores(caller, _personal.get(caller.getName()), "Personal Best", false);
		
		else if (args[0].equals("debug"))
		{
			if (!Clients().Get(caller).Rank().Has(Rank.ADMIN, true))
				return;

			if (!_debug.remove(caller.getName()))
				_debug.add(caller.getName());

			caller.sendMessage("Fishing Debug: " + _debug.contains(caller.getName()));
		}

		else if (args[0].equals("test"))
		{
			if (!Clients().Get(caller).Rank().Has(Rank.ADMIN, true))
				return;

			int tests = 100000000;
			double totalSize = 0;
			for (int i=0 ; i<tests ; i++)
			{
				if (i%1000000 == 0)
					System.out.println(i + " Fish Catches Simulated");

				totalSize += CatchFish(caller, caller.getLocation(), true);
			}

			System.out.println("Average Weight from " + tests + " simulations: " + 
					UtilMath.trim(1, ((double)totalSize/(double)tests)));
		}
	}

	@EventHandler
	public void Interact(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand() == null)
			return;

		/*
		if (event.getPlayer().getItemInHand().getType() == Material.FISHING_ROD)
			if (Util().Event().isAction(event, ActionType.R))
				if (!Recharge().use(event.getPlayer(), "Cast Line", 2000, true))
				{
					event.setCancelled(true);
					return;
				}
		*/

		if (!_debug.contains(event.getPlayer().getName()))
			return;
		
		if (!Util().Event().isAction(event, ActionType.L))
			return;

		if (event.getPlayer().getItemInHand().getType() == Material.FLINT)
			CatchFish(event.getPlayer(), event.getPlayer().getTargetBlock(null, 0).getLocation().add(0, 2, 0), false);

		if (event.getPlayer().getItemInHand().getType() == Material.BOWL)
			CatchMonster(event.getPlayer(), event.getPlayer().getTargetBlock(null, 0).getLocation().add(0, 2, 0), false);

		if (event.getPlayer().getItemInHand().getType() == Material.BOAT)
			CatchItem(event.getPlayer(), event.getPlayer().getTargetBlock(null, 0).getLocation().add(0, 2, 0), false);

		event.setCancelled(true);

	}

	@EventHandler
	public void FishCatch(PlayerFishEvent event)
	{
		Player player = event.getPlayer();

		//Damage Caught Ent
		if (event.getState() == State.CAUGHT_ENTITY)
		{
			if (event.getCaught() instanceof Player)
			{
				Player ent = (Player)event.getCaught();
				
				if (Clans().CUtil().isSafe(ent))
				{
					event.setCancelled(true);
					return;
				}
				
				UtilAction.velocity(ent, UtilAlg.getTrajectory(ent, player), 1, false, 0, 0.6, 0.8, true);
			}

			return;
		}

		if (event.getState() == State.FAILED_ATTEMPT)
		{
			UtilPlayer.message(player, F.main(GetName(), "You failed to catch anything."));
			return;
		}

		else if (event.getState() == State.IN_GROUND)
		{
			UtilPlayer.message(player, F.main(GetName(), "You can only fish in water."));
			return;
		}

		else if (event.getState() == State.FISHING)
		{
			UtilPlayer.message(player, F.main(GetName(), "You cast out your fishing line..."));
			return;
		}

		if (event.getState() != State.CAUGHT_FISH)
			return;


		//No Water
		if (!WaterCheck(event.getCaught().getLocation()))
		{
			UtilPlayer.message(player, F.main(GetName(), "You can only fish in large bodies of water."));
			return;
		}
		else if (Clans().CUtil().isClaimed(event.getCaught().getLocation()) || Clans().CUtil().isClaimed(event.getPlayer().getLocation()))
		{
			UtilPlayer.message(player, F.main(GetName(), "You cannot fish inside claimed territory."));
			return;
		}


		if (Math.random() > 0.2)
			CatchFish(player, event.getCaught().getLocation(), false);

		else if (Math.random() > 0.3)
			CatchMonster(player, event.getCaught().getLocation(), false);

		else 
			CatchItem(player, event.getCaught().getLocation(), false);

		//Effect
		player.getWorld().playEffect(event.getCaught().getLocation(), Effect.STEP_SOUND, 8);
		player.getWorld().playSound(event.getCaught().getLocation(), Sound.SPLASH, 1f, 1f);

		event.setCancelled(true);
	}

	private Fish SelectFish()
	{
		if (!_fishScale.isEmpty())
			return _fishScale.get(UtilMath.r(_fishScale.size()));

		for (Fish fish : Fish.values())
			for (int i=0 ; i <fish.GetScale() ; i++)
				_fishScale.add(fish);

		return _fishScale.get(UtilMath.r(_fishScale.size()));
	}

	private Age SelectAge()
	{
		if (!_fishAge.isEmpty())
			return _fishAge.get(UtilMath.r(_fishAge.size()));

		for (Age age : Age.values())
			for (int i=0 ; i <age.GetScale() ; i++)
				_fishAge.add(age);

		return _fishAge.get(UtilMath.r(_fishAge.size()));
	}

	private Size SelectSize()
	{
		if (!_fishSize.isEmpty())
			return _fishSize.get(UtilMath.r(_fishSize.size()));

		for (Size size : Size.values())
			for (int i=0 ; i <size.GetScale() ; i++)
				_fishSize.add(size);

		return _fishSize.get(UtilMath.r(_fishSize.size()));
	}

	private double CatchFish(Player player, Location loc, boolean simulation) 
	{
		//Fish Selection
		Fish fish = SelectFish();
		Age age = SelectAge();
		Size size = SelectSize();

		//Base
		double weight = 1 + ((Math.random() + 0.5) * fish.GetMult() * age.GetMult() * size.GetMult());

		if (simulation)
			return weight;

		//Create Item
		double weightCopy = weight;
		while (weightCopy > 0)
		{
			ItemStack fishStack = ItemStackFactory.Instance.CreateStack(Material.RAW_FISH, (byte)0, Math.min((int)weightCopy, 64), 
					fish.GetRarity().GetColor() + fish.toString(), new String[] 
							{
				C.cGray + "Age: " + age.GetRarity().GetColor() + age.toString(),
				C.cGray + "Size: " + size.GetRarity().GetColor() + size.toString(),
				C.cGray + "Weight: " + C.cGreen + UtilMath.trim(1, weight) + " Pounds"
							}, player.getName() + " Fishing");
			Item item = player.getWorld().dropItem(loc, fishStack);

			//Launch
			double mult = 0.5 + (0.6 * (UtilMath.offset(player.getLocation(), item.getLocation())/16d));
			item.setVelocity(player.getLocation().toVector().subtract(item.getLocation().toVector()).normalize().add(new Vector(0, 0.4, 0)).multiply(mult));

			weightCopy -= 64;
		}

		//Inform
		UtilPlayer.message(player, F.main(GetName(), 
				"You caught a " + 
						ChatColor.GREEN + UtilMath.trim(1, weight) + " Pound " +
						size.GetRarity().GetColor() + size.toString() + " " +
						age.GetRarity().GetColor() + age.toString() + " " +
						fish.GetRarity().GetColor() + fish.toString() + 
						C.cGray + "."));

		//Record
		RecordCatchFish(player, fish, age, size, weight);

		//Item
		if (player.getItemInHand() != null)
		{
			if (player.getItemInHand().getType() == Material.FISHING_ROD)
			{
				//Weight
				int pounds = (int)weight + ItemStackFactory.Instance.GetLoreVar(player.getItemInHand(), "Fish Caught", 0);
				ItemStackFactory.Instance.SetLoreVar(player.getItemInHand(), "Fish Caught", "" + pounds);

				//Best
				String caught = "" + ChatColor.GREEN + UtilMath.trim(1, weight) + " Pound " +
						size.GetRarity().GetColor() + size.toString() + " " +
						age.GetRarity().GetColor() + age.toString() + " " +
						fish.GetRarity().GetColor() + fish.toString();
				try
				{
					String prevStr = ItemStackFactory.Instance.GetLoreVar(player.getItemInHand(), "Best Catch").split(" ")[0];
					double prev = Double.parseDouble(prevStr.substring(2, prevStr.length()));

					if (weight > prev)
						ItemStackFactory.Instance.SetLoreVar(player.getItemInHand(), "Best Catch", caught);
				}
				catch (Exception e)
				{
					ItemStackFactory.Instance.SetLoreVar(player.getItemInHand(), "Best Catch", caught);
				}
			}
		}


		return weight;
	}

	private void RecordCatchFish(Player player, Fish fish, Age age, Size size, double weight) 
	{
		//ATH
		boolean ath = true;
		if (!_allTimeHigh.containsKey(fish))
			_allTimeHigh.put(fish, new FishData(weight, player.getName()));
		else if (_allTimeHigh.get(fish).GetPounds() < weight)
			_allTimeHigh.put(fish, new FishData(weight, player.getName()));
		else
			ath = false;

		if (ath)
		{
			UtilServer.broadcast("§b§lNew Fishing All Time High -------------------");	

			UtilServer.broadcast(F.name(player.getName()) + " caught a " + 
					ChatColor.GREEN + UtilMath.trim(1, weight) + " Pound " +
					size.GetRarity().GetColor() + size.toString() + " " +
					age.GetRarity().GetColor() + age.toString() + " " +
					fish.GetRarity().GetColor() + fish.toString() + 
					C.cGray + ".");

			GetRepository().SaveFishingAllTimeHigh(fish, new FishData(weight, player.getName()));
		}

		//Day
		boolean day = true;
		if (!_day.containsKey(fish))
			_day.put(fish, new FishData(weight, player.getName()));
		else if (_day.get(fish).GetPounds() < weight)
			_day.put(fish, new FishData(weight, player.getName()));
		else
			day = false;

		if (day)
		{
			if (!ath)
			{
				UtilPlayer.message(player, F.main(GetName(), 
						"This is the best " + fish.GetRarity().GetColor() + fish.toString() + C.cGray + " of the day!"));
			}


			GetRepository().SaveFishingDayHigh(fish, new FishData(weight, player.getName()));
		}

		//Personal
		if (!_personal.containsKey(player.getName()))
			_personal.put(player.getName(), new HashMap<Fish, FishData>());

		boolean personalBest = true;
		if (!_personal.get(player.getName()).containsKey(fish))
			_personal.get(player.getName()).put(fish, new FishData(weight, player.getName()));
		else if (_personal.get(player.getName()).get(fish).GetPounds() < weight)
			_personal.get(player.getName()).put(fish, new FishData(weight, player.getName()));
		else
			personalBest = false;

		if (personalBest)
		{
			if (!ath)
				UtilPlayer.message(player, F.main(GetName(), 
						"This is your personal best " + fish.GetRarity().GetColor() + fish.toString() + C.cGray + " catch!"));

			GetRepository().SaveFishingScores(fish, new FishData(weight, player.getName()));
		}
	}
	
	private Monster SelectMonster()
	{
		if (!_monsterList.isEmpty())
			return _monsterList.get(UtilMath.r(_monsterList.size()));

		for (Monster monster : Monster.values())
			for (int i=0 ; i <monster.GetScale() ; i++)
				_monsterList.add(monster);

		return _monsterList.get(UtilMath.r(_monsterList.size()));
	}

	private Monster CatchMonster(Player player, Location loc, boolean simulation) 
	{
		Monster monster = SelectMonster();

		if (simulation)
			return monster;

		//Create Monster
		Entity ent = Creature().SpawnEntity(loc, monster.GetType());

		//Launch
		double mult = 0.7 + (0.7 * (UtilMath.offset(player.getLocation(), ent.getLocation())/16d));
		ent.setVelocity(player.getLocation().toVector().subtract(ent.getLocation().toVector()).normalize().add(new Vector(0, 0.4, 0)).multiply(mult));

		//Inform
		UtilPlayer.message(player, F.main(GetName(), 
				"You caught a " + 
						monster.GetRarity().GetColor() + monster.toString() + 
						C.cGray + "."));

		if (ent instanceof org.bukkit.entity.Monster)
			((org.bukkit.entity.Monster)ent).setTarget(player);

		//Item
		if (player.getItemInHand() != null)
			if (player.getItemInHand().getType() == Material.FISHING_ROD)
			{
				//Weight
				int mobs = 1 + ItemStackFactory.Instance.GetLoreVar(player.getItemInHand(), "Monsters Caught", 0);
				ItemStackFactory.Instance.SetLoreVar(player.getItemInHand(), "Monsters Caught", "" + mobs);
			}

		return monster;
	}
	
	private Loot SelectLoot()
	{
		if (!_lootList.isEmpty())
			return _lootList.get(UtilMath.r(_lootList.size()));

		for (Loot loot : Loot.values())
			for (int i=0 ; i <loot.GetScale() ; i++)
				_lootList.add(loot);

		return _lootList.get(UtilMath.r(_lootList.size()));
	}

	private Loot CatchItem(Player player, Location loc, boolean simulation) 
	{
		Loot loot = SelectLoot();

		if (simulation)
			return loot;

		int amount = 1 + UtilMath.r(loot.GetAmount());

		//Create Loot
		ItemStack lootStack;
		if (loot.GetType().getMaxStackSize() == 1)
			lootStack = ItemStackFactory.Instance.CreateStack(loot.GetType(), (byte)0, amount, 
					null, new String[] {}, player.getName() + " Fishing");
		else
			lootStack = ItemStackFactory.Instance.CreateStack(loot.GetType(), (byte)0, amount);

		Item item = player.getWorld().dropItem(loc, lootStack);

		//Launch
		double mult = 0.5 + (0.6 * (UtilMath.offset(player.getLocation(), item.getLocation())/16d));
		item.setVelocity(player.getLocation().toVector().subtract(item.getLocation().toVector()).normalize().add(new Vector(0, 0.4, 0)).multiply(mult));

		String amountStr = "";
		if (amount > 1)
			amountStr += amount + " ";

		//Inform
		UtilPlayer.message(player, F.main(GetName(), 
				"You caught " + 
						loot.GetRarity().GetColor() + amountStr + ChatColor.stripColor(lootStack.getItemMeta().getDisplayName()) + 
						C.cGray + "."));

		//Item
		if (player.getItemInHand() != null)
			if (player.getItemInHand().getType() == Material.FISHING_ROD)
			{
				//Weight
				int loots = 1 + ItemStackFactory.Instance.GetLoreVar(player.getItemInHand(), "Loot Caught", 0);
				ItemStackFactory.Instance.SetLoreVar(player.getItemInHand(), "Loot Caught", "" + loots);
			}

		return loot;
	}

	public boolean WaterCheck(Location loc)
	{
		int waterCount = 0;
		for (Block cur : UtilBlock.getInRadius(loc.getBlock().getLocation(), 4d).keySet())
		{
			int type = cur.getTypeId();
			if (type == 8 || type == 9)
			{
				waterCount++;
			}
		}

		if (waterCount > 64) 
			return true;

		return false;
	}

	public void DisplayScores(Player caller, HashMap<Fish, FishData> scores, String listName, boolean catcher)
	{
		UtilPlayer.message(caller, F.main(GetName(), "Displaying " + listName + ";"));

		for (Fish fish : Fish.values())
		{
			if (scores == null || !scores.containsKey(fish))
				UtilPlayer.message(caller, 
						C.cYellow + fish.toString() + " " + 
								C.cRed + "None");

			else if (!catcher)
				UtilPlayer.message(caller, 
						C.cYellow + fish.toString() + " " + 
								C.cGreen + UtilMath.trim(1, scores.get(fish).GetPounds()) + " Pounds");

			else
				UtilPlayer.message(caller, 
						C.cYellow + fish.toString() + " " + 
								C.cGreen + UtilMath.trim(1, scores.get(fish).GetPounds()) + " Pounds " +
								C.cGold + scores.get(fish).GetCatcher());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void AsyncLogin(AsyncClientLoadEvent event)
	{
		HashMap<Fish, FishData> personal = new HashMap<Fish, FishData>();
		
		for (FishToken token : event.GetClientToken().FishTokens)
		{
			personal.put(Fish.valueOf(token.Name), new FishData(token.Size, event.GetClientToken().Name));
		}
		
		_personal.put(event.GetClientToken().Name, personal);
	}

	@EventHandler
	public void UnloadPersonal(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		_personal.remove(player.getName());
	}

	@Override
	public void AppointmentFire()
	{
		UtilServer.broadcast("§b§lBest Catches of the Day @ " + UtilTime.date());	

		double bestWeight = 0;
		String best = C.cRed + "None";

		for (Fish fish : Fish.values())
		{
			if (_day == null || !_day.containsKey(fish))
			{
				UtilServer.broadcast(C.cYellow + fish.toString() + " " + C.cRed + "None");
			}

			else
			{
				String out = C.cYellow + fish.toString() + " " + 
						C.cGreen + UtilMath.trim(1, _day.get(fish).GetPounds()) + " Pounds " +
						C.cGold + _day.get(fish).GetCatcher();

				if (_day.get(fish).GetPounds() > bestWeight)
				{
					bestWeight = _day.get(fish).GetPounds();
					best = out;
				}

				UtilServer.broadcast(out);// + C.cDGreen + " 40,000 Coins");
			}			
		}

		if (!best.contains("None"))
		{
			UtilServer.broadcast("§b§lThe Best");	
			UtilServer.broadcast(best);// + C.cDGreen + " 100,000 Coins");
		}

		_day.clear();
		
		GetRepository().ClearDailyFishingScores();
	}
}
