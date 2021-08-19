package mineplex.hub.modules;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import mineplex.core.Managers;
import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.donation.DonationManager;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramInteraction;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.commands.EggAddCommand;
import mineplex.hub.modules.EasterEggHunt.EasterEggHunter;
import mineplex.serverdata.database.DBPool;

public class EasterEggHunt extends MiniDbClientPlugin<EasterEggHunter>
{
	public enum Perm implements Permission
	{
		ADD_EGG_COMMAND,
	}

	private static final int SHARD_REWARD = 500;
	private static final String ITEM_REWARD = "Omega Chest";
	private static final int EGGS_PER_DAY = 30;
	
	private static final BlockFace[] BLOCK_FACES =
	{
		BlockFace.NORTH,
		BlockFace.EAST,
		BlockFace.SOUTH,
		BlockFace.WEST,
		BlockFace.NORTH_EAST,
		BlockFace.SOUTH_EAST,
		BlockFace.SOUTH_WEST,
		BlockFace.NORTH_WEST
	};
	
	private static final String[] EGG_SKINS =
	{
		"KingCrazy_",
		"Trajectories"
	};

	private final DonationManager _donationManager;
	private final InventoryManager _inventoryManager;
	private final List<EasterEgg> _possibleEggs;

	public EasterEggHunt(JavaPlugin plugin, CoreClientManager clientManager)
	{
		super("Egg Hunt", plugin, clientManager);

		_donationManager = Managers.get(DonationManager.class);
		_inventoryManager = Managers.get(InventoryManager.class);
		
		_possibleEggs = new ArrayList<>();
		runAsync(() ->
		{
			final List<EasterEgg> fetch = new ArrayList<>();
			loadEggs(fetch);
			runSync(() ->
			{
				fetch.stream().peek(EasterEgg::setup).forEach(_possibleEggs::add);
			});
		});
		
		addCommand(new EggAddCommand(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.ADD_EGG_COMMAND, true, true);
	}
	
	private String vecToStr(Vector vec)
	{
		return vec.getX() + "," + vec.getY() + "," + vec.getZ();
	}
	
	private Vector strToVec(String str)
	{
		String[] coords = str.split(",");
		double x = Double.parseDouble(coords[0]);
		double y = Double.parseDouble(coords[1]);
		double z = Double.parseDouble(coords[2]);
		
		return new Vector(x, y, z);
	}
	
	private void loadEggs(List<EasterEgg> eggs)
	{
		try (Connection c = DBPool.getAccount().getConnection())
		{
			ResultSet rs = c.prepareStatement("SELECT * FROM easterEggs;").executeQuery();
			while (rs.next())
			{
				EasterEgg egg = new EasterEgg(rs.getInt("id"), strToVec(rs.getString("eggLocation")), rs.getDate("eggDate"));
				eggs.add(egg);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void addEggToInventory(int accountId, int eggId)
	{
		runAsync(() ->
		{
			try (Connection c = DBPool.getAccount().getConnection())
			{
				PreparedStatement ps = c.prepareStatement("INSERT INTO accountEggs (accountId, eggId) VALUES (?, ?);");
				ps.setInt(1, accountId);
				ps.setInt(2, eggId);
				ps.execute();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	@EventHandler
	public void interactBlock(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
		{
			return;
		}

		Player player = event.getPlayer();

		for (EasterEgg egg : _possibleEggs)
		{
			if (egg.isMe(event.getClickedBlock()))
			{
				findEgg(player, egg.getId());
				event.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler
	public void refreshEggs(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			LocalDate currentDate = LocalDate.now();
			
			_possibleEggs.stream().filter(EasterEgg::isSpawned).filter(egg ->
			{
				LocalDate eggDate = egg.getDate().toLocalDate();
				
				return eggDate.getDayOfYear() != currentDate.getDayOfYear();
			}).forEach(egg ->
			{
				egg.despawn();
				GetValues().forEach(hunter -> hunter.getEggs().remove(egg.getId()));
			});
			_possibleEggs.stream().filter(egg -> !egg.isSpawned()).filter(egg ->
			{
				LocalDate eggDate = egg.getDate().toLocalDate();
				
				return eggDate.getDayOfYear() == currentDate.getDayOfYear();
			}).forEach(EasterEgg::spawn);
		}
	}
	
	public void addEgg(Player player, String date)
	{
		final Date parsed = Date.valueOf(date);
		final Vector loc = player.getLocation().toVector();
		runAsync(() ->
		{
			try (Connection c = DBPool.getAccount().getConnection())
			{
				PreparedStatement ps = c.prepareStatement("INSERT INTO easterEggs (eggLocation, eggDate) VALUES (?, ?);");
				ps.setString(1, vecToStr(loc));
				ps.setDate(2, parsed);
				ps.execute();
				
				PreparedStatement ret = c.prepareStatement("SELECT COUNT(id) FROM easterEggs WHERE eggDate=?;");
				ret.setDate(1, parsed);
				ResultSet rs = ret.executeQuery();
				if (rs.next())
				{
					UtilPlayer.message(player, F.main(getName(), "There are " + rs.getInt(1) + " eggs saved for " + date + "!"));
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	private void findEgg(Player player, Integer eggId)
	{
		if (Get(player).getEggs().contains(eggId))
		{
			UtilPlayer.message(player, F.main(getName(), "You have already found this egg! There are " + F.count(EGGS_PER_DAY - Get(player).getEggs().size()) + " more eggs to find today!"));
			return;
		}
		player.playSound(player.getLocation(), Sound.CAT_MEOW, 1, 0.7F);
		Get(player).findEgg(eggId);
		UtilPlayer.message(player, F.main(getName(), "Found " + F.count(Get(player).getEggs().size()) + "/" + F.count(EGGS_PER_DAY) + " Easter Eggs +" + F.currency(GlobalCurrency.TREASURE_SHARD, SHARD_REWARD) + "."));
		_donationManager.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, player, "", SHARD_REWARD);
		addEggToInventory(ClientManager.getAccountId(player), eggId);
		
		if (Get(player).getEggs().size() == 1)
		{
			UtilPlayer.message(player, F.main(getName(), "There are " + F.count(EGGS_PER_DAY) + " hidden " + F.elem("Easter Eggs") + " to find through the lobby each day."));
			UtilPlayer.message(player, F.main(getName(), "Each one is worth " + F.currency(GlobalCurrency.TREASURE_SHARD, SHARD_REWARD) + "."));
			UtilPlayer.message(player, F.main(getName(), "If you find all " + F.count(EGGS_PER_DAY) + " you will receive an " + C.cAqua + "Omega Chest for that day!"));
		}
		else if (Get(player).getEggs().size() == EGGS_PER_DAY)
		{
			UtilPlayer.message(player, F.main(getName(), "You have found all the eggs available today!"));

			_inventoryManager.addItemToInventory(success ->
			{
				if (success)
				{
					UtilPlayer.message(player, F.main(getName(), "+1 " + C.cAqua + ITEM_REWARD + C.mBody + "!"));
				}
				else
				{
					UtilPlayer.message(player, F.main(getName(), "Oh no! An error occurred while trying to give you your chest! Go find a staff member ASAP!"));
				}
			}, player, ITEM_REWARD, 1);
		}
	}
	
	public static class EasterEggHunter
	{
		private List<Integer> _found;
		
		public EasterEggHunter()
		{
			this(new ArrayList<>());
		}
		
		public EasterEggHunter(List<Integer> foundEggs)
		{
			_found = foundEggs;
		}
		
		public List<Integer> getEggs()
		{
			return _found;
		}
		
		public void findEgg(Integer id)
		{
			_found.add(id);
		}
	}
	
	public static class EasterEgg
	{
		private final Integer _id;
		private final Vector _loc;
		private final Date _date;
		private boolean _spawned;
		private Location _spawn;
		private Hologram _holo;
		
		public EasterEgg(Integer id, Vector loc, Date date)
		{
			_id = id;
			_loc = loc;
			_date = date;
			_spawned = false;
		}
		
		public Integer getId()
		{
			return _id;
		}
		
		public Date getDate()
		{
			return _date;
		}
		
		public boolean isSpawned()
		{
			return _spawned;
		}
		
		public boolean isMe(Block block)
		{
			return isSpawned() && UtilWorld.locToStr(block.getLocation()).equals(UtilWorld.locToStr(_spawn.getBlock().getLocation()));
		}
		
		public void setup()
		{
			_spawn = _loc.toLocation(Managers.get(HubManager.class).GetSpawn().getWorld());
			_spawned = false;
			_holo = new Hologram(Managers.get(HubManager.class).getHologram(), _spawn.clone().add(0, 1.5, 0), C.cDPurple + C.Scramble + "ABC " + C.cPurpleB + "Easter Egg Hunt" + C.cDPurple + C.Scramble + " ABC");
			_holo.setViewDistance(4);
			_holo.setInteraction(new HologramInteraction()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					Managers.get(EasterEggHunt.class).findEgg(player, getId());
				}
			});
			_holo.stop();
		}
		
		@SuppressWarnings("deprecation")
		public void spawn()
		{
			if (isSpawned())
			{
				return;
			}
			_spawn.getBlock().setType(Material.SKULL);
			_spawn.getBlock().setData((byte) 1);
			Skull skull = (Skull) _spawn.getBlock().getState();
			skull.setSkullType(SkullType.PLAYER);
			skull.setOwner(EGG_SKINS[UtilMath.r(EGG_SKINS.length)]);
			skull.setRotation(BLOCK_FACES[UtilMath.r(BLOCK_FACES.length)]);
			skull.update();
			_holo.start();
			_spawned = true;
		}
		
		public void despawn()
		{
			if (isSpawned())
			{
				_spawn.getBlock().setType(Material.AIR);
				_spawned = false;
				_holo.stop();
			}
		}
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT ae.eggId, ee.eggDate FROM accountEggs AS ae INNER JOIN easterEggs AS ee ON ae.eggId=ee.id WHERE ae.accountId=" + accountId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		List<Integer> found = new ArrayList<>();
		while (resultSet.next())
		{
			if (resultSet.getDate("eggDate").toLocalDate().getDayOfYear() == LocalDate.now().getDayOfYear())
			{
				found.add(resultSet.getInt("eggId"));
			}
		}
		
		Set(uuid, new EasterEggHunter(found));
	}

	@Override
	protected EasterEggHunter addPlayer(UUID uuid)
	{
		return new EasterEggHunter();
	}
}