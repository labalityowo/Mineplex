package nautilus.game.arcade.game.games.minestrike;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.server.v1_8_R3.EntityArrow;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticManager;
import mineplex.core.gadget.gadgets.gamemodifiers.minestrike.GameModifierMineStrikeSkin;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargedEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minestrike.data.Bomb;
import nautilus.game.arcade.game.games.minestrike.data.Bullet;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItem;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItemType;
import nautilus.game.arcade.game.games.minestrike.items.equipment.armor.Armor;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Grenade;
import nautilus.game.arcade.game.games.minestrike.items.guns.Gun;
import nautilus.game.arcade.game.games.minestrike.items.guns.GunType;

public class GunModule implements Listener
{
	public ArcadeManager Manager;
	private Game _host;

	public static class RoundOverEvent extends Event
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		private final Game _game;

		public RoundOverEvent(Game game)
		{
			_game = game;
		}

		public Game getGame()
		{
			return _game;
		}
	}

	
	public static float RECOIL = 0.8f;
	public static float CONE = 0.7f;
	public static float MOVE_PENALTY = 0.8f;
	
	public boolean Debug = false;
	public int BulletInstant = 2;	//0 = Slow, 1 = Instant, 2 = Mix
	public boolean CustomHitbox = true;
	public boolean BulletAlternate = false;

	public boolean EnableNormalArmor = false;
	public boolean BlockRegeneration = true;
	public boolean EnablePickup = true;
	public boolean EnableDrop = true;
	public boolean EnableCleaning = true;
	private boolean _disableDamage;
	
	private long _freezeTime = -1;
	
	private int _round;

	//Ongoing Data
	private HashMap<Gun, Player> _gunsEquipped = new HashMap<Gun, Player>();
	private HashMap<Grenade, Player> _grenadesEquipped = new HashMap<Grenade, Player>();

	private HashMap<Player, DisguisePlayer> _disguise = new HashMap<Player, DisguisePlayer>();

	//Round Data (wiped at end of each round)
	private HashMap<Entity, Gun> _gunsDropped = new HashMap<Entity, Gun>();
	private HashMap<Entity, Grenade> _grenadesDropped = new HashMap<Entity, Grenade>();

	private HashMap<Entity, Bullet> _bullets = new HashMap<Entity, Bullet>();
	private HashMap<Entity, Grenade> _grenadesThrown = new HashMap<Entity, Grenade>();

	private HashSet<Entity> _defusalDropped = new HashSet<Entity>();

	private HashMap<Location, Long> _incendiary = new HashMap<Location, Long>();
	private HashMap<Block, Long> _smokeBlocks = new HashMap<Block, Long>();

	private Bomb _bomb = null;
	private Item _bombItem = null;
	private Player _bombHolder = null;
	private long _bombHolderLastMove = 0;

	private HashMap<Player, ItemStack> _scoped = new HashMap<Player, ItemStack>();
	
	public GunModule(Game host)
	{
		_host = host;
		Manager = host.Manager;
		_round = 1;
		
		Manager.registerEvents(this);
	}
	
	@EventHandler
	public void unregisterSelf(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Dead)
			return;
		
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void playerDeath(PlayerDeathEvent event)
	{
		//Dont Get Hit By Bullets
		((CraftPlayer) event.getEntity()).getHandle().spectating = true;
		
		Manager.GetCondition().Factory().Blind("Ghost", event.getEntity(), event.getEntity(), 2.5, 0, false, false, false);
	}

//	public void disguiseSneak(Player player, GameTeam team)
//	{
//		//Create Disguise
//		if (!_disguise.containsKey(player))
//		{
//			_disguise.put(player, new DisguisePlayer(player, ((CraftPlayer)player).getProfile()));
//		}
//
//		DisguisePlayer disguise = _disguise.get(player);
//		disguise.setSneaking(true);
//		Manager.GetDisguise().disguise(_disguise.get(player));
//
//		for (Player other : UtilServer.getPlayers())
//		{
//			if (team.HasPlayer(other))
//			{
//				Manager.GetDisguise().removeViewerToDisguise(disguise, other);
//			}
//			else
//			{
//				Manager.GetDisguise().addViewerToDisguise(disguise, other, true);
//			}
//		}
//	}

	public void registerGun(Gun gun, Player player)
	{
		_gunsEquipped.put(gun, player);
	}

	public void deregisterGun(Gun gun)
	{
		_gunsEquipped.remove(gun);
	}

	public void registerGrenade(Grenade grenade, Player player)
	{
		_grenadesEquipped.put(grenade, player);
	}

	public void deregisterGrenade(Grenade grenade)
	{
		_grenadesEquipped.remove(grenade);
	}

	public void registerDroppedGun(Entity ent, Gun gun)
	{
		_gunsDropped.put(ent, gun);
	}

	public void deregisterDroppedGun(Gun gun)
	{
		Iterator<Entity> entIterator = _gunsDropped.keySet().iterator();

		while (entIterator.hasNext())
			if (gun.equals(_gunsDropped.get(entIterator.next())))
				entIterator.remove();
	}

	public void registerDroppedGrenade(Entity ent, Grenade grenade)
	{
		_grenadesDropped.put(ent, grenade);
	}

	public void deregisterDroppedGrenade(Grenade grenade)
	{
		Iterator<Entity> entIterator = _grenadesDropped.keySet().iterator();

		while (entIterator.hasNext())
			if (grenade.equals(_grenadesDropped.get(entIterator.next())))
				entIterator.remove();
	}

	public void registerBullet(Bullet bullet)
	{
		_bullets.put(bullet.Bullet, bullet);

		UtilEnt.ghost(bullet.Bullet, false, true);
	}

	public void registerThrownGrenade(Entity ent, Grenade grenade)
	{
		_grenadesThrown.put(ent, grenade);
	}

	public void registerIncendiary(Location loc, long endTime)
	{
		_incendiary.put(loc, endTime);
	}
	
	@SuppressWarnings("deprecation")
	public void registerSmokeBlock(Block block, long endTime)
	{
		if (block.getType() == Material.AIR || block.getType() == Material.PORTAL || block.getType() == Material.FIRE)
		{
			block.setTypeIdAndData(90, (byte)UtilMath.r(2), false);
			
			_smokeBlocks.put(block, endTime);
		}
	}

	public Gun getGunInHand(Player player, ItemStack overrideStack)
	{
		ItemStack stack = player.getItemInHand();
		if (overrideStack != null)
			stack = overrideStack;

		for (Gun gun : _gunsEquipped.keySet())
		{
			if (!_gunsEquipped.get(gun).equals(player))
				continue;

			if (!gun.isStack(stack))
				continue;

			return gun;
		}

		return null;
	}

	public Grenade getGrenadeInHand(Player player, ItemStack overrideStack)
	{
		ItemStack stack = player.getItemInHand();
		if (overrideStack != null)
			stack = overrideStack;

		for (Grenade grenade : _grenadesEquipped.keySet())
		{
			if (!_grenadesEquipped.get(grenade).equals(player))
				continue;

			if (!grenade.isStack(stack))
				continue;

			return grenade;
		}

		return null;
	}



	@EventHandler
	public void triggerShoot(PlayerInteractEvent event)
	{
		if (_freezeTime > 0)
			return;

		if (!_host.IsLive())
			return;

		if (!_host.IsAlive(event.getPlayer()))
			return;

		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		//Players get between 150 and 250. 200 is norm. For some reason it doesnt go outside 150-250 bounds.
		//Announce("Since Last: " + (System.currentTimeMillis() - last), false);
		//last = System.currentTimeMillis();

		Gun gun = getGunInHand(event.getPlayer(), null);
		if (gun == null)
			return;

		gun.shoot(event.getPlayer(), this);
		event.setCancelled(true);
	}

	@EventHandler
	public void triggerReload(PlayerInteractEvent event)
	{
		if (_freezeTime > 0)
			return;

		if (!_host.IsLive())
			return;

		if (!_host.IsAlive(event.getPlayer()))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		Gun gun = getGunInHand(event.getPlayer(), null);
		if (gun == null)
			return;

		gun.reload(event.getPlayer());
		event.setCancelled(true);
	}

	@EventHandler
	public void triggerGrenade(PlayerInteractEvent event)
	{
		if (_freezeTime > 0)
		{
			event.setCancelled(true);
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.L) && !UtilEvent.isAction(event, ActionType.R))
			return;

		if (!_host.IsLive())
			return;

		if (!_host.IsAlive(event.getPlayer()))
			return;

		Grenade grenade = getGrenadeInHand(event.getPlayer(), null);
	
		if (grenade == null)
			return;
		
		if (!UtilTime.elapsed(_host.GetStateTime(), 10000))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot throw Grenades yet."));
			return;
		}

		grenade.throwGrenade(event.getPlayer(), UtilEvent.isAction(event, ActionType.L), this);
		event.setCancelled(true);
	}

	@EventHandler
	public void triggerDrop(PlayerDropItemEvent event)
	{
		if(!EnableDrop)
			return;
		
		if (!_host.InProgress())
			return;

		//Without this, the event is cancelled in pre-game by managers
		//Which results in the item staying in your hand, even if i set to null here.
		event.setCancelled(false);

		//Guns
		Gun gun = getGunInHand(event.getPlayer(), event.getItemDrop().getItemStack());
		if (gun != null)
		{
			gun.drop(this, event.getPlayer(), false, false);
			event.getItemDrop().remove();
			event.getPlayer().setItemInHand(null);
			return;
		}

		//Grenades
		Grenade grenade = getGrenadeInHand(event.getPlayer(), event.getItemDrop().getItemStack());
		if (grenade != null)
		{
			grenade.drop(this, event.getPlayer(), false, false);
			event.getItemDrop().remove();
			event.getPlayer().setItemInHand(null);
			return;
		}

		//Bomb
		if (event.getItemDrop().getItemStack().getType() == Material.GOLD_SWORD)
		{
			_bombItem = event.getItemDrop();
			_bombHolder = null;

			//Radio
			playSound(Radio.T_BOMB_DROP, null, _host.GetTeam(event.getPlayer()));

			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void deathDrop(PlayerDeathEvent event)
	{
		_scoped.remove(event.getEntity());
		dropInventory(event.getEntity());
	}

	public void dropInventory(Player player)
	{
		if (!_host.InProgress())
			return;

		for (int i=0 ; i<9 ; i++)
		{
			ItemStack stack = player.getInventory().getItem(i);

			if (stack == null || stack.getType() == Material.AIR)
				continue;

			HashSet<StrikeItem> toDrop = new HashSet<StrikeItem>();

			//Guns
			for (StrikeItem item : _gunsEquipped.keySet())
			{
				if (!_gunsEquipped.get(item).equals(player))
					continue;

				toDrop.add(item);
			}

			//Grenades
			for (StrikeItem item : _grenadesEquipped.keySet())
			{
				if (!_grenadesEquipped.get(item).equals(player))
					continue;

				toDrop.add(item);
			}

			//Drop Primary
			boolean primaryDropped = false;
			for (StrikeItem item : toDrop)
			{
				if (item.getType() == StrikeItemType.PRIMARY_WEAPON)
				{
					item.drop(this, player, true, false);
					primaryDropped = true;
				}
			}

			//Drop Remaining
			boolean grenadeDropped = false;
			for (StrikeItem item : toDrop)
			{
				//Record Primary Dropped
				if (item.getType() == StrikeItemType.PRIMARY_WEAPON)
					continue;

				//Pistol
				if (item.getType() == StrikeItemType.SECONDARY_WEAPON)
				{
					item.drop(this, player, true, primaryDropped);
					continue;
				}

				//Grenade
				if (item.getType() == StrikeItemType.GRENADE)
				{
					item.drop(this, player, true, grenadeDropped);
					grenadeDropped = true;
					continue;
				}

				//Other
				item.drop(this, player, true, false);	
			}

			//Defuse Kit
			if (stack.getType() == Material.SHEARS)
			{
				_defusalDropped.add(player.getWorld().dropItemNaturally(player.getEyeLocation(), stack));
			}

			//Bomb
			if (stack.getType() == Material.GOLD_SWORD)
			{
				_bombItem = player.getWorld().dropItemNaturally(player.getEyeLocation(), stack);
				_bombItem.setPickupDelay(40);
				_bombHolder = null;

				//Radio
				playSound(Radio.T_BOMB_DROP, null, _host.GetTeam(ChatColor.RED));
			}
		}

		UtilInv.Clear(player);
	}

	@EventHandler
	public void triggerPickup(PlayerPickupItemEvent event)
	{
		if (!EnablePickup)
			return; 
		
		if (!_host.InProgress())
			return;

		if (event.getItem().getTicksLived() < 10)
			return;

		if (UtilMath.offset(event.getItem(), event.getPlayer()) > 1)
			return;

		if (!_host.IsAlive(event.getPlayer()))
			return;

		//Guns
		Gun gun = _gunsDropped.get(event.getItem());
		if (gun != null)
		{
			if (gun.pickup(this, event.getPlayer()))
				event.getItem().remove();
		}

		//Grenades
		Grenade grenade = _grenadesDropped.get(event.getItem());
		if (grenade != null)
		{
			if (grenade.pickup(this, event.getPlayer()))
				event.getItem().remove();
		}

		//Defusal
		if (UtilGear.isMat(event.getItem().getItemStack(), Material.SHEARS))
		{
			if (_host.GetTeam(event.getPlayer()).GetColor() == ChatColor.RED)
				return;

			if (UtilInv.contains(event.getPlayer(), Material.SHEARS, (byte)0, 1))
				return;

			event.getPlayer().getInventory().setItem(8, event.getItem().getItemStack());

			UtilPlayer.message(event.getPlayer(), F.main("Game", "You equipped Defusal Kit."));

			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.HORSE_ARMOR, 1.5f, 1f);

			event.getItem().remove();
		}

		//Bomb
		if (_bombItem != null && _bombItem.equals(event.getItem()))
		{
			giveBombToPlayer(event.getPlayer());
		}
	}

	@EventHandler
	public void reloadCancel(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Gun gun : _gunsEquipped.keySet())
		{
			gun.cancelReloadCheck(_gunsEquipped.get(gun), this);
		}
	}

	@EventHandler
	public void rechargeWeapons(RechargedEvent event)
	{
		for (Gun gun : _gunsEquipped.keySet())
		{
			if (_gunsEquipped.get(gun).equals(event.GetPlayer()))
			{
				gun.reloadEvent(event);
			}
		}
	}

	@EventHandler
	public void coneOfFireIncrease(PlayerMoveEvent event)
	{
		Gun gun = getGunInHand(event.getPlayer(), null);
		if (gun != null)
		{
			gun.moveEvent(event);
		}
	}

	@EventHandler
	public void coneOfFireReduction(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Gun gun : _gunsEquipped.keySet())
		{
			gun.reduceCone();
		}
	}

	@EventHandler
	public void slowBulletHit(final ProjectileHitEvent event)
	{
		if (!(event.getEntity() instanceof Snowball))
			return;

		Bullet bullet = _bullets.get(event.getEntity());

		//Particle
		if (bullet != null && bullet.Shooter != null)
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, event.getEntity().getLocation(), 0, 0, 0, 0, 1,
					ViewDist.MAX, bullet.Shooter);

		//Hit Block Sound
		event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENDERMAN_HIT, 1f, 1f);

		//Block Particle
		Location loc = event.getEntity().getLocation().add(event.getEntity().getVelocity().multiply(0.8));
		Block block = loc.getBlock();
		if (block.getType() == Material.AIR)
		{
			Block closest = null;
			double closestDist = 0;

			for (Block other : UtilBlock.getSurrounding(block, true))
			{
				if (other.getType() == Material.AIR)
					continue;

				double dist = UtilMath.offset(loc, other.getLocation().add(0.5, 0.5, 0.5));

				if (closest == null || dist < closestDist)
				{
					closest = other;
					closestDist = dist;
				}
			}

			if (closest != null)
				block = closest;
		}

		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		
		if (block.getType() == Material.STAINED_GLASS_PANE)
			block.setType(Material.AIR);
	}

	@EventHandler
	public void slowBulletWhizz(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Entity ent : _bullets.keySet())
		{
			if (ent instanceof Snowball)
				continue;

			if (ent.getTicksLived() < 10)
				continue;

			Bullet bullet = _bullets.get(ent);

			for (Player player : UtilServer.getPlayers())
			{
				if (UtilMath.offset(ent, player) < 4)
				{
					if (!bullet.WhizzSound.contains(player))
					{
						player.playSound(ent.getLocation(), Sound.BAT_IDLE, (float)(0.5 + Math.random() * 0.5), 1f);
						bullet.WhizzSound.add(player);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void instantBulletHit(final ProjectileHitEvent event)
	{
		if (!_host.IsLive())
			return;
		
		if (!(event.getEntity() instanceof Arrow))
			return;

		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				Arrow arrow = (Arrow)event.getEntity();

				Bullet bullet = _bullets.get(arrow);

				//Particle
				if (bullet != null && bullet.Shooter != null)
					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, arrow.getLocation(), 0, 0, 0, 0, 1,
							ViewDist.MAX, bullet.Shooter);

				//Hit Block Sound
				arrow.getWorld().playSound(arrow.getLocation(), Sound.ENDERMAN_HIT, 1f, 1f);

				//Bullet Whiz Sound
				instantBulletWhizz(arrow.getLocation(), bullet);

				//Tracer Particle
				Location loc = bullet.Origin.clone();
				while (UtilMath.offset(loc, arrow.getLocation()) > 2)
				{
					loc.add(UtilAlg.getTrajectory(loc, arrow.getLocation()).multiply(1));

					UtilParticle.PlayParticle(ParticleType.CRIT, loc, 0, 0, 0, 0, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
				}

				//Block Particle
				try
				{
					EntityArrow entityArrow = ((CraftArrow) arrow).getHandle();

					Field fieldX = EntityArrow.class.getDeclaredField("d");
					Field fieldY = EntityArrow.class.getDeclaredField("e");
					Field fieldZ = EntityArrow.class.getDeclaredField("f");

					fieldX.setAccessible(true);
					fieldY.setAccessible(true);
					fieldZ.setAccessible(true);

					int x = fieldX.getInt(entityArrow);
					int y = fieldY.getInt(entityArrow);
					int z = fieldZ.getInt(entityArrow);

					Block block = arrow.getWorld().getBlockAt(x, y, z);
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
					
					if (block.getType() == Material.STAINED_GLASS_PANE)
						block.setType(Material.AIR);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				_bullets.remove(arrow);
				arrow.remove();
			}	
		}, 0);
	}

	private void instantBulletWhizz(Location bulletEndLocation, Bullet bullet)
	{
		if (bullet == null)
			return;

		Location loc = bullet.Origin.clone();

		if (bullet.Shooter != null)
			bullet.WhizzSound.add(bullet.Shooter);

		//Move between points, check players
		while (UtilMath.offset(loc, bulletEndLocation) > 1)
		{
			//This is used to calculate whether players are between current/end.
			//Add 5 so you still hear the whizz if it hits just infront of you.
			double offsetStartToEnd = UtilMath.offset(loc, bulletEndLocation) + 4;

			for (Player player : UtilServer.getPlayers())
			{
				if (bullet.WhizzSound.contains(player))
					continue;

				//Remove players who are not between current/end points
				if (offsetStartToEnd < UtilMath.offset(player.getEyeLocation(), loc) && 
						offsetStartToEnd < UtilMath.offset(player.getEyeLocation(), bulletEndLocation))
				{
					bullet.WhizzSound.add(player);
					continue;
				}

				//Check
				if (UtilMath.offset(player.getEyeLocation(), loc) < 4)
				{
					player.playSound(loc, Sound.BAT_IDLE, (float)(0.5 + Math.random() * 0.5), 1f);
					bullet.WhizzSound.add(player);
				}
			}

			//Move Closer
			loc.add(UtilAlg.getTrajectory(loc, bulletEndLocation));
		}
	}




	@EventHandler(priority=EventPriority.MONITOR)
	public void removeArrowsFromPlayer(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() != null)
			((CraftPlayer) event.GetDamageePlayer()).getHandle().o(0);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (_disableDamage)
			return;
		
		if (event.GetDamageePlayer() == null)
			return;

		if (event.GetCause() == DamageCause.FALL)
		{
			event.AddMod(_host.GetName(), "Fall Reduction", -2, false);
			return;
		}

		//Knife
		if (event.GetProjectile() == null && event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			if (event.GetDamagerPlayer(false) != null)
			{
				Player damager = event.GetDamagerPlayer(false);
				
				GameCosmeticManager cosmeticManager = Manager.getCosmeticManager().getGadgetManager().getGameCosmeticManager();
				GameModifierMineStrikeSkin knifeSkin = (GameModifierMineStrikeSkin) cosmeticManager.getActiveCosmetic(damager, GameDisplay.MineStrike, "Knife");
				
				ItemStack inHand = damager.getItemInHand(); 
				//Cancel Non-Knife Melee
				if (!UtilGear.isMat(inHand, Material.IRON_AXE) && 
						!UtilGear.isMat(inHand, Material.IRON_SWORD) &&
						!(knifeSkin != null && UtilGear.isMatAndData(inHand, knifeSkin.getSkinMaterial(), knifeSkin.getSkinData())))
				{
					event.SetCancelled("Non-Knife");
				}
				//Knife Attack
				else if (!event.IsCancelled())
				{
					LivingEntity damagee = event.GetDamageeEntity();
					if (damagee == null)	return;

					Vector look = damagee.getLocation().getDirection();
					look.setY(0);
					look.normalize();

					Vector from = damager.getLocation().toVector().subtract(damagee.getLocation().toVector());
					from.setY(0);
					from.normalize();

					Vector check = new Vector(look.getX() * -1, 0, look.getZ() * -1);

					//Backstab
					if (check.subtract(from).length() < 0.8)
					{
						//Damage
						event.AddMod(damager.getName(), "Knife (Backstab)", 40 - event.GetDamage(), false);

						//Effect
						damagee.getWorld().playSound(damagee.getLocation(), Sound.IRONGOLEM_DEATH, 1f, 1f);	
					}
					//Standard
					else
					{
						//Damage
						event.AddMod(damager.getName(), "Knife", 6 - event.GetDamage(), false);

						event.GetDamageeEntity().getWorld().playSound(event.GetDamageeEntity().getLocation(), Sound.BAT_HURT, 1f, 1f);
					}
					
					event.SetKnockback(false);
					event.setMetadata("gunType", "KNIFE");
				}
			}

			return;
		}

		//Gun
		Bullet bullet = _bullets.remove(event.GetProjectile());
		if (bullet == null)
			return;

		//Get Hit Area
		int hitArea = 0;

		if (CustomHitbox)
		{
			if (event.GetProjectile() instanceof Arrow)
				hitArea = getArrowHitArea(event.GetDamageePlayer(), bullet.Origin.clone(), bullet.Direction.clone());
			else
				hitArea = getSnowballHitArea(event.GetDamageePlayer(), event.GetProjectile());
		}


		if (hitArea == -1)
		{
			event.SetCancelled("Miss");
			return;
		}

		//Bullet Whiz Sound
		if (event.GetProjectile() instanceof Arrow)
			instantBulletWhizz(event.GetDamageePlayer().getEyeLocation(), bullet);

		//Wipe previous data!
		event.GetCancellers().clear();
		event.GetDamageMod().clear();

		if (!Manager.canHurt(event.GetDamageePlayer(), event.GetDamagerPlayer(true)))
			event.SetCancelled("Team Damage");

		event.AddMod(_host.GetName(), "Negate Default", -event.GetDamageInitial(), false);	

		//Damage + Dropoff
		double damage = bullet.getDamage();
		double damageDropoff = bullet.getDamageDropoff(event.GetDamageeEntity().getLocation());

		//Add Damages
		event.AddMod(bullet.Shooter.getName(), bullet.Gun.getChatName(), damage, true);
		event.AddMod(bullet.Shooter.getName(), "Distance Dropoff", damageDropoff, false);

		//Headshot
		if (hitArea == 1)
		{
			Bukkit.getPluginManager().callEvent(new PlayerHeadshotEvent(bullet.Shooter, event.GetDamageePlayer()));

			event.AddMod(bullet.Shooter.getName(), "Headshot", damage, true);

			//Wearing Helmet
			if (Armor.isArmor(event.GetDamageePlayer().getInventory().getHelmet()) || 
					(_scoped.containsKey(event.GetDamageePlayer()) && UtilGear.isMat(_scoped.get(event.GetDamageePlayer()), Material.LEATHER_HELMET)))
			{
				event.AddMod(event.GetDamageePlayer().getName(), "Helmet", -damage*0.5, false);
				event.GetDamageePlayer().getWorld().playSound(event.GetDamageePlayer().getEyeLocation(), Sound.SPIDER_DEATH, 1f, 1f);
			}
			else
			{
				event.GetDamageePlayer().getWorld().playSound(event.GetDamageePlayer().getEyeLocation(), Sound.SLIME_ATTACK, 1f, 1f);
			}
		}

		//Kevlar - Body Hit
		if (hitArea == 0 && Armor.isArmor(event.GetDamageePlayer().getInventory().getChestplate()))
		{
			double damageArmor = -(1 - bullet.Gun.getArmorPenetration()) * (damage + damageDropoff);

			event.AddMod(event.GetDamageePlayer().getName(), "Kevlar", damageArmor, false);
		}
		//Mini-Stun
		else
		{
			UtilAction.zeroVelocity(event.GetDamageeEntity());
		}
		
		if (EnableNormalArmor)
		{
			
			double armomrDamage = 0;
			
			for (ItemStack item : event.GetDamageePlayer().getInventory().getArmorContents())
			{
				if (item == null)
					continue;
				
				if (item.getType().toString().contains("DIAMOND")) armomrDamage -= 1;
				if (item.getType().toString().contains("GOLD")) armomrDamage -= 0.5;
				if (item.getType().toString().contains("IRON")) armomrDamage -= 0.75;
				if (item.getType().toString().contains("LEATHER")) armomrDamage -= 0.25;
				if (item.getType().toString().contains("CHAIN")) armomrDamage -= 0.5;
			}
			
			if (event.GetDamage() - armomrDamage <= 0)
				armomrDamage = -(event.GetDamage() - 1);
			
			event.AddMod(event.GetDamageePlayer().getName(), "Armor", armomrDamage, false);
		}

		event.SetKnockback(false);
		event.SetIgnoreRate(true);
		event.SetIgnoreArmor(true);
		
		Bukkit.getPluginManager().callEvent(new CustomGunDamageEvent(bullet, event.GetDamageePlayer(), hitArea == 1, event, this));
		event.setMetadata("gunType", bullet.Gun.getGunStats().name());
	}

	public int getArrowHitArea(Player damagee, Location origin, Vector trajectory)
	{
		//Move to near-player
		Location start = origin.clone().add(trajectory.clone().multiply(UtilMath.offset(origin, damagee.getEyeLocation()) - 2));

		Location loc = start.clone();

		while (!hitHead(damagee, loc) && !hitBody(damagee, loc) && UtilMath.offset(damagee.getLocation(), loc) < 6)
		{
			loc.add(trajectory.clone().multiply(0.1));
		}

		if (hitHead(damagee, loc))
		{
			return 1;
		}


		if (hitBody(damagee, loc))
		{
			return 0;
		}

		return -1;
	}

	public int getSnowballHitArea(Player damagee, Projectile snowball)
	{
		//Move to near-player
		Location start = snowball.getLocation();

		Location loc = start.clone();

		while (!hitHead(damagee, loc) && !hitBody(damagee, loc) && UtilMath.offset(damagee.getLocation(), loc) < 6)
		{
			loc.add(snowball.getVelocity().clone().multiply(0.1));
		}

		if (hitBody(damagee, loc))
			return 0;

		if (hitHead(damagee, loc))
			return 1;

		return -1;
	}

	public boolean hitBody(Player player, Location loc)
	{
		return UtilMath.offset2d(loc, player.getLocation()) < 0.6 &&	
				loc.getY() >= player.getLocation().getY() &&
				loc.getY() <= player.getEyeLocation().getY();
	}

	public boolean hitHead(Player player, Location loc)
	{
		return UtilMath.offset2d(loc, player.getLocation()) < 0.2 &&	
				loc.getY() >= player.getEyeLocation().getY() + 0.0 &&
				loc.getY() < player.getEyeLocation().getY() + 0.2;		
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void killMessage(CombatDeathEvent event)
	{
		Player killed = (Player)event.GetEvent().getEntity();

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			String reason = "You died.";
			if (killer != null)
			{
				if (killer.equals(killed))
				{
					reason = "You killed yourself";
				}
				else
				{
					reason = "";
					
					GameTeam team = _host.GetTeam(killer);
					if (team != null)
						reason += team.GetColor();
						
					reason += killer.getName() + ChatColor.RESET + " killed you with " + event.GetLog().GetLastDamager().GetReason();
				}
			}
			
			UtilTextMiddle.display("DEAD", reason, 0, 100, 20, killed);
		}
	}

	@EventHandler
	public void updateBulletsGrenades(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Bullets
		Iterator<Entity> bulletIterator = _bullets.keySet().iterator();

		while (bulletIterator.hasNext())
		{
			Entity bullet = bulletIterator.next();

			if (!bullet.isValid() || bullet.getTicksLived() > 40)
			{
				bulletIterator.remove();
				bullet.remove();
			}
		}

		//Grenades
		Iterator<Entity> grenadeIterator = _grenadesThrown.keySet().iterator();

		while (grenadeIterator.hasNext())
		{
			Entity grenadeItem = grenadeIterator.next();

			UtilParticle.PlayParticle(ParticleType.CRIT, grenadeItem.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.NORMAL, UtilServer.getPlayers());

			//Completed
			Grenade grenade = _grenadesThrown.get(grenadeItem);
			if (grenade.update(this, grenadeItem))
			{
				grenadeItem.remove();
				grenadeIterator.remove();
			}
		}
	}

	public void dropSlotItem(Player player, int slot)
	{
		for (Gun gun : _gunsEquipped.keySet())
		{
			if (!_gunsEquipped.get(gun).equals(player))
				continue;
			
			if (player.getInventory().getItem(slot) == null)
				continue;
			
			if (gun.isStack(player.getInventory().getItem(slot)))
			{
				gun.drop(this, player, false, false);
				player.getInventory().setItem(slot, null);
				return;
			}
		}
	}

	@EventHandler
	public void bombItemUpdate(UpdateEvent event)
	{
		if (!_host.InProgress())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		if (_bombItem == null)
			return;

		UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, _bombItem.getLocation().add(0.0, 0.2, 0.0), 0, 0, 0, 0, 1,
				ViewDist.LONG, UtilServer.getPlayers());
	}
	
	public void reset()
	{
		//Dropped Guns
		for (Entity ent : _gunsDropped.keySet())
			ent.remove();
		_gunsDropped.clear();

		//Dropped Grenades
		for (Entity ent : _grenadesDropped.keySet())
			ent.remove();
		_grenadesDropped.clear();

		//Thrown Grenades
		for (Entity ent : _grenadesThrown.keySet())
			ent.remove();
		_grenadesThrown.clear();

		//Bullets
		for (Entity ent : _bullets.keySet())
			ent.remove();
		_bullets.clear();

		//Bullets
		for (Entity ent : _defusalDropped)
			ent.remove();
		_defusalDropped.clear();

		//Incendiary
		_incendiary.clear();
		Manager.GetBlockRestore().restoreAll();

		//Smoke
		for (Block block : _smokeBlocks.keySet())
			block.setType(Material.AIR);
		_smokeBlocks.clear();

		//Restock Ammo
		for (Gun gun : _gunsEquipped.keySet())
			gun.restockAmmo(_gunsEquipped.get(gun));	
	}

	@EventHandler
	public void restartPlayerFreeze(PlayerMoveEvent event)
	{
		if (_freezeTime <= 0)
			return;

		if (!_host.IsAlive(event.getPlayer()))
			return;

		if (UtilMath.offset2d(event.getFrom(), event.getTo()) <= 0)
			return;

		event.getFrom().setPitch(event.getTo().getPitch());
		event.getFrom().setYaw(event.getTo().getYaw());

		event.setTo(event.getFrom());
	}

	//@EventHandler
	public void boostClimb(PlayerToggleSneakEvent event)
	{
		if (!_host.IsLive())
			return;

		Player player = event.getPlayer();

		boolean nearOther = false;
		for (Player other : _host.GetPlayers(true))
		{
			if (player.equals(other))
				continue;

			if (UtilMath.offset(player, other) < 1 && other.getLocation().getY() <= player.getLocation().getY())
			{
				nearOther = true;
				break;
			}
		}

		if (!nearOther)
			return;

		if (!Recharge.Instance.use(player, "Boost", 1500, false, false))
			return;

		UtilAction.velocity(event.getPlayer(), new Vector(0,1,0), 0.6, false, 0, 0, 1, true);

		Recharge.Instance.useForce(player, "Boost", 1500);

		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
	}

	@EventHandler
	public void healthCancel(EntityRegainHealthEvent event)
	{
		if (!BlockRegeneration)
			return;
		
		if (event.getRegainReason() == RegainReason.SATIATED)
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void scopeUpdate(PlayerToggleSneakEvent event)
	{
		if (!_host.IsLive())
			return;

		Gun gun = getGunInHand(event.getPlayer(), null);
		if (gun == null)
			return;

		if (!gun.hasScope())
			return;

		//Enable
		if (!event.getPlayer().isSneaking())
		{
			_scoped.put(event.getPlayer(), event.getPlayer().getInventory().getHelmet());
			Manager.GetCondition().Factory().Slow("Scope", event.getPlayer(), null, 9999, 2, false, false, false, false);
			event.getPlayer().getInventory().setHelmet(new ItemStack(Material.PUMPKIN));

			if (gun.getGunType() == GunType.SNIPER)
			{
				event.getPlayer().getWorld().playSound(event.getPlayer().getEyeLocation(), Sound.GHAST_DEATH, 0.8f, 1f);

				//if (Manager.GetCondition().GetActiveCondition(event.getPlayer(), ConditionType.BLINDNESS) == null)
				Manager.GetCondition().Factory().Blind("Scope Blind", event.getPlayer(), null, 1, 0, false, false, false);
				Manager.GetCondition().Factory().NightVision("Scope Accuracy", event.getPlayer(), null, 0.5, 0, false, false, false);
			}
		}
		else
		{
			removeScope(event.getPlayer());
		}
	}
	
	@EventHandler
	public void scopeUpdate(UpdateEvent event)
	{
		if (!_host.IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : _host.GetPlayers(true))
		{
			if (!_scoped.containsKey(player))
				continue;

			Gun gun = getGunInHand(player, null);
			if (gun == null || !gun.hasScope() || !player.isSneaking())
			{
				removeScope(player);
			}
		}
	}

	public void removeScope(Player player)
	{
		if (!_scoped.containsKey(player))
			return;

		ItemStack stack = _scoped.remove(player);


		player.getInventory().setHelmet(stack);
		UtilInv.Update(player);

		Manager.GetCondition().EndCondition(player, null, "Scope");

		player.getWorld().playSound(player.getEyeLocation(), Sound.GHAST_DEATH, 0.8f, 1f);
	}

	//@EventHandler
	public void speedUpdate(UpdateEvent event)
	{
		if (!_host.IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (_host.IsAlive(player))
			{
				if (UtilGear.isMat(player.getItemInHand(), Material.IRON_AXE) || UtilGear.isMat(player.getItemInHand(), Material.IRON_SWORD))
					player.setWalkSpeed(0.2F);
				else
					player.setWalkSpeed(0.1F);
			}
			else
			{
				player.setWalkSpeed(0.1F);
			}
		}
	}

	@EventHandler
	public void incendiaryUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
			for (Player player : UtilServer.getPlayers())
				player.setFireTicks(0);

		if (event.getType() != UpdateType.SLOW)
			return;

		Iterator<Location> fireIterator = _incendiary.keySet().iterator();

		while (fireIterator.hasNext())
		{
			Location loc = fireIterator.next();

			if (_incendiary.get(loc) < System.currentTimeMillis())
				fireIterator.remove();

			else 
				loc.getWorld().playSound(loc, Sound.PIG_DEATH, 1f, 1f);
		}
	}
	
	@EventHandler
	public void gunUpdate(UpdateEvent event)
	{
		if (!_host.IsLive())
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Gun gun : _gunsEquipped.keySet())
		{
			gun.displayAmmo(_gunsEquipped.get(gun));
		}	
	}
	
	@EventHandler
	public void smokeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Block> smokeIterator = _smokeBlocks.keySet().iterator();

		while (smokeIterator.hasNext())
		{
			Block block = smokeIterator.next();

			if (System.currentTimeMillis() > _smokeBlocks.get(block))
			{
				if (block.getType() == Material.PORTAL)
					block.setTypeIdAndData(0, (byte)0, false);
				
				smokeIterator.remove();
			}
			else if (block.getType() == Material.AIR)
			{
				block.setTypeIdAndData(90, (byte)UtilMath.r(2), false);
			}
		}
	}

	@EventHandler
	public void bombBurnUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_bombItem == null)
			return;

		if (!_bombItem.isValid())
		{
			Location loc = _bombItem.getLocation();

			_bombItem.remove();

			_bombItem = loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_SWORD));
		}
	}

	//Cleans entities that may not have been removed due to unloaded chunks
	@EventHandler
	public void clean(UpdateEvent event)
	{
		if(!EnableCleaning)
			return;
		
		if (!_host.IsLive())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		for (Entity ent : _host.WorldData.World.getEntities())
		{
			if (ent instanceof Player)
				continue;

			if (ent instanceof Painting)
				continue;

			if (_gunsDropped.containsKey(ent))
				continue;

			if (_grenadesDropped.containsKey(ent))
				continue;

			if (_grenadesThrown.containsKey(ent))
				continue;

			if (_bullets.containsKey(ent))
				continue;

			if (_defusalDropped.contains(ent))
				continue;

			if (_bombItem != null && _bombItem.equals(ent))
				continue;
			
			if (ent instanceof Item && (((Item)ent).getItemStack().getTypeId() == 175))
				continue;

			ent.remove();
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.LOWEST)
	public void damagePainting(PaintingBreakEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void entityExpire(ItemDespawnEvent event)
	{
		if (_gunsDropped.containsKey(event.getEntity()))
			event.setCancelled(true);

		else if (_grenadesDropped.containsKey(event.getEntity()))
			event.setCancelled(true);

		else if (_grenadesThrown.containsKey(event.getEntity()))
			event.setCancelled(true);

		else if (_defusalDropped.contains(event.getEntity()))
			event.setCancelled(true);

		else if (_bombItem != null && _bombItem.equals(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void terroristCompass(UpdateEvent event)
	{
		if (!_host.IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		Location target = null;
		if (_bombItem != null)
			target = _bombItem.getLocation();
		else if (_bombHolder != null)
			target = _bombHolder.getLocation();
		else if (_bomb != null)
			target = _bomb.Block.getLocation();

		if (_host.GetTeam(ChatColor.RED) == null)
			return;
		
		for (Player player : _host.GetTeam(ChatColor.RED).GetPlayers(true))
		{
			//Has Bomb
			if (player.getInventory().contains(Material.GOLD_SWORD))
				continue;

			//Error - Random Loc
			if (target == null)
				target = player.getLocation().add(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);

			//Set Target
			player.setCompassTarget(target);

			ItemStack stack = new ItemStack(Material.COMPASS);

			//Text
			ItemMeta itemMeta = stack.getItemMeta();

			if (_bombItem != null)
			{
				itemMeta.setDisplayName(
						"    " + C.cGreen + C.Bold + "Bomb Dropped" + 
								"    " + C.cGreen + C.Bold + "Distance: " + C.cWhite + UtilMath.trim(1, UtilMath.offset2d(target, player.getLocation())));
			}
			else if (_bombHolder != null)
			{
				itemMeta.setDisplayName(
						"    " + C.cGreen + C.Bold + "Bomb Holder: " + C.cWhite + _bombHolder.getName() +
						"    " + C.cGreen + C.Bold + "Distance: " + C.cWhite + UtilMath.trim(1, UtilMath.offset2d(target, player.getLocation())));
			}
			else if (_bomb != null)
			{
				itemMeta.setDisplayName(
						"    " + C.cGreen + C.Bold + "Bomb Planted" + 
								"    " + C.cGreen + C.Bold + "Distance: " + C.cWhite + UtilMath.trim(1, UtilMath.offset2d(target, player.getLocation())));
			}
			else
			{
				itemMeta.setDisplayName(
						"    " + C.cGreen + C.Bold + "Bomb Not Found");;
			}

			stack.setItemMeta(itemMeta);

			//Set
			player.getInventory().setItem(8, stack);
		}	
	}
	
	@EventHandler
	public void fireDamage(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.FIRE)
			event.AddMod(_host.GetName(), "Fire", 1, false);
	}

	@EventHandler
	public void teleportCancel(PlayerTeleportEvent event)
	{
		if (!_host.IsLive())
			return;

		if (event.getCause() == TeleportCause.ENDER_PEARL)
			event.setCancelled(true);
	}

	public int getBulletType()
	{
		return BulletInstant;
	}

	public void playSound(Radio radio, Player player, GameTeam team)
	{
		if (player == null && team == null)
		{
			for (Player other : UtilServer.getPlayers())
				other.playSound(other.getLocation(), radio.getSound(), 1.5f, 1f);
		}
		else if (team != null)
		{
			for (Player other : team.GetPlayers(false))
				other.playSound(other.getLocation(), radio.getSound(), 1.5f, 1f);
		}
		else if (player != null)
		{
			GameTeam playerTeam = _host.GetTeam(player);
			if (playerTeam == null)
				return;

			for (Player other : playerTeam.GetPlayers(false))
				other.playSound(player.getLocation(), radio.getSound(), 1.5f, 1f);
		}
	}
	
	@EventHandler
	public void debug(PlayerCommandPreprocessEvent event)
	{
		if (!event.getPlayer().isOp())
		{
			return;
		}
		
		if (event.getMessage().contains("recoil"))
		{
			try
			{
				RECOIL = Float.parseFloat(event.getMessage().split(" ")[1]);
				_host.Announce(C.cPurple + C.Bold + "Recoil Bloom: " + ChatColor.RESET + (int)(RECOIL * 100) + "%");
			}
			catch (Exception e)
			{
				
			}
			event.setCancelled(true);
		}
		
		if (event.getMessage().contains("cone"))
		{
			try
			{
				CONE = Float.parseFloat(event.getMessage().split(" ")[1]);
				_host.Announce(C.cPurple + C.Bold + "Cone of Fire: " + ChatColor.RESET + (int)(CONE * 100) + "%");
			}
			catch (Exception e)
			{
				
			}
			event.setCancelled(true);
		}
		
		if (event.getMessage().contains("move"))
		{
			try
			{
				MOVE_PENALTY = Float.parseFloat(event.getMessage().split(" ")[1]);
				_host.Announce(C.cPurple + C.Bold + "Move/Sprint/Jump Penalties: " + ChatColor.RESET + (int)(MOVE_PENALTY * 100) + "%");
			}
			catch (Exception e)
			{
				
			}
			event.setCancelled(true);
		}

		if (event.getMessage().contains("instant"))
		{
			BulletInstant = (BulletInstant + 1)%3;

			if (BulletInstant == 0)
				_host.Announce(C.cPurple + C.Bold + "Bullets: " + ChatColor.RESET + "Slow and Visible");
			else if (BulletInstant == 1)
				_host.Announce(C.cPurple + C.Bold + "Bullets: " + ChatColor.RESET + "Instant and Invisible");
			else
				_host.Announce(C.cPurple + C.Bold + "Bullets: " + ChatColor.RESET + "Slow and Visible with Instant Sniper");


			event.setCancelled(true);
		}

		if (event.getMessage().contains("hitbox"))
		{
			CustomHitbox = !CustomHitbox;

			if (CustomHitbox)
				_host.Announce(C.cPurple + C.Bold + "Hitbox: " + ChatColor.RESET + "Accurate with Headshots");
			else
				_host.Announce(C.cPurple + C.Bold + "Hitbox: " + ChatColor.RESET + "Default with No Headshot");

			event.setCancelled(true);
		}

		if (event.getMessage().contains("alternate"))
		{
			BulletAlternate = !BulletAlternate;

			_host.Announce(C.cPurple + C.Bold + "Alternate Bullet Type: " + ChatColor.RESET + BulletAlternate);

			event.setCancelled(true);
		}

		if (event.getMessage().contains("god"))
		{
			if (_host.HealthSet == 20)
				_host.HealthSet = -1;
			else
				_host.HealthSet = 20;

			_host.Announce(C.cPurple + C.Bold + "God Mode: " + ChatColor.RESET + (_host.HealthSet == 20));

			event.setCancelled(true);
		}

		if (event.getMessage().contains("debugplayer"))
		{
			_host.Announce(C.Bold + "PLAYER DEBUG:");

			for (Player player : UtilServer.getPlayers())
			{
				GameTeam team = _host.GetTeam(player);

				_host.Announce(player.getName() + "   " + 
						(team != null ? team.GetColor() + team.GetName() : C.cGray + "No Team") + "   " + 
						(_host.IsAlive(player) ? C.cGreen + "ALIVE" : C.cRed + "DEAD") + "   " + 
						C.cGray + UtilWorld.locToStrClean(player.getLocation())
						);
			}

			event.setCancelled(true);
		}

		if (event.getMessage().contains("debugteam"))
		{
			_host.Announce(C.Bold + "TEAM DEBUG:");

			for (GameTeam team : _host.GetTeamList())
				for (Player player : team.GetPlayers(false))
				{
					_host.Announce(player.getName() + "   " + 
							(team != null ? team.GetColor() + team.GetName() : C.cGray + "No Team") + "   " + 
							(_host.IsAlive(player) ? C.cGreen + "ALIVE" : C.cRed + "DEAD") + "   " + 
							C.cGray + UtilWorld.locToStrClean(player.getLocation())
							);
				}

			event.setCancelled(true);
		}
	}

	public boolean isFreezeTime()
	{
		return _freezeTime > 0;
	}
	
	@EventHandler
	public void cancelFireExtinguish(PlayerInteractEvent event)
	{
		// This is an extra event added in order to prevent players from
		// extinguishing fire that gets generated from moltovs and incendiary grenades.

		if (!_host.IsLive())
			return;

		if (UtilEvent.isAction(event, ActionType.L_BLOCK))
		{
			Block block = event.getClickedBlock();

			for (BlockFace face : BlockFace.values())
			{
				if (block.getRelative(face).getType() == Material.FIRE)
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void bombMove(PlayerMoveEvent event)
	{
		if (_bombHolder == null || !event.getPlayer().equals(_bombHolder))
			return;
		
		_bombHolderLastMove = System.currentTimeMillis();
	}
	
	@EventHandler
	public void bombTimeDrop(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (_freezeTime > 0)
			_bombHolderLastMove = System.currentTimeMillis();
		
		if (_bombHolder != null && UtilTime.elapsed(_bombHolderLastMove, 10000))
		{
			_bombHolder.getInventory().remove(Material.GOLD_SWORD);
						
			Vector vel = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);
			vel.normalize();
			vel.multiply(0.3);
			vel.setY(0.2);
			
			_bombItem = _bombHolder.getWorld().dropItem(_bombHolder.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.GOLD_SWORD, (byte)0, 1, C.cGold + C.Bold + "C4 Explosive"));
			_bombItem.setVelocity(vel);
			_bombItem.setPickupDelay(60);

			//Radio
			playSound(Radio.T_BOMB_DROP, null, _host.GetTeam(_bombHolder));
			
			_bombHolder = null;
		}
	}
	
	@EventHandler
	public void enableScpResPack(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equalsIgnoreCase("/rpoff"))
		{
			//event.getPlayer().setResourcePack("http://file.mineplex.com/ResReset.zip");
			event.getPlayer().setResourcePack("http://198.20.72.74/ResReset.zip");
			
			UtilPlayer.message(event.getPlayer(), F.main("Resource Pack", "Resource Pack: " + C.cRed + "Disabled"));
			UtilPlayer.message(event.getPlayer(), F.main("Resource Pack", "Type " + F.elem("/rpon") + " to enable."));
		}
		else if (event.getMessage().equalsIgnoreCase("/rpon"))
		{
			Manager.getResourcePackManager().setPlayerPack(event.getPlayer());
			
			UtilPlayer.message(event.getPlayer(), F.main("Resource Pack", "Resource Pack: " + C.cGreen + "Enabled"));
			UtilPlayer.message(event.getPlayer(), F.main("Resource Pack", "Type " + F.elem("/rpoff") + " to disable."));
		}
	}
	
	public void giveBombToPlayer(Player player)
	{
		if (player == null)
			return;

		GameTeam team = _host.GetTeam(player);
		if (team == null)
			return;

		if (team.GetColor() != ChatColor.RED)
			return;

		//Bomb
		player.getInventory().setItem(8, ItemStackFactory.Instance.CreateStack(Material.GOLD_SWORD, (byte)0, 1, C.cGold + C.Bold + "C4 Explosive"));

		//Inform
		if (!_host.IsLive() || _freezeTime > 0)
		{
			for (Player other : team.GetPlayers(false))
				if (!other.equals(player))
				{
					UtilTextMiddle.display(null, player.getName() + " has the Bomb", 10, 80, 10, other);
					UtilPlayer.message(other, C.cGold + C.Bold + player.getName() + " has the Bomb!");
				}

			//Chat
			UtilPlayer.message(player, C.cGold + C.Bold + "You have the Bomb!");
			UtilPlayer.message(player, C.cGold + C.Bold + "Hold Right-Click to place at a Bomb Site!");

			//Title
			UtilTextMiddle.display(C.cRed + "You have the Bomb", "Hold Right-Click to place at a Bomb Site!", 10, 80, 10, player);
		}
		else
		{
			for (Player other : team.GetPlayers(false))
				if (!other.equals(player))
				{
					UtilPlayer.message(other, C.cGold + C.Bold + player.getName() + " picked up the Bomb!");

					//Title
					UtilTextMiddle.display(null, player.getName() + " picked up the Bomb", 10, 50, 10, other);
				}


			//Chat
			UtilPlayer.message(player, C.cGold + C.Bold + "You picked up the Bomb!");

			//Title
			UtilTextMiddle.display(null, "You picked up the Bomb", 10, 50, 10, player);
		}


		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 2f);
		_bombHolder = player;
		_bombHolderLastMove = System.currentTimeMillis();

		if (_bombItem != null)
		{
			_bombItem.remove();
			_bombItem = null;
		}
	}
	
	public void undisguise(Player player)
	{
		_disguise.remove(player);
	}
	
	public Player getBombHolder()
	{
		return _bombHolder;
	}
	
	public void setBombHolder(Player player)
	{
		_bombHolder = player;
	}
	
	public Bomb getBomb()
	{
		return _bomb;
	}
	
	public void setBomb(Bomb bomb)
	{
		_bomb = bomb;
	}
	
	public Item getBombItem()
	{
		return _bombItem;
	}
	
	public void setBombItem(Item bomb)
	{
		_bombItem = bomb;
	}
	
	public long getFreezeTime()
	{
		return _freezeTime;
	}
	
	public void setFreezeTime(long time)
	{
		_freezeTime = time;
	}
	
	public int getRound()
	{
		if (_host instanceof Minestrike)
			return ((Minestrike) _host).getRound();
		
		return _round;
	}
	
	public void setRound(int round)
	{	
		_round = round;
	}
	
	public void setEnablePickup(boolean pickup)
	{
		EnablePickup = pickup;
	}
	
	public void setEnableDrop(boolean drop)
	{
		EnableDrop = drop;
	}
	
	public void setEnableCleaning(boolean cleaning)
	{
		EnableCleaning = cleaning;
	}
	
	public Game getHost()
	{
		return _host;
	}
	
	public HashMap<Entity, Gun> getDroppedGuns()
	{
		return _gunsDropped;
	}
	
	public HashMap<Entity, Grenade> getDroppedGrenades()
	{
		return _grenadesDropped;
	}
	
	public HashMap<Player, ItemStack> getScoped()
	{
		return _scoped;
	}
	
	public void setDisableDamage(boolean disabled)
	{
		_disableDamage = disabled;
	}
}
