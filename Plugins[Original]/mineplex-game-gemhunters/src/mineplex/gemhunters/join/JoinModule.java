package mineplex.gemhunters.join;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.UtilItem;
import mineplex.core.recharge.Recharge;
import mineplex.gemhunters.death.quitnpc.QuitNPC;
import mineplex.gemhunters.death.quitnpc.QuitNPCModule;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.loot.InventoryModule;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.persistence.PersistenceData;
import mineplex.gemhunters.persistence.PersistenceModule;
import mineplex.gemhunters.persistence.PersistenceRepository;
import mineplex.gemhunters.quest.QuestModule;
import mineplex.gemhunters.spawn.SpawnModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ReflectivelyCreateMiniPlugin
public class JoinModule extends MiniPlugin
{

	private static final double MAXIMUM_DURABILITY_LOSS = 0.85;
	private final CoreClientManager _client;
	private final EconomyModule _economy;
	private final LootModule _loot;
	private final QuestModule _quest;
	private final PersistenceModule _persistence;
	private final QuitNPCModule _npc;
	private final InventoryModule _inventory;
	private final SpawnModule _spawn;

	private JoinModule()
	{
		super("Join");

		_client = require(CoreClientManager.class);
		_economy = require(EconomyModule.class);
		_loot = require(LootModule.class);
		_quest = require(QuestModule.class);
		_persistence = require(PersistenceModule.class);
		_npc = require(QuitNPCModule.class);
		_inventory = require(InventoryModule.class);
		_spawn = require(SpawnModule.class);
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		CoreClient client = _client.Get(player);
		PersistenceRepository repository = _persistence.getRepository();
		Consumer<PersistenceData> response = data ->
			runSync(() -> {
				_economy.setStore(player, data.getGems());
				_quest.setPlayerData(player, data.getQuestData());
				player.teleport(data.getLocation());
				player.setHealth(data.getHealth());
				player.setMaxHealth(data.getMaxHealth());
				player.setFoodLevel(data.getHunger());
				loseDurability(data.getItems(), data.getSaveTime());
				for (ItemStack itemStack : data.getItems())
				{
					_loot.handleRewardItem(player, itemStack);
				}
				player.getInventory().addItem(data.getItems());
				loseDurability(data.getArmour(), data.getSaveTime());
				player.getInventory().setArmorContents(data.getArmour());
				_inventory.unlockSlots(player, data.getSlots(), false);
				Recharge.Instance.useForce(player, "Cash Out", data.getCashOutTime());
			});

		player.getInventory().clear();

		if (_npc.hasNPC(player))
		{
			QuitNPC npc = _npc.getNPC(player);
			npc.despawn(true);
		}

		_inventory.resetSlots(player);
		_spawn.teleportToSpawn(player);

		runAsync(() ->
		{
			repository.getPersistenceData(response, client);

			if (!repository.exists(client))
			{
				runSync(() -> _spawn.teleportToSpawn(player));
			}
		}, 40);
	}

	private void loseDurability(ItemStack[] items, long time)
	{
		long diff = System.currentTimeMillis() - time;
		long hours = TimeUnit.MILLISECONDS.toHours(diff);

		for (ItemStack item : items)
		{
			if (!UtilItem.isSword(item) && !UtilItem.isArmor(item))
			{
				continue;
			}

			short max = item.getType().getMaxDurability();
			short change = (short) (((double) max / 100D) * hours);
			short apply = (short) (item.getDurability() + change);

			if (apply > max * MAXIMUM_DURABILITY_LOSS)
			{
				apply = (short) (max * MAXIMUM_DURABILITY_LOSS);
			}

			item.setDurability(apply);
		}
	}
}
