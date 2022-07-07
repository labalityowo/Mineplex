package mineplex.mavericks.review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.worldgen.WorldGenCleanRoom;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.mavericks.MavericksApprovedRepository;
import mineplex.core.mavericks.MavericksBuildRepository;
import mineplex.core.mavericks.MavericksBuildWrapper;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeFormat;
/**
 * The Mavericks Review Manager. Handles the review process.
 */
public class MavericksReviewManager extends MiniPlugin
{
	public static final Vector OFFSET_VECTOR = new Vector(8, -8, 0);
	
	private MavericksBuildRepository _repoBuilds;
	private MavericksApprovedRepository _repoApprove;
	
	private Map<Player, ReviewData> _reviewers = new HashMap<>();
	
	private Location _spawn;
	private World _world;
	
	private List<Player> _processing = new ArrayList<>();
	
	private List<MavericksBuildWrapper> _reviewQueue = new ArrayList<>();
	
	private final boolean DEBUG = false;
	
	private ItemStack _itemEnterReviewMode;
	private ItemStack _itemExitReviewMode;
	private ItemStack _itemNext;
	private ItemStack _itemPrevious;
	private ItemStack _itemApprove;
	private ItemStack _itemDeny;
	private ItemStack _itemFilter;

	public MavericksReviewManager(JavaPlugin plugin, MavericksBuildRepository repoBuilds, MavericksApprovedRepository repoApprove)
	{
		super("MavericksReviewManager", plugin);
		
		_repoBuilds = repoBuilds;
		_repoApprove = repoApprove;
		
		_world = Bukkit.getWorlds().get(0);
		_spawn = _world.getSpawnLocation().getBlock().getLocation();
		
		//Apply Clean Room Generator to default world
		((CraftWorld)_world).getPopulators().addAll(new WorldGenCleanRoom().getDefaultPopulators(_world));
		_world.setGameRuleValue("keepInventory", "true");
		
		
		_itemEnterReviewMode = ItemStackFactory.Instance.CreateStack(Material.PAPER, (byte) 0, 1, C.cGreen + C.Bold + "Enter Review Mode");
		_itemExitReviewMode  = ItemStackFactory.Instance.CreateStack(Material.COMPASS, (byte) 0, 1, C.cRed + C.Bold + "Exit Review Mode");
		_itemNext 			 = ItemStackFactory.Instance.CreateStack(Material.ARROW, (byte) 0, 1, C.cGold + C.Bold + "Next Build");
		_itemPrevious 		 = ItemStackFactory.Instance.CreateStack(Material.ARROW, (byte) 0, 1, C.cGold + C.Bold + "Previous Build");
		_itemApprove 		 = ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, (byte) 5, 1, C.cGreen + C.Bold + "Approve");
		_itemDeny 			 = ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, (byte) 14, 1, C.cRed + C.Bold + "Deny");
		_itemFilter 		 = ItemStackFactory.Instance.CreateStack(Material.HOPPER, (byte) 0, 1, C.cGold + C.Bold + "Filter Settings");
	}
	

	private boolean isActionItem(ItemStack item)
	{
		if(_itemApprove.equals(item)) return true;
		if(_itemDeny.equals(item)) return true;
		if(_itemEnterReviewMode.equals(item)) return true;
		if(_itemExitReviewMode.equals(item)) return true;
		if(_itemFilter.equals(item)) return true;
		if(_itemNext.equals(item)) return true;
		if(_itemPrevious.equals(item)) return true;
		
		return false;
	}
	
	@EventHandler
	public void onGameMode(PlayerGameModeChangeEvent event)
	{
		boolean flying = event.getPlayer().isFlying();
		new BukkitRunnable()
		{
			public void run()
			{
				event.getPlayer().setAllowFlight(true);
				event.getPlayer().setFlying(flying);
			}
		}.runTask(UtilServer.getPlugin());
	}
	
	@EventHandler
	public void onForm(BlockFormEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onSpread(BlockSpreadEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityBlockFormEvent(EntityBlockFormEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onFade(BlockFadeEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onGrow(BlockGrowEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBurn(BlockBurnEvent event)
	{
			event.setCancelled(true);
	}

	@EventHandler
	public void ignite(EntityCombustEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onRedstone(BlockRedstoneEvent event)
	{
			event.setNewCurrent(event.getOldCurrent());
	}
	
	@EventHandler
	public void onDecay(LeavesDecayEvent event)
	{
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onIgnite(BlockIgniteEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPhysics(BlockPhysicsEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPickup(PlayerPickupItemEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onVehicleCollide(VehicleEntityCollisionEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event)
	{
		if(event.getCause() == DamageCause.VOID) return;
		if(event.getCause() == DamageCause.CUSTOM) return;
		if(event.getCause() == DamageCause.SUICIDE) return;
		
		if(event.getCause() == DamageCause.FIRE_TICK && event.getEntity() instanceof Player)
		{
			event.getEntity().setFireTicks(0);
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{	
		event.getPlayer().getInventory().removeItem(_itemApprove, _itemDeny, _itemExitReviewMode, _itemFilter, _itemNext, _itemPrevious);
		
		event.getPlayer().getInventory().setItem(0, _itemEnterReviewMode);
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
		event.getPlayer().setAllowFlight(true);
		event.getPlayer().setFlying(UtilEnt.isGrounded(event.getPlayer()));
	}
	
	@EventHandler
	public void onHunger(FoodLevelChangeEvent event)
	{
		event.setFoodLevel(20);
	}
	
	@EventHandler
	public void onWeather(WeatherChangeEvent event)
	{
		if(event.getWorld().getName().equals(_spawn.getWorld().getName()))
		{
			if(event.toWeatherState()) event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(isActionItem(event.getCurrentItem()) || isActionItem(event.getCursor())) event.setCancelled(true);
	}
	
	@EventHandler
	public void onCreativeInventoryClick(InventoryCreativeEvent event)
	{
		if(isActionItem(event.getCurrentItem()) || isActionItem(event.getCursor())) event.setCancelled(true);
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if(isActionItem(event.getItem()))
		{
			event.setCancelled(true);
		}
		else
		{
			return;
		}
		
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		String name = event.getItem().getItemMeta().getDisplayName();
		
		if(!Recharge.Instance.use(player, "MavericksReviewManager Action - " + name, 500, false, false)) return;
		
		if(_processing.contains(player))
		{
			player.sendMessage(F.main(getName(), "Your action is still processing. Please wait..."));
			player.playSound(player.getLocation(), Sound.NOTE_PIANO, 0.5f, 1);
			return;
		}
		
		_processing.add(player);
		
		if(event.getItem().equals(_itemEnterReviewMode))
		{
			if(isInReviewMode(player))
			{
				player.sendMessage(F.main(getName(), "You are already in " + F.item("review mode")));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				return;
			}
			
			ReviewData data = new ReviewData(player, getAvalibleLocation());
			
			for(int i = 0; i < 9; i++)
			{
				player.getInventory().setItem(i, null);
			}
			
			ItemStack back = _itemPrevious.clone();
			UtilInv.addDullEnchantment(back);
			player.getInventory().setItem(0, back);
			player.getInventory().setItem(1, _itemNext);
			player.getInventory().setItem(3, _itemApprove);
			player.getInventory().setItem(4, _itemDeny);
			//player.getInventory().setItem(6, _itemFilter);
			player.getInventory().setItem(8, _itemExitReviewMode);
			
			_reviewers.put(player, data);
			pasteBuild(player, true);
		}
		else if(event.getItem().equals(_itemExitReviewMode))
		{
			if(!isInReviewMode(player))
			{
				player.sendMessage(F.main(getName(), "You are not currently in " + F.item("review mode")));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				return;
			}
			
			for(int i = 0; i < 9; i++)
			{
				player.getInventory().setItem(i, null);
			}
			player.getInventory().setItem(0, _itemEnterReviewMode);
			
			_reviewers.remove(player);
			player.teleport(_spawn);
			
			_processing.remove(player);
		}
		
		else if(!isInReviewMode(player))
		{
			player.sendMessage(F.main(getName(), "Invalid action!"));
			player.sendMessage(F.main(getName(), "You are not currently in " + F.item("review mode")));
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
			_processing.remove(player);
			return;
		}
		else if(event.getItem().equals(_itemApprove))
		{	
			ReviewData reviewData = _reviewers.get(player);
			MavericksBuildWrapper data = reviewData.getData();
			if(data == null)
			{
				player.sendMessage(F.main(getName(), "Invalid action!"));
				player.sendMessage(F.main(getName(), "You are currently not reviewing a build."));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				_processing.remove(player);
				return;
			}
			
			if(data.isReviewed())
			{
				player.sendMessage(F.main(getName(), "This build has already been reviewed!"));
				player.sendMessage(F.main(getName(), "Contact the tech wizards if you want to change the"));
				player.sendMessage(F.main(getName(), "state of the already reviewed build."));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				_processing.remove(player);
				return;
			}
			
			_repoApprove.add(data, player.getUniqueId()).thenCompose(BukkitFuture.accept((success) ->
			{
				if(success)
				{
					player.sendMessage(F.main(getName(), "Marked build as " + F.color("approved", C.cGreen) + "."));
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0);
					
					
					_repoBuilds.setReviewed(_reviewers.get(player).getData().getBuildId(), true).thenCompose(BukkitFuture.accept((success2) ->
					{
						if(success2)
						{
							data.setReviewed(true);
							player.sendMessage(F.main(getName(), "Marked build as " + F.item("processed") + "."));
							player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0);
							player.sendMessage(F.main(getName(), "Loading next build..."));
							clear(reviewData);
							pasteBuild(player, true);
						}
						else
						{
							player.sendMessage(F.main(getName(), "Unable to mark the build as " + F.item("processed") + "!"));
							player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
						}
						_processing.remove(player);
					}));
					
				}
				else
				{
					player.sendMessage(F.main(getName(), "Unable to mark the build as " + F.color("approved", C.cGreen) + "!"));
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
					_processing.remove(player);
				}
			}));
		}
		else if(event.getItem().equals(_itemDeny))
		{
			MavericksBuildWrapper data = _reviewers.get(player).getData();
			if(data == null)
			{
				player.sendMessage(F.main(getName(), "Invalid action!"));
				player.sendMessage(F.main(getName(), "You are currently not reviewing a build."));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				_processing.remove(player);
				return;
			}
			
			if(data.isReviewed())
			{
				player.sendMessage(F.main(getName(), "This build has already been"));
				player.sendMessage(F.main(getName(), "reviewed! Contact the tech wizards"));
				player.sendMessage(F.main(getName(), "if you want to change the state of"));
				player.sendMessage(F.main(getName(), "the already reviewed build."));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				_processing.remove(player);
				return;
			}
			
			_repoBuilds.setReviewed(_reviewers.get(player).getData().getBuildId(), true).thenCompose(BukkitFuture.accept((success) ->
			{
				if(success)
				{
					data.setReviewed(true);
					player.sendMessage(F.main(getName(), "Marked build as " + F.color("denied", C.cRed) + "."));
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0);
					player.sendMessage(F.main(getName(), "Loading next build..."));
					clear(_reviewers.get(player));
					pasteBuild(player, true);
				}
				else
				{
					player.sendMessage(F.main(getName(), "Unable to mark the build as " + F.color("denied", C.cRed) + "!"));
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				}
				_processing.remove(player);
			}));
		}
		else if(event.getItem().equals(_itemFilter))
		{
			player.sendMessage(F.main(getName(), "Not yet implemented."));
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
			_processing.remove(player);
		}
		else if(event.getItem().equals(_itemNext))
		{
			pasteBuild(player, true);
		}
		else if(event.getItem().equals(_itemPrevious))
		{
			pasteBuild(player, false);
		}
	}
	
	public void pasteBuild(Player player, boolean next)
	{
		ReviewData review = _reviewers.get(player);
		if(review == null)
		{
			player.sendMessage(F.main(getName(), "Invalid action!"));
			player.sendMessage(F.main(getName(), "You are not currently in " + F.item("review mode")));
			_processing.remove(player);
			return;
		}
		
		MavericksBuildWrapper data = null;
		if(next)
		{
			data = review.getNext();
		}
		else
		{
			data = review.getPrevious();
		}
		if(!next && data == null)
		{
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0.5f);
			player.sendMessage(F.main(getName(), "No previous builds available"));
			ItemStack back = player.getInventory().getItem(0).clone();
			UtilInv.addDullEnchantment(back);
			player.getInventory().setItem(0, back);
			_processing.remove(player);
			return;
		}
		if(next && data == null)
		{
			player.sendMessage(F.main(getName(), "Pulling new data, please wait..."));
			getNext((pulledData) ->
			{
				if(pulledData == null)
				{
					player.sendMessage(F.main(getName(), "No new data was found."));
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
					_processing.remove(player);
				}
				else
				{
					if(DEBUG) Bukkit.broadcastMessage("Displaying from DB -2");
					try {
						player.getInventory().setItem(0, _itemPrevious);
						display(player, review, pulledData);
					} catch(Exception e)
					{
						e.printStackTrace();
						_processing.remove(player);
					}
				}
			});
		}
		if(data != null)
		{
			if(DEBUG) Bukkit.broadcastMessage("Displaying fast");
			display(player, review, data);
			player.getInventory().setItem(0, _itemPrevious);
		}
	}
	
	private void clear(ReviewData review)
	{
		if(review == null) return;
		
		UtilBlock.startQuickRecording();
		//Clear old blocks for stuff like lava and so on.
		for(Block b : UtilBlock.getInBoundingBox(review.getAreaMin(), review.getAreaMax()))
		{
			UtilBlock.setQuick(_world, b.getX(), b.getY(), b.getZ(), 0, (byte) 0); 
		}
		Location a = review.getAreaMin();
		Location b = review.getAreaMax();
		b.setY(a.getY());
		// Floor
		for(Block block : UtilBlock.getInBoundingBox(a, b, false))
		{
			UtilBlock.setQuick(_world, block.getX(), block.getY(), block.getZ(), 95, (byte) 1);
		}
		// Walls/edge
		for(Block block : UtilBlock.getInBoundingBox(a, b, false, true, true, false))
		{
			UtilBlock.setQuick(_world, block.getX(), block.getY()+1, block.getZ(), 95, (byte) 0);
		}
		
		UtilBlock.stopQuickRecording();
		List<Entity> list = review.getEntitiesInArea();
		for(Entity e : list)
		{
			e.remove();
		}
		if(DEBUG) Bukkit.broadcastMessage("Cleared " + list.size() + " entities in area");
	}
	
	private void display(Player player, ReviewData review, MavericksBuildWrapper data)
	{
		Location loc = review.getLoc();
		Location paste = review.getAreaMin().add(1, 1, 1);
		loc.setDirection(OFFSET_VECTOR.clone().add(new Vector(0, 4, 0)));
		
		clear(review);
		
		if(DEBUG) Bukkit.broadcastMessage("Trying to parse from " + data.getDateStamp());
		if(DEBUG) Bukkit.broadcastMessage("SBytes: " + data.getSchematicBytes().length + ", Schematic: " + data.getSchematic());
		Schematic schematic = data.getSchematic();
		
		SchematicData pasteData = schematic.paste(paste, false, false);
		for(Entity e : pasteData.getEntities())
		{
			if(e instanceof Item)
			{
				//Don't despawn
				e.setTicksLived(32768);
			}
			else
			{
				UtilEnt.vegetate(e, true);
				UtilEnt.ghost(e, true, false);
			}
		}
		player.sendMessage(ArcadeFormat.Line);
		
		String key = C.cGreen + C.Bold;
		String value = C.cYellow + C.Bold;
		SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss z");
		
		player.sendMessage(key + "BuildId: " + value + data.getBuildId());
		player.sendMessage(key + "Creator: " + value + data.getUUID());
		player.sendMessage(key + "Last C Name: " + value + (data.hasNameSet() ? data.getName() : C.Italics + "Not Avalible"));
		player.sendMessage(key + "Word: " + value + data.getTheme());
		player.sendMessage(key + "Place: " + value + (data.getPlace() + 1));
		player.sendMessage(key + "Points: " + value + data.getPoints());
		player.sendMessage(key + "Date: " + value + dformat.format(new Date(data.getDateStamp())));
		player.sendMessage(key + "Reviewed: " + value + data.isReviewed());
		
		player.sendMessage(ArcadeFormat.Line);
		
		player.setVelocity(new Vector(0,0,0));
		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(loc);
		
		review.setData(data);
		
		_processing.remove(player);
	}
	
	
	@EventHandler
	public void onUpdateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST) return;
		
		for(ReviewData data : _reviewers.values())
		{
			if(data.getData() == null) continue;
			
			if(!data.getData().hasParticles()) continue;
			
			for(Entry<Vector, ParticleType> e : data.getData().getParticles().entrySet())
			{
				Location loc = data.getAreaMin().add(1, 1, 1).add(e.getKey());
				
				ParticleType type = e.getValue();
				
				int amount = 8;
				
				if (type == ParticleType.HUGE_EXPLOSION ||
						type == ParticleType.LARGE_EXPLODE ||
						type == ParticleType.NOTE)
						amount = 1;
				
				UtilParticle.PlayParticleToAll(type, loc, 0.4f, 0.4f, 0.4f, 0, amount, ViewDist.LONG); 
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		ReviewData data = _reviewers.get(event.getPlayer());
		if(data != null)
		{
			for(Entity e : data.getEntitiesInArea()) e.remove();
			
			for(Block b : UtilBlock.getInBoundingBox(data.getAreaMin(), data.getAreaMax()))
			{
				UtilBlock.setQuick(_world, b.getX(), b.getY(), b.getZ(), 0, (byte) 0);
			}
		}
		exitReviewMode(event.getPlayer());
	}
	
	public boolean isInReviewMode(Player player)
	{
		return _reviewers.containsKey(player);
	}
	
	public void enterReviewMode(Player player)
	{
		if(isInReviewMode(player)) return;
	}
	
	public void exitReviewMode(Player player)
	{
		if(!isInReviewMode(player)) return;
		
		_reviewers.remove(player);
	}
	
	public Location getAvalibleLocation()
	{
		for(int i = 0; i < 500; i++)
		{
			Location loc = _spawn.clone().add(100*i, 0, 0);
			loc.setY(200);
			if(isTaken(loc)) continue;
			return loc;
		}
		return null;
	}
	
	private boolean isTaken(Location loc)
	{
		for(ReviewData data : _reviewers.values())
		{
			if(data.getData() == null) continue;
			if(data.getLoc().equals(loc)) return true;
		}
		return false;
	}
	
	private boolean isTaken(MavericksBuildWrapper wrapper)
	{
		if(wrapper.isReviewed()) return true;
		for(ReviewData data : _reviewers.values())
		{
			if(data.containsData(wrapper)) return true;
//			if(data.getData() == null) continue;
//			if(data.getData().equals(wrapper)) return true;
		}
		return false;
	}
	
	public void getNext(Consumer<MavericksBuildWrapper> consumer)
	{
		pullQueue(getNextIndex(), consumer);
	}
	
	public int getNextIndex()
	{
		for(int i = 0; i < _reviewQueue.size(); i++)
		{
			MavericksBuildWrapper wrapper = _reviewQueue.get(i);
			if(isTaken(wrapper)) continue;
			if(wrapper.isReviewed()) continue;
			if(wrapper.getSchematic() == null) continue;
			if(DEBUG) Bukkit.broadcastMessage("Found avalible index: " + i);
			return i;
		}
		if(DEBUG) Bukkit.broadcastMessage("Found no avalible indexes, returning next one: " + _reviewQueue.size());
		return _reviewQueue.size();
	}
	
	public void pullQueue(int index, Consumer<MavericksBuildWrapper> consumer)
	{
		if(_reviewQueue.size() > index)
		{
			consumer.accept(_reviewQueue.get(index));
			if(DEBUG) Bukkit.broadcastMessage("Local queue is bigger then index");
			return;
		}
		
		int offset = 0;
		for(MavericksBuildWrapper build : _reviewQueue)
		{
			if(!build.isReviewed()) offset++;
		}
		
		if(DEBUG) Bukkit.broadcastMessage("Pulling from DB, limit: " + (index-_reviewQueue.size()+1) + ", offset " + offset);
		
		_repoBuilds.getToReview(true, index-_reviewQueue.size()+1, offset).thenCompose(BukkitFuture.accept((list) ->
		{
			if(DEBUG) Bukkit.broadcastMessage("Retrived " + list.size() + " entries from DB");
			
			
			
			_reviewQueue.addAll(list);
			if(_reviewQueue.size() > index)
			{
				if(DEBUG) Bukkit.broadcastMessage("Found new entry to process!");
				
				consumer.accept(_reviewQueue.get(index));
			}
			else
			{
				if(DEBUG) Bukkit.broadcastMessage("Still not enough though");
				
				consumer.accept(null);
			}
		}));
	}
}