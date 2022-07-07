package nautilus.game.arcade.game.games.minecraftleague.variation.wither.data;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSkeleton;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.minecraftleague.tracker.GrabSkullEvent;
import nautilus.game.arcade.game.games.minecraftleague.variation.wither.WitherVariation;

public class WitherMinionManager implements Listener
{
	private WitherVariation _host;
	private List<Location> _spawns;
	private long _lastDied;
	private int _lastUsed;
	private int _selected;

	private Skeleton _entity;

	private ItemStack _witherItem;

	private WitherSkeletonTimer _sbTimer;
	//private MapZone _skellyZone;

	private Block _cb;
	private Material _changed = Material.AIR;

	private boolean _witherBlocked = false;

	private boolean _spawned = false;

	public WitherMinionManager(WitherVariation host, List<Location> spawns)
	{
		_host = host;
		_spawns = spawns;
		_lastDied = System.currentTimeMillis();
		_lastUsed = -1;
		_witherItem = new ItemBuilder(Material.SKULL_ITEM).setTitle(C.cDRedB + "Wither Skeleton Head").setData((short) 1).setLore(C.cGray + "Bring this back", C.cGray + "to your team's Altar", C.cGray + "To summon a Wither!").build();
		_sbTimer = new WitherSkeletonTimer(host.Host.GetScoreboard());
		host.Host.ExtraSb.put(_sbTimer, host);
		//_skellyZone = new MapZone(spawns.get(0), new int[] {0, 0, 0});
		//_skellyZone.setValid(false);
		//host.Host.MapZones.add(_skellyZone);

		Bukkit.getPluginManager().registerEvents(this, host.Manager.getPlugin());
	}

	private void preSpawn()
	{
		Location chosen = null;
		while (chosen == null)
		{
			int check = new Random().nextInt(_spawns.size());
			if (check != _lastUsed)
			{
				chosen = _spawns.get(check);
				_lastUsed = check;
				_selected = check;
			}
		}

		_cb = chosen.clone().add(0, -2, 0).getBlock();
		_changed = _cb.getType();
		chosen.clone().add(0, -2, 0).getBlock().setType(Material.BEACON);
	}

	private void spawn(boolean respawn)
	{
		Location chosen = null;
		if (!respawn)
			chosen = _spawns.get(_selected);		
		else
			chosen = _spawns.get(_lastUsed);
		_host.Host.CreatureAllowOverride = true;
		_host.Host.CreatureAllowOverride = false;
		Skeleton e = UtilVariant.spawnWitherSkeleton(chosen);
		_entity = (Skeleton)e;
		UtilEnt.ghost(e, true, false);
		UtilEnt.vegetate(e);
		e.setCustomName(C.cRed + "Wither Skeleton");
		((Skeleton)e).setMaxHealth(/*100*/65);
		((Skeleton)e).setHealth(/*100*/65);
		((CraftSkeleton)e).getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		((CraftSkeleton)e).getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		//((Skeleton)e).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999999, 7));

		if (!respawn)
		{
			UtilTextMiddle.display("", C.cGray + "A Wither Skeleton has spawned!");
			//_skellyZone.setCenter(chosen);
			//_skellyZone.setValid(true);
			//_host.Host.Objective.setMainObjective(new GrabSkullObjective());
		}
		_selected = -1;
		_spawned = true;
	}

	public void onWitherDeath()
	{
		_lastDied = System.currentTimeMillis();
		_witherBlocked = false;
		_host.Host.getTowerManager().toggleAttack();
		_sbTimer.freezeTime(-1);
	}

	public void onWitherSpawn()
	{
		_host.Host.getTowerManager().toggleAttack();
		_witherBlocked = true;
		if (_entity != null)
		{
			_spawned = false;
			_entity.remove();
			_lastDied = System.currentTimeMillis();
			//_skellyZone.setValid(false);
			_entity = null;
		}
		if (_cb != null)
		{
			_cb.setType(_changed);
			_cb = null;
			_changed = Material.AIR;
		}
	}

	@EventHandler
	public void onLive(GameStateChangeEvent event)
	{
		if (event.GetGame() != _host.Host)
			return;
		if (event.GetState() != GameState.Live)
			return;

		_lastDied = System.currentTimeMillis() + UtilTime.convert(60, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		if (_entity == null || !_entity.isValid())
		{
			if (_spawned)
				spawn(true);
		}

		if (_entity != null)
		{
			_entity.teleport(_spawns.get(_lastUsed));
		}
		
		try {
		if (UtilTime.elapsed(_lastDied, UtilTime.convert(45, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
		{
			boolean noUse = _host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.RED)) < 1;
			if (_host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.AQUA)) >= 1)
				noUse = false;
			if (!_witherBlocked && !noUse)
			{
				if (_entity == null)
				{
					if (_selected != _lastUsed)
					{
						preSpawn();
					}
					long end = _lastDied + UtilTime.convert(1, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
					end =- System.currentTimeMillis();
					long secLeft = UtilTime.convert(end, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
					if (secLeft <= 3)
					{
						if (secLeft > 0)
						{
							UtilTextMiddle.display(secLeft + " Seconds", "Until Wither Skeleton Spawn");
						}
					}
				}
			}
		}

		if (UtilTime.elapsed(_lastDied, UtilTime.convert(90, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
		{
			boolean noUse = _host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.RED)) < 1;
			if (_host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.AQUA)) >= 1)
				noUse = false;
			if (!_witherBlocked && !noUse)
			{
				if (_entity == null)
					spawn(false);
			}
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (UtilInv.contains(player, Material.SKULL_ITEM, (byte) 1, 1))
			{
				UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0, 1, ViewDist.MAX);
				if (Recharge.Instance.usable(player, "FIREWORK_ALERT", false))
				{
					Recharge.Instance.use(player, "FIREWORK_ALERT", UtilTime.convert(2, TimeUnit.SECONDS, TimeUnit.MILLISECONDS), false, false);
					UtilFirework.playFirework(player.getEyeLocation().add(0, 3, 0), Type.BURST, _host.Host.GetTeam(player).GetColorBase(), false, true);
				}
			}
			else
			{
				/*if (_host.Host.Objective.getPlayerObjective(player) != null)
				{
					if (_host.Host.Objective.getPlayerObjective(player).equalsIgnoreCase("GRAB_SKULL"))
					{
						_host.Host.Objective.resetPlayerToMainObjective(player);
					}
				}*/
			}
			
			if (player.getInventory().getHelmet() != null)
			{
				if (UtilInv.IsItem(player.getInventory().getHelmet(), Material.SKULL_ITEM, (byte) 1))
				{
					player.getInventory().setHelmet(new ItemStack(Material.AIR));
					UtilInv.insert(player, _witherItem.clone());
				}
			}
		}
		} catch (Exception e)
		{
			
		}
	}

	@EventHandler
	public void handleTimer(UpdateEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		boolean noUse = _host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.RED)) < 1;
		if (_host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.AQUA)) >= 1)
			noUse = false;

		if (_entity != null || noUse)
		{
			_sbTimer.freezeTime(0);
			return;
		}

		if (_host.WitherSpawned || _witherBlocked)
		{
			_sbTimer.freezeTime(-2);
			return;
		}

		if (_entity == null)
			_sbTimer.freezeTime(-1);

		_sbTimer.setEnd((_lastDied + UtilTime.convert(90, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSkellyDie(EntityDeathEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		if (event.getEntity() instanceof Skeleton)
		{
			Skeleton skelly = (Skeleton)event.getEntity();
			if (skelly.getSkeletonType() == SkeletonType.WITHER)
			{
				if (_entity != null)
				{
					if (skelly.getEntityId() == _entity.getEntityId())
					{
						_spawned = false;
						_lastDied = System.currentTimeMillis();
						_entity = null;
						event.getDrops().clear();
						event.getDrops().add(_witherItem.clone());
						event.setDroppedExp(10);
						//_skellyZone.setValid(false);

						_cb.setType(_changed);
						_cb = null;
						_changed = Material.AIR;
						
						Bukkit.getScheduler().runTaskLater(_host.Manager.getPlugin(), new Runnable() {
							public void run()
							{
								_host.Host.getTowerManager().ironOreGen(null, false);
							}
						}, 20 * 10);

						//_host.Host.Objective.setMainObjective(new GearObjective());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPickup(PlayerPickupItemEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		if (!event.isCancelled())
		{
			String com = UtilItem.itemToStr(event.getItem().getItemStack());
			String com1 = UtilItem.itemToStr(_witherItem.clone());
			String[] compare = com.split(":");
			String[] compare1 = com1.split(":");
			String fin = compare[0] + ":" + compare[2] + ":" + compare[3];
			String fin1 = compare1[0] + ":" + compare1[2] + ":" + compare1[3];
			if (fin.equalsIgnoreCase(fin1))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You have the Wither Skull! Return the skull to your base and place it on your Altar to summon the Wither!"));
				UtilTextMiddle.display("", C.cGray + "You have picked up the Wither Skull!", event.getPlayer());
				Bukkit.getScheduler().runTaskLater(_host.Manager.getPlugin(), () -> UtilTextMiddle.display("", "Place the skull on top of", event.getPlayer()), 20 * 5);
				Bukkit.getScheduler().runTaskLater(_host.Manager.getPlugin(), () -> UtilTextMiddle.display("", "Your Altar's Soul Sand!", event.getPlayer()), 20 * 8);
				//_host.Host.Objective.setPlayerObjective(event.getPlayer(), new ReturnSkullObjective());
				if (!event.getItem().hasMetadata("Dropped"))
				{
                    event.getItem().setMetadata("Dropped", new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("Arcade"), ""));
					Bukkit.getPluginManager().callEvent(new GrabSkullEvent(event.getPlayer()));
					UtilTextMiddle.display("", _host.Host.GetTeam(event.getPlayer()).GetColor() + _host.Host.GetTeam(event.getPlayer()).getDisplayName() + " has picked up a Wither Skull!", 20, 20 * 5, 20);
				}
			}
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		if (!event.isCancelled())
		{
			String com = UtilItem.itemToStr(event.getItemDrop().getItemStack());
			String com1 = UtilItem.itemToStr(_witherItem.clone());
			String[] compare = com.split(":");
			String[] compare1 = com1.split(":");
			String fin = compare[0] + ":" + compare[2] + ":" + compare[3];
			String fin1 = compare1[0] + ":" + compare1[2] + ":" + compare1[3];
			if (fin.equalsIgnoreCase(fin1))
			{
				event.getItemDrop().setMetadata("Dropped", new FixedMetadataValue(_host.Manager.getPlugin(), true));
				//_host.Host.Objective.resetPlayerToMainObjective(event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		for (Location loc : _spawns)
		{
			//if (UtilShapes.getSphereBlocks(loc, 6, 6, false).contains(event.getBlock().getLocation()))
			if (UtilMath.offset(loc, event.getBlock().getLocation()) <= 6)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		for (Location loc : _spawns)
		{
			//if (UtilShapes.getSphereBlocks(loc, 6, 6, false).contains(event.getBlock().getLocation()))
			if (UtilMath.offset(loc, event.getBlock().getLocation()) <= 6)
				event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handlePlace(PlayerBucketEmptyEvent event)
	{
		if (!_host.Host.IsLive())
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		
		for (Location check : _spawns)
		{
			if (UtilMath.offset(check, block.getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handleBreak(PlayerBucketFillEvent event)
	{
		if (!_host.Host.IsLive())
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		
		for (Location check : _spawns)
		{
			if (UtilMath.offset(check, block.getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onExp(EntityExplodeEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		if (event.getEntity() instanceof Wither)
		{
			event.blockList().clear();
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntity(EntityChangeBlockEvent event)
	{
		if (!_host.Host.IsLive())
			return;

		for (Location loc : _spawns)
		{
			//if (UtilShapes.getSphereBlocks(loc, 5, 5, false).contains(event.getBlock().getLocation()))
			if (UtilMath.offset(loc, event.getBlock().getLocation()) <= 6)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void fixDamage(EntityDamageEvent event)
	{
		if (!_host.Host.IsLive())
			return;
		
		if (_entity == null)
			return;
		if (event.getEntity().getEntityId() != _entity.getEntityId())
			return;
		
		UtilAction.zeroVelocity(event.getEntity());
	}
}
