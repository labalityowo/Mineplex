package nautilus.game.arcade.game.games.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.paintball.events.PaintballEvent;
import nautilus.game.arcade.game.games.paintball.events.ReviveEvent;
import nautilus.game.arcade.game.games.paintball.kits.KitMachineGun;
import nautilus.game.arcade.game.games.paintball.kits.KitRifle;
import nautilus.game.arcade.game.games.paintball.kits.KitShotgun;
import nautilus.game.arcade.game.games.paintball.kits.KitSniper;
import nautilus.game.arcade.game.games.paintball.trackers.KillingSpreeTracker;
import nautilus.game.arcade.game.games.paintball.trackers.LastStandStatTracker;
import nautilus.game.arcade.game.games.paintball.trackers.MedicStatTracker;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.stats.WinFastStatTracker;
import nautilus.game.arcade.stats.WinWithoutLosingTeammateStatTracker;

public class Paintball extends TeamGame
{
	private HashMap<Player, PlayerCopyPaintball> _doubles = new HashMap<Player, PlayerCopyPaintball>();

	@SuppressWarnings("unchecked")
	public Paintball(ArcadeManager manager)
	{
		super(manager, GameType.Paintball, new Kit[]
				{ 
						new KitRifle(manager),				
						new KitShotgun(manager),
						new KitMachineGun(manager),
						new KitSniper(manager),
						
				}, new String[]
						{
								"Shoot enemies to paint them",
								"Revive/heal with Water Bombs",
								"Last team alive wins!"
						});

		StrictAntiHack = true;

		HungerSet = 20;
		
		InventoryClick = false;

		PlayerGameMode = GameMode.ADVENTURE;

		registerStatTrackers(
				new KillingSpreeTracker(this),
				new WinWithoutLosingTeammateStatTracker(this, "FlawlessVictory"),
				new MedicStatTracker(this),
				new WinFastStatTracker(this, 30, "Speedrunner"),
				new LastStandStatTracker(this)
				);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageTaken,
				DamageDealt
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
		new TeamArmorModule()
				.giveHotbarItem()
				.register(this);
	}

	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		this.GetTeamList().get(0).SetColor(ChatColor.AQUA);
		this.GetTeamList().get(0).SetName("Frost");

		this.GetTeamList().get(1).SetColor(ChatColor.RED);
		this.GetTeamList().get(1).SetName("Nether");
	}
	
	@EventHandler
	public void onNameTag(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare)
			for (Team team : Scoreboard.getScoreboard().getTeams())
				team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void ColorArmor(PlayerPrepareTeleportEvent event)
	{
		CleanColorArmor(event.GetPlayer());
	}

	@EventHandler
	public void HealthRegen(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED)
			event.setCancelled(true);
	}

	@EventHandler
	public void Teleport(PlayerTeleportEvent event)
	{
		if (event.getCause() == TeleportCause.ENDER_PEARL)
			event.setCancelled(true);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void Paint(final ProjectileHitEvent event)
	{
		if (IsLive() || GetState() == GameState.End) 
		{
			if (event.getEntity() instanceof ThrownPotion)	
				return;

			final byte color = (byte) (event.getEntity() instanceof EnderPearl || (event.getEntity() instanceof Arrow && event.getEntity().hasMetadata("color") && ChatColor.values()[event.getEntity().getMetadata("color").get(0).asInt()] == ChatColor.RED) ? 14 : 3);

			event.getEntity().setVelocity(event.getEntity().getVelocity().normalize().multiply(1.2));

			PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(new int[]{event.getEntity().getEntityId()});
			for (Player player : UtilServer.getPlayers())
				UtilPlayer.sendPacket(player, destroy);

			Manager.runSyncLater(new Runnable() // Stupid thing I have to do to make sure the arrow's location is accounted for. Stupid mojang. - Myst
					{
				@Override
				public void run()
				{
					Location loc = event.getEntity().getLocation();
					// loc.add(event.getEntity().getVelocity().clone().normalize().multiply(.5));

					for (Block block : UtilBlock.getInRadius(loc, 1.5d).keySet())
					{
						if (block.getType() != Material.WOOL && block.getType() != Material.STAINED_CLAY && block.getType() != Material.HARD_CLAY)
							continue;

						if (block.getType() == Material.HARD_CLAY)
							block.setType(Material.STAINED_CLAY);

						block.setData(color);
					}

					if (color == 3)		loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 8);
					else				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 10);

					event.getEntity().remove();
				}
					}, 0);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		if (_doubles.containsKey(player))
		{
			PlayerCopyPaintball copy = _doubles.remove(player);
			copy.GetEntity().remove();
			copy.GetHolo().stop();
			copy.GetSaveMe().stop();
		}
	}

	@EventHandler
	public void DamageCancel(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() == null)
			event.SetCancelled("Not Player");

		// Fixed void damage being blocked from this check.

		if (event.GetProjectile() == null && event.GetCause() != DamageCause.VOID)
			event.SetCancelled("No Projectile");
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PaintballDamage(CustomDamageEvent event)
	{
		if (!IsLive())
			return;

		if (event.GetProjectile() == null)
			return;

		if (!(event.GetProjectile() instanceof Snowball) && !(event.GetProjectile() instanceof EnderPearl) && !(event.GetProjectile() instanceof Arrow))	
			return;

		//Negate

		event.AddMod("Negate", "Negate", -event.GetDamageInitial(), false);

		event.AddMod("Paintball", "Paintball", 2, true);
		event.AddKnockback("Paintball", 2);
		
		event.setShowArrows(false);

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)
			return;

		GameTeam damageeTeam = GetTeam(damagee);
		if (damageeTeam == null)
			return;

		GameTeam damagerTeam = GetTeam(damager);
		if (damagerTeam == null)
			return;

		if (damagerTeam.equals(damageeTeam))
			return;

		//Count
		int count = 1;
		if (GetKit(damager) != null)
		{
			if (GetKit(damager) instanceof KitRifle)
			{
				count = 3;
			}
			if (GetKit(damager) instanceof KitSniper && event.GetProjectile() instanceof Arrow)
			{
				count = ((KitSniper) GetKit(damager)).getPaintDamage((Arrow) event.GetProjectile());
				
				if (count == -1)
					count = 1;
			}
		}

		//Out
		if (Color(damagee, count))
		{
			for (Player player : UtilServer.getPlayers())
				UtilPlayer.message(player, damageeTeam.GetColor() + damagee.getName() + ChatColor.RESET + " was painted by " + 
					damagerTeam.GetColor() + damager.getName() + ChatColor.RESET + "!");
			
			PlayerOut(damagee, damager);

			AddGems(damager, 2, "Kills", true, true);
			
			AddStat(damager, "Kills", 1, false, false);
			AddStat(damagee, "Deaths", 1, false, false);
			
			Bukkit.getPluginManager().callEvent(new PaintballEvent(damagee, damager));
		}

		//Hit Sound
		Player player = event.GetDamagerPlayer(true);
		if (player != null)
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 3f);
	}

	@EventHandler
	public void ArmorRemoveCancel(InventoryClickEvent event)
	{
		if (!IsAlive(event.getWhoClicked()))
			event.setCancelled(true);
	}
	
	public boolean Color(Player player, int amount)
	{
		//Get Non-Coloured
		ArrayList<ItemStack> nonColored = new ArrayList<ItemStack>();
		for (ItemStack stack : player.getInventory().getArmorContents())
		{
			if (!(stack.getItemMeta() instanceof LeatherArmorMeta))
				continue;

			LeatherArmorMeta meta = (LeatherArmorMeta)stack.getItemMeta();

			if (meta.getColor().equals(Color.RED) || meta.getColor().equals(Color.AQUA))
				nonColored.add(stack);
		}

		//Color Piece
		for (int i=0 ; i<amount ; i++)
		{
			if (nonColored.isEmpty())
				break;

			ItemStack armor = nonColored.remove(UtilMath.r(nonColored.size()));

			LeatherArmorMeta meta = (LeatherArmorMeta)armor.getItemMeta();
			meta.setColor(Color.PURPLE);
			armor.setItemMeta(meta);
		}

		player.setHealth(Math.min(20, Math.max(2, nonColored.size() * 5 + 1)));

		return nonColored.isEmpty();
	}

	public void PlayerOut(Player player, Player killer)
	{		
		//State
		SetPlayerState(player, PlayerState.OUT);
		player.setHealth(20);

		//Conditions
		Manager.GetCondition().Factory().Blind("Hit", player, player, 1.5, 0, false, false, false);
		Manager.GetCondition().Factory().Cloak("Hit", player, player, 9999, false, false);

		_doubles.put(player, new PlayerCopyPaintball(this, player, killer, GetTeam(player).GetColor()));
		
		//Settings
		player.setAllowFlight(true);
		player.setFlying(true);
		((CraftPlayer)player).getHandle().spectating = true;
		((CraftPlayer)player).getHandle().k = false;
		
		UtilAction.velocity(player, new Vector(0,1.2,0));
	}

//	@EventHandler
//	public void CleanThrow(PlayerInteractEvent event)
//	{
//		if (!IsLive())
//			return;
//		
//		Player player = event.getPlayer();
//
//		if (!UtilGear.isMat(player.getItemInHand(), Material.POTION))
//			return;
//
//		if (!IsAlive(player))
//			return;
//
//		if (!Recharge.Instance.use(player, "Water Bomb", 4000, false, false))
//			return;
//
//		//Use Stock
//		UtilInv.remove(player, Material.POTION, (byte)0, 1);
//
//		//Start
//		ThrownPotion potion = player.launchProjectile(ThrownPotion.class);
//
//		_water.add(potion);
//
//		//Inform
//		UtilPlayer.message(player, F.main("Skill", "You threw " + F.skill("Water Bomb") + "."));
//	}

	@EventHandler
	public void CleanHit(ProjectileHitEvent event)
	{
		if (!IsLive())
			return;
		
		if (!(event.getEntity() instanceof ThrownPotion))
			return;

		if (event.getEntity().getShooter() == null)
			return;

		if (!(event.getEntity().getShooter() instanceof Player))
			return;

		Player thrower = (Player)event.getEntity().getShooter();
		if (!IsAlive(thrower))
			return;

		GameTeam throwerTeam = GetTeam(thrower);
		if (throwerTeam == null)	return;

		//Revive
		Iterator<PlayerCopyPaintball> copyIterator = _doubles.values().iterator();
		while (copyIterator.hasNext())
		{
			PlayerCopyPaintball copy = copyIterator.next();

			GameTeam otherTeam = GetTeam(copy.GetPlayer());
			if (otherTeam == null || !otherTeam.equals(throwerTeam))
				continue;

			if (UtilMath.offset(copy.GetEntity().getLocation().add(0,1,0), event.getEntity().getLocation()) > 3)
				continue;

			PlayerIn(copy.GetPlayer(), copy);
			
			copyIterator.remove();

			AddGems(thrower, 3, "Revived Ally", true, true);

			Bukkit.getPluginManager().callEvent(new ReviveEvent(thrower, copy.GetPlayer()));
		}

		//Clean
		for (Player player : GetPlayers(true))
		{
			GameTeam otherTeam = GetTeam(player);
			if (otherTeam == null || !otherTeam.equals(throwerTeam))
				continue;

			if (UtilMath.offset(player.getLocation().add(0,1,0), event.getEntity().getLocation()) > 3)
				continue;

			PlayerIn(player, null);
		}
	}

	public void PlayerIn(final Player player, final PlayerCopyPaintball copy)
	{
		//State
		SetPlayerState(player, PlayerState.IN);
		player.setHealth(20);

		//Teleport
		if (copy != null)
		{
			Location loc = player.getLocation();
			loc.setX(copy.GetEntity().getLocation().getX());
			loc.setY(copy.GetEntity().getLocation().getY());
			loc.setZ(copy.GetEntity().getLocation().getZ());
			player.teleport(loc);
		}

		//Settings
		if (player.getGameMode() == GameMode.SPECTATOR)
			player.setSpectatorTarget(null);
			
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setFlying(false);
		((CraftPlayer)player).getHandle().spectating = false;
		((CraftPlayer)player).getHandle().k = true;
		
		//Items
		player.getInventory().remove(Material.WATCH);
		player.getInventory().remove(Material.COMPASS);

		//Clean Armor
		CleanColorArmor(player);

		//Reapply Team Item
		getModule(TeamArmorModule.class).apply(player);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You have been cleaned!"));

		//Delayed Visibility
		if (copy != null)
		{
			UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					//Remove Invis
					if (IsAlive(player))
						Manager.GetCondition().EndCondition(player, ConditionType.CLOAK, null);

					//Remove Copy
					copy.GetEntity().remove();
					copy.GetHolo().stop();
					copy.GetSaveMe().stop();
				}
			}, 4);
		}
	}
	
	public void CleanColorArmor(Player player)
	{
		Color color = Color.RED;
		if (Manager.GetColor(player) != ChatColor.RED)
			color = Color.AQUA;
		
		for (ItemStack stack : player.getEquipment().getArmorContents())
		{
			if (!(stack.getItemMeta() instanceof LeatherArmorMeta))
				continue;

			LeatherArmorMeta meta = (LeatherArmorMeta)stack.getItemMeta();
			meta.setColor(color);
			stack.setItemMeta(meta);
		}
	}
	
	@EventHandler
	public void removePotionEffect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : GetPlayers(true))
			player.removePotionEffect(PotionEffectType.WATER_BREATHING);
	}
	
	@EventHandler
	public void onHeal(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		LaunchPotion(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onHeal(PlayerInteractEntityEvent event)
	{
		LaunchPotion(event.getPlayer(), event);
	}
	
	@EventHandler(ignoreCancelled = false)
	public void onHeal(PlayerInteractAtEntityEvent event)
	{
		LaunchPotion(event.getPlayer(), event);
	}
	
	public void LaunchPotion(Player player, Cancellable event)
	{
		if (!IsLive())
			return;
		
		if (!UtilGear.isMat(player.getItemInHand(), Material.POTION))
			return;
		
		event.setCancelled(true);
		
		if (!IsAlive(player) || UtilPlayer.isSpectator(player))
			return;		
		
		if (!Recharge.Instance.use(player, "Water Potion", 250, false, false))
			return;
	
		UtilInv.UseItemInHand(player);
		
		ThrownPotion potion = player.launchProjectile(ThrownPotion.class);
		potion.getEffects().clear();
		potion.getEffects().add(new PotionEffect(PotionEffectType.WATER_BREATHING, 100, 100));
		
		Manager.runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				UtilInv.Update(player);
			}
		});
	}
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void updateCloneHolograms(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		for (PlayerCopyPaintball clone : _doubles.values())
		{
			if (clone.GetHolo().getHologramTarget() == HologramTarget.WHITELIST)
			{
				//Other team blacklist
				clone.GetHolo().setHologramTarget(HologramTarget.BLACKLIST);

				for (Player cur : GetPlayers(false))
				{
					if (!IsAlive(cur))
					{
						if (clone.GetHolo().containsPlayer(cur))
							clone.GetHolo().removePlayer(cur);
					}
					else
					{
						if (GetTeam(cur) != GetTeam(clone.GetPlayer()))
						{
							if (!clone.GetHolo().containsPlayer(cur))
								clone.GetHolo().addPlayer(cur);
						}
						else if (clone.GetHolo().containsPlayer(cur))
						{
							clone.GetHolo().removePlayer(cur);
						}
					}
				}
				clone.GetHolo().start();
			}

			clone.setSaveMeFlop(!clone.getSaveMeFlop());
			clone.GetSaveMe().setText((clone.getSaveMeFlop() ? C.cRedB : C.cWhiteB) + "SAVE ME!");

			for (Player player : GetTeam(clone.GetPlayer()).GetPlayers(false))
			{
				if (!IsAlive(player)) // Remove if it's not alive
				{
					if (clone.GetSaveMe().containsPlayer(player))
						clone.GetSaveMe().removePlayer(player);
				}
				else
				{
					boolean hasPotion = UtilInv.contains(player, Material.POTION, (byte) -1, 1);
					if (clone.GetSaveMe().containsPlayer(player))
					{
						if (!hasPotion) // No potions left
						{
							clone.GetSaveMe().removePlayer(player);
						}
					}
					else if (hasPotion)
					{
						clone.GetSaveMe().addPlayer(player);
					}
				}
			}			
		}
	}
}
