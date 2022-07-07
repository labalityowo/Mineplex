package mineplex.gemhunters.quest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilItem.ItemAttribute;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Menu;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.StatsManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.quest.command.ResetQuestsCommand;
import mineplex.gemhunters.quest.types.ChestOpenerQuest;
import mineplex.gemhunters.quest.types.EnjoyTheViewQuest;
import mineplex.gemhunters.quest.types.GiveItemQuest;
import mineplex.gemhunters.quest.types.KillMostValuableQuest;
import mineplex.gemhunters.quest.types.KillPlayerQuest;
import mineplex.gemhunters.quest.types.LocationQuest;
import mineplex.gemhunters.quest.types.SamitoDQuest;
import mineplex.gemhunters.quest.types.SpecificChestOpenerQuest;
import mineplex.gemhunters.quest.types.WalkingQuest;
import mineplex.gemhunters.world.WorldDataModule;

@ReflectivelyCreateMiniPlugin
public class QuestModule extends MiniClientPlugin<QuestPlayerData>
{
	public enum Perm implements Permission
	{
		RESET_QUESTS_COMMAND,
	}

	private static final int MAX_QUESTS = 5;
	private static final long RESET_QUESTS_TIME = TimeUnit.MINUTES.toMillis(15);
	private static final Material MATERIAL = Material.PAPER;
	private static final String ITEM_METADATA = "quest";
	
	// -1 for the reward dictates that the quest's reward will be handled by the subclass
	private final Quest[] _quests =
	{ 
		new ChestOpenerQuest(0, "Chest Opener", 100, 250, 5), 
		new ChestOpenerQuest(1, "Grand Chest Opener", 200, 500, 20), 
		new ChestOpenerQuest(2, "Superior Chest Opener", 500, 750, 40),

		new SamitoDQuest(3, "Give to the Homeless", "Donate " + F.count("10") + " gems to the Hobo.", 100, 300, 10),
		
		new KillPlayerQuest(4, "Mercenary", 50, -1, 5, "Mythical Chest"),
		new KillPlayerQuest(5, "Warrior", 100, -1, 10, "Illuminated Chest"),
		new KillPlayerQuest(6, "Slayer", 250, -1, 25, "Omega Chest"),
		//new KillPlayerQuest(7, "Ruthless", 1000, -1, 50, "Rank Upgrade"),
		
		new LocationQuest(8, "Vision Quest", "Climb the tallest mountain.", 100, 300, "TALL_MOUNTAIN"),
		new LocationQuest(9, "Emergency Medi-Vac", "Get to the helicopter on the roof of the hospital.", 50, 150, "HOSPITAL_HELI"),
		
		new SpecificChestOpenerQuest(10, "Treasure Hunter", "Open " + F.count("1") + " Legendary Chest.", 100, 5000, "PURPLE", 1),
		new SpecificChestOpenerQuest(11, "Resupply", "Open a Supply Drop.", 100, 500, "RED", 1),
		
		new EnjoyTheViewQuest(12, 25, 100),
		
		new GiveItemQuest(13, "Waiter", "5 apples", 300, 750, new ItemStack(Material.APPLE), 5),
		new GiveItemQuest(14, "The Golden Apple", "a Golden Apple", 200, 3000, new ItemStack(Material.GOLDEN_APPLE), 1),
		
		new WalkingQuest(15, "Going for a walk", "Go on a journey of " + F.count("1000") + " blocks.", 100, -1, 1000, Material.GOLD_BARDING),
		new WalkingQuest(16, "Going on a journey", "Go on a journey of " + F.count("5000") + " blocks.", 200, -1, 5000, Material.DIAMOND_BARDING),
		
		new KillMostValuableQuest(17, "Equality", "Slay the most valuable player in the game.", 100, 1500),
		
		//new CraftingQuest(18, "Light em up", "Craft " + F.count("5 Torches"), 25, 250, Material.TORCH, 5)
	};

	private final EconomyModule _economy;
	private final StatsManager _stats;
	private final WorldDataModule _worldData;
	
	private QuestModule()
	{
		super("Quest");

		_economy = require(EconomyModule.class);
		_stats = require(StatsManager.class);
		_worldData = require(WorldDataModule.class);
		
		Menu<?> menu = new QuestUI(this);

		runSyncLater(() ->
		{
			for (Location location : _worldData.getCustomLocation("QUEST_NPC"))
			{			
				new QuestNPC(this, location, menu);
			}
		}, 20);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.RESET_QUESTS_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new ResetQuestsCommand(this));
	}
	
	@Override
	protected QuestPlayerData addPlayer(UUID uuid)
	{
		return new QuestPlayerData();
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		updateQuests(event.getPlayer());
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			QuestPlayerData playerData = Get(player);

			if (!UtilTime.elapsed(playerData.getLastClear(), RESET_QUESTS_TIME))
			{
				continue;
			}

			if (playerData.getLastClear() != 0)
			{
				player.sendMessage(F.main(C.cYellowB + "Quest Master", "I have " + F.count(String.valueOf(MAX_QUESTS)) + " new quests for you! Come and see me to start them!"));
			}
			
			playerData.clear();
			updateQuests(player);
		}
	}

	@EventHandler
	public void pickupItem(PlayerPickupItemEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Item item = event.getItem();
		Player player = event.getPlayer();
		Quest quest = fromItemStack(event.getItem().getItemStack());

		if (quest == null)
		{
			return;
		}
		
		if (!item.hasMetadata(ITEM_METADATA))
		{
			return;
		}
		
		if (!Recharge.Instance.use(event.getPlayer(), "Quest Pickup " + quest.getId(), 2000, false, false))
		{
			event.setCancelled(true);
			return;
		}
		
		boolean able = startQuest(quest, player, false);
		
		if (!able)
		{
			event.setCancelled(true);
		}
		else
		{			
//			UUID owner = UUID.fromString(item.getMetadata(ITEM_METADATA).get(0).asString());
//			Player other = UtilPlayer.searchExact(owner);
		
			event.getItem().remove();
			event.setCancelled(true);
			/*
			 * Noting here that when a player leaves their quest progress is removed.
			 * However that means that if a new player picks up their quest item we
			 * run into a problem where that will be null. Thus the progress on that
			 * quest is lost.
			 * More complications are added when a player quits out and their NPC is
			 * there instead until they finally really really quit out.
			 * This is one massive headache in order to keep quests alive while not
			 * running into some serious memory leaks.
			 * Furthermore the time complications of this project mean that there isn't
			 * enough time right now to implement this (however a enough time for me
			 * to type this lengthy comment about it). So in true style I'm cutting
			 * corners and saying that if a player quits out then don't allow other 
			 * players to be able to pickup the quest.
			 */
		}
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();
		Quest quest = fromItemStack(event.getItemDrop().getItemStack());
		
		if (quest == null)
		{
			return;
		}
		
		cancelQuest(quest, player);
		handleDroppedQuest(event.getItemDrop(), player);
	}
	
	public void handleDroppedQuest(Item item, Player player)
	{
		item.setMetadata(ITEM_METADATA, new FixedMetadataValue(_plugin, player.getUniqueId().toString()));
	}
	
	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Get(event.getEntity()).clear(true);
	}
	
	public void updateQuests(Player player)
	{
		QuestPlayerData playerData = Get(player);
		List<Integer> quests = playerData.getPossibleQuests();

		for (int i = 0; i < MAX_QUESTS; i++)
		{
			Quest quest = getRandomQuest(playerData, player);

			if (quest == null)
			{
				player.sendMessage(F.main(_moduleName, "It seems that there was some trouble finding you a new quest. Please try again later."));
				return;
			}

			quests.add(quest.getId());
		}
	}

	public boolean startQuest(Quest quest, Player player, boolean applyCost)
	{
		if (isActive(quest, player))
		{
			player.sendMessage(F.main(_moduleName, "You have already accepted that quest."));
			return false;
		}
		else if (isComplete(quest, player))
		{
			player.sendMessage(F.main(_moduleName, "You have already completed that quest."));
			return false;
		}
		else if (!UtilInv.hasSpace(player, 1))
		{
			player.sendMessage(F.main(_moduleName, "You do not have enough space in your inventory to start this quest."));
			return false;
		}
		else if (applyCost && _economy.Get(player) < quest.getStartCost())
		{
			player.sendMessage(F.main(_moduleName, "You do not have enough gems to start this quest."));
			return false;
		}

		player.sendMessage(F.main(_moduleName, "Started " + F.name(quest.getName()) + "."));

		QuestPlayerData playerData = Get(player);
		playerData.getActiveQuests().add(quest.getId());

		updateQuestItem(quest, player);

		quest.onStart(player);
		
		if (applyCost)
		{
			_economy.removeFromStore(player, quest.getStartCost());
		}
		return true;
	}

	public void completeQuest(Quest quest, Player player)
	{
		if (!isActive(quest, player))
		{
			player.sendMessage(F.main(_moduleName, "This quest is not active for you."));
			return;
		}

		player.sendMessage(F.main(_moduleName, "Completed " + F.name(quest.getName()) + "."));

		_stats.incrementStat(player, "Gem Hunters.QuestsCompleted", 1);

		QuestPlayerData playerData = Get(player);
		playerData.getActiveQuests().remove(Integer.valueOf(quest.getId()));
		playerData.getCompletedQuests().add(quest.getId());

		updateQuestItem(quest, player);
	}

	public void cancelQuest(Quest quest, Player player)
	{
		if (!isActive(quest, player))
		{
			player.sendMessage(F.main(_moduleName, "This quest is not active for you."));
			return;
		}

		player.sendMessage(F.main(_moduleName, "Dropped " + F.name(quest.getName()) + "."));
		
		QuestPlayerData playerData = Get(player);
		playerData.getActiveQuests().remove(Integer.valueOf(quest.getId()));
	}
	
	public Quest getRandomQuest(QuestPlayerData playerData, Player player)
	{
		int attempts = 0;

		while (attempts < _quests.length * 2)
		{
			attempts++;

			int index = UtilMath.r(_quests.length);
			Quest quest = _quests[index];

			if (isActive(quest, player) || isPossible(quest, player))
			{
				continue;
			}

			return quest;
		}

		return null;
	}

	public ItemStack getItemStack(Quest quest, Player player, boolean npc, boolean hasSpace, boolean hasGems)
	{
		ItemBuilder builder = new ItemBuilder(MATERIAL);

		builder.setTitle(C.cGreen + quest.getName());
		builder.addLore(C.blankLine, quest.getDescription(), C.blankLine);

		boolean active = isActive(quest, player);
		boolean complete = isComplete(quest, player);

		if (npc)
		{
			if (active)
			{
				builder.setGlow(true);
				builder.addLore(C.cRed + "You have already started this quest!");
			}
			else if (complete)
			{
				builder.addLore(C.cRed + "You have already completed this quest!");
			}
			else if (!hasGems)
			{
				builder.addLore(C.cRed + "You do not have enough gems to start this quest!");
			}
			else if (hasSpace)
			{
				builder.addLore(C.cGreen + "Click to start this quest!");
			}
			else
			{
				builder.addLore(C.cRed + "You do not have enough space in your inventory!");
			}
		}
		else
		{
			String progress = C.mBody + "[" + C.cGreen + quest.get(player) + C.mBody + "/" + C.cGreen + quest.getGoal() + C.mBody + "]";
			
			builder.addLore(UtilTextMiddle.progress(quest.getProgress(player)) + C.mBody + " " + progress);
		}

		builder.addLore("", "Cost: " + F.currency(GlobalCurrency.GEM, quest.getStartCost()), "Reward: " + quest.getRewardString());
		
		return builder.build();
	}

	public Quest fromItemStack(ItemStack itemStack)
	{
		Material material = itemStack.getType();
		ItemMeta meta = itemStack.getItemMeta();

		if (material != MATERIAL || meta == null || !meta.hasLore())
		{
			return null;
		}

		String name = ChatColor.stripColor(meta.getDisplayName());

		for (Quest quest : _quests)
		{
			if (!quest.getName().equals(name))
			{
				continue;
			}

			return quest;
		}

		return null;
	}

	public void updateQuestItem(Quest quest, Player player)
	{
		ItemStack itemStack = getItemStack(quest, player, false, true, true);

		for (ItemStack items : player.getInventory().getContents())
		{
			if (UtilItem.isSimilar(itemStack, items, ItemAttribute.NAME, ItemAttribute.MATERIAL, ItemAttribute.DATA, ItemAttribute.AMOUNT))
			{
				player.getInventory().remove(items);
			}
		}

		if (isActive(quest, player))
		{
			player.getInventory().addItem(itemStack);
		}
	}

	public boolean isPossible(Quest quest, Player player)
	{
		return Get(player).getPossibleQuests().contains(quest.getId());
	}
	
	public boolean isActive(Quest quest, Player player)
	{
		return Get(player).getActiveQuests().contains(quest.getId());
	}

	public boolean isComplete(Quest quest, Player player)
	{
		return Get(player).getCompletedQuests().contains(quest.getId());
	}

	public Quest getFromId(int id)
	{
		for (Quest quest : _quests)
		{
			if (quest.getId() == id)
			{
				return quest;
			}
		}

		return null;
	}
	
	public void setPlayerData(Player player, QuestPlayerData playerData)
	{
		Set(player, playerData);
	}
	
	public boolean isQuestNPC(Entity entity)
	{
		return entity.hasMetadata("quest_npc");
	}
	
	@EventHandler
	public void debug(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().startsWith("/questdatatest"))
		{
			event.setCancelled(true);
			Player player = event.getPlayer();
			
			QuestPlayerData playerData = Get(player);
			
			for (int i : playerData.getPossibleQuests())
			{
				player.sendMessage("P: " + i);
			}
			
			for (int i : playerData.getActiveQuests())
			{
				player.sendMessage("A: " + i);
			}
			
			for (int i : playerData.getCompletedQuests())
			{
				player.sendMessage("C: " + i);
			}
		}
	}
}