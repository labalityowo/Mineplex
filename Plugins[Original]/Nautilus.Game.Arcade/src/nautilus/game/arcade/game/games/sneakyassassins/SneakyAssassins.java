package nautilus.game.arcade.game.games.sneakyassassins;

import mineplex.core.common.Pair;
import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.*;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.*;
import mineplex.core.updater.event.*;
import mineplex.minecraft.game.core.combat.event.*;
import mineplex.minecraft.game.core.damage.*;
import nautilus.game.arcade.*;
import nautilus.game.arcade.events.*;
import nautilus.game.arcade.game.*;
import nautilus.game.arcade.game.games.sneakyassassins.kits.*;
import nautilus.game.arcade.game.games.sneakyassassins.npc.*;
import nautilus.game.arcade.game.games.sneakyassassins.powerups.*;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.*;
import nautilus.game.arcade.stats.KillEntityStatTracker;
import nautilus.game.arcade.stats.MasterAssassinStatTracker;
import nautilus.game.arcade.stats.RevealStatTracker;
import nautilus.game.arcade.stats.TheMastersMasterStatTracker;

import org.bukkit.*;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;

import java.util.*;

public class SneakyAssassins extends SoloGame
{
	private static final Map<Material, Double> SWORD_DAMAGE = new EnumMap<>(Material.class);

	static
	{
		SWORD_DAMAGE.put(Material.WOOD_SWORD, 4.0);
		SWORD_DAMAGE.put(Material.STONE_SWORD, 4.5);
		SWORD_DAMAGE.put(Material.GOLD_SWORD, 5.0);
		SWORD_DAMAGE.put(Material.IRON_SWORD, 6.0);
		SWORD_DAMAGE.put(Material.DIAMOND_SWORD, 7.0);
	}

	private NpcManager _npcManager;
	private PowerUpManager _powerUpManager;
	private EntityType _disguiseType = EntityType.VILLAGER;
	private int _revealCountdown = 60;
	private KillEntityStatTracker _killEntityStatTracker;

	public SneakyAssassins(ArcadeManager manager)
	{
		super(
				manager,
				GameType.SneakyAssassins,
				new Kit[]{
						new KitEscapeArtist(manager),
						new KitAssassin(manager),
						new KitRevealer(manager),
						new KitBriber(manager),
				},
				new String[]
				{
						"Each kill grants you +1 Heart",
						"Powerups upgrade Armor/Weapon",
						"Get 5 Powerups to become a Master!",
						"Master has a Compass to find others",
						"Last player alive wins!"
				}
		);

		this._npcManager = new NpcManager(this, UtilMath.random);

		this.StrictAntiHack = true;
		
		this.DamageTeamSelf = true;
		this.PrepareFreeze = false;
		
		this.HungerSet = 20;

		this.AllowParticles = false;

		this.PlayerGameMode = GameMode.ADVENTURE;

		new CompassModule()
				.setGiveCompassToAlive(true)
				.setGiveCompass(false)
				.register(this);
		
		Manager.getCosmeticManager().setHideParticles(true);

		_killEntityStatTracker = new KillEntityStatTracker(this, "Incompetence", _npcManager.getDisguiseType());

		registerStatTrackers(
				new MasterAssassinStatTracker(this),
				new TheMastersMasterStatTracker(this),
				new RevealStatTracker(this, "ISeeYou"),
				_killEntityStatTracker
		);

		registerChatStats(//assists
				Kills,
				Assists,
				BlankLine,
				DamageTaken,
				DamageDealt
		);
	}

	@Override
	public void ParseData()
	{
		Collections.shuffle(GetTeamList().get(0).GetSpawns());

		_powerUpManager = new PowerUpManager(this, UtilMath.random, WorldData.GetDataLocs("RED"));
	}

	public PowerUpManager getPowerUpManager()
	{
		return _powerUpManager;
	}

	@EventHandler
	public void onSpawnNpcs(GamePrepareCountdownCommence event)
	{
		if (event.GetGame() == this)
		{
			for (int i = 0; i < 200; i++)				
				getNpcManager().spawnNpc();
		} 
	}

	/*
	@EventHandler(ignoreCancelled = true)
	public void onPlayerAttackWithSword(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;

		Player damager = (Player) event.getDamager();

		if (GetTeam(damager) == null)
			return;

		if (damager.getItemInHand() == null)
			return;

		Double damage = SWORD_DAMAGE.get(damager.getItemInHand().getType());

		if (damage == null)
			return;

		event.setDamage(damage);
	}
	*/

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerAttackWithSword(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)
			return;

		if (!IsAlive(damager))
			return;

		if (damager.getItemInHand() == null)
			return;

		Double damage = SWORD_DAMAGE.get(damager.getItemInHand().getType());

		if (damage == null)
			return;

		event.AddMod(GetName(), "Sword Damage", damage - event.GetDamage(), false);
	}

	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (event.GetLog().GetKiller() == null)
			return;

		if (!event.GetLog().GetKiller().IsPlayer())
			return;

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (player == null)
			return;

		player.setMaxHealth(player.getMaxHealth() + 2);
		UtilPlayer.health(player, 2);
		
		player.getInventory().addItem(SneakyAssassinKit.SMOKE_BOMB.clone());
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		if (GetState() == GameState.End || GetState() == GameState.Dead)
			return;

		Scoreboard.reset();

		Scoreboard.writeNewLine();
		
		GameTeam team = GetTeamList().get(0);

		if (team.GetPlayers(false).size() < 13)
		{
			Scoreboard.writeGroup(team.GetPlayers(false), player ->
			{
				if (team.IsAlive(player))
				{
					return Pair.create(C.cGreen + player.getName(), getPowerUpManager().getPowerUpCount(player));
				}
				else
				{
					return Pair.create(C.cGray + player.getName(), getPowerUpManager().getPowerUpCount(player));
				}
			}, true);
		}
		else if (team.GetPlayers(true).size() < 13)
		{
			Scoreboard.writeGroup(team.GetPlayers(true), player -> Pair.create(C.cGreen + player.getName(), getPowerUpManager().getPowerUpCount(player)), true);
		}
		else
		{
			Scoreboard.write(C.cGreen + "Players Alive");
			Scoreboard.write("" + team.GetPlayers(true).size());

			Scoreboard.writeNewLine();
			Scoreboard.write(C.cRed + "Players Dead");
			Scoreboard.write("" + (team.GetPlayers(false).size() - team.GetPlayers(true).size()));
		}

		GetScoreboard().writeNewLine();
		GetScoreboard().write(C.cYellow + "Player Reveal");
		GetScoreboard().write(String.valueOf(Math.max(0, _revealCountdown)) + (_revealCountdown == 1 ? " Second" : " Seconds"));
		GetScoreboard().draw();
	}

	@EventHandler
	public void onRevealCountdown(UpdateEvent event)
	{
		if (GetState() == GameState.Live && event.getType() == UpdateType.SEC)
		{
			_revealCountdown--;

			if (_revealCountdown <= 3 && _revealCountdown > 0)
			{
				this.Announce(F.main("Game", C.cYellow + C.Bold + "Players Revealed in " + _revealCountdown + " Seconds"));
			}
			else if (_revealCountdown == 0)
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (IsAlive(player))
					{
						Manager.GetDisguise().undisguise(player);
						player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 0));
					}
				}
				
				this.Announce(F.main("Game", C.cRed + C.Bold + "Players Revealed!"));
			}
			else if (_revealCountdown == -4)
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (IsAlive(player))
					{
						Manager.GetDisguise().disguise(DisguiseFactory.createDisguise(player, _disguiseType));
						player.removePotionEffect(PotionEffectType.WITHER);
					}
				}

				_revealCountdown = 60;
				
				this.Announce(F.main("Game", C.cGreen + C.Bold + "Players Disguised!"));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Villager)
		{
			event.setCancelled(true);
			
			if (UtilGear.isMat(event.getPlayer().getItemInHand(), Material.EMERALD))
			{
				if (!Recharge.Instance.use(event.getPlayer(), "Bribe Villager", 8000, true, true))
					return;
				
				_npcManager.setBribed(event.getRightClicked(), event.getPlayer());
				
				UtilInv.remove(event.getPlayer(), Material.EMERALD, (byte)0, 1);
				
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You used " + F.elem("Bribe Villager") + "."));
				
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.VILLAGER_YES, 1f, 1f);
				
				UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, event.getRightClicked().getLocation().add(0, 1, 0), 0.3f, 0.5f, 0.3f, 0, 10,
						ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractHanging(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Hanging)
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onWitherEffectDamager(CustomDamageEvent event)
	{
		if (event.GetCause() == EntityDamageEvent.DamageCause.WITHER)
			event.SetCancelled("Wither damage disabled");
	}

	public NpcManager getNpcManager()
	{
		return _npcManager;
	}
	
	@EventHandler
	public void unregisterListeners(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End || event.GetState() == GameState.Dead)
		{
			HandlerList.unregisterAll(_npcManager);
			HandlerList.unregisterAll(_powerUpManager);
			_npcManager = null;
			_powerUpManager = null;
		}
	}
}
