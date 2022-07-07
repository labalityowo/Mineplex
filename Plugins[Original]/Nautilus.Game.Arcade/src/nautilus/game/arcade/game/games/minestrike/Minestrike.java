package nautilus.game.arcade.game.games.minestrike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.mineplex.anticheat.checks.combat.KillauraTypeA;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticManager;
import mineplex.core.gadget.gadgets.gamemodifiers.minestrike.GameModifierMineStrikeSkin;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.minestrike.GunModule.RoundOverEvent;
import nautilus.game.arcade.game.games.minestrike.data.Bomb;
import nautilus.game.arcade.game.games.minestrike.items.guns.Gun;
import nautilus.game.arcade.game.games.minestrike.items.guns.GunStats;
import nautilus.game.arcade.game.games.minestrike.kits.KitPlayer;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.KaboomStatTracker;
import nautilus.game.arcade.stats.KillAllOpposingMineStrikeRoundStatTracker;
import nautilus.game.arcade.stats.KillFastStatTracker;
import nautilus.game.arcade.stats.KillReasonStatTracker;
import nautilus.game.arcade.stats.KillsWithConditionStatTracker;
import nautilus.game.arcade.stats.MineStrikeGunStats;
import nautilus.game.arcade.stats.MineStrikeLastAliveKillStatTracker;
import nautilus.game.arcade.stats.TeamDeathsStatTracker;
import nautilus.game.arcade.stats.TeamKillsStatTracker;

/**
 * Minestrike
 *
 * @author xXVevzZXx
 */
public class Minestrike extends TeamGame
{

	//Managers
	private ShopManager _shopManager;

	//Data
	private int _roundsToWin = 8;
	private long _roundTime = 120000;
	
	//Round Data
	private String _winText = null;
	private boolean _roundOver = false;

	//Money Data
	private boolean _bombPlanted = false;
	private boolean _ctWonLast = false;
	private int _winStreak = 0;

	private HashMap<GameTeam, Integer> _score = new HashMap<GameTeam, Integer>();

	//Map Data
	private ArrayList<Location> _bombSites;

	private Player _bombPlanter;
	private Player _bombDefuser;
	private Player _bombPlantedBy;

	private boolean _bombScoreboardFlash = false;
	
	private GunModule _gunModule;
	
	//Scoreboard
	private Objective _scoreObj;
	
	public Minestrike(ArcadeManager manager, Kit[] kits, GameType type)
	{
		super(manager, type, kits, 
				new String[]
				{
						C.cAqua + "SWAT" + C.cWhite + "  Defend the Bomb Sites",
						C.cAqua + "SWAT" + C.cWhite + "  Kill the Bombers",
						" ",
						C.cRed + "Bombers" + C.cWhite + "  Plant the Bomb at Bomb Site",
						C.cRed + "Bombers" + C.cWhite + "  Kill the SWAT Team",
				});

		Manager.getCosmeticManager().setHideParticles(true);

		this.StrictAntiHack = true;

		AnnounceStay = false;

		this.HungerSet = 20;

		this.ItemDrop = true;

		this.DeathTeleport = false;

		this.InventoryClick = true;

		this.JoinInProgress = true;

		this.AllowEntitySpectate = false;

		this.AllowParticles = false;

		this.PlayerGameMode = GameMode.ADVENTURE;
		
		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(KillauraTypeA.class);
		
		_scoreObj = Scoreboard.getScoreboard().registerNewObjective("HP", "dummy");
		_scoreObj.setDisplaySlot(DisplaySlot.BELOW_NAME);

		_help = new String[]
				{
						//"Tap Crouch when close to an ally to Boost",
						"Open Inventory at spawn to buy guns",
						"Hold Right-Click to Plant Bomb",
						"Look at the Bomb to Defuse it",
						"Moving decreases accuracy",
						"Sprinting heavily decreases accuracy",
						"Jumping massively decreases accuracy",
						"Crouching increases accuracy",
						"Left-Click to drop Grenades",
						"Right-Click to throw Grenades",
						"Burst Fire for greater accuracy",
						"Sniper Rifles are only accurate while scoped",
						"Rifles have 30% recoil reduction while scoped",
						"Pick up better weapons from dead players"
				};

		_gunModule = new GunModule(this);
		_shopManager = new ShopManager(this);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}
	
	public Minestrike(ArcadeManager manager)
	{
		this(manager, new Kit[]{new KitPlayer(manager)}, GameType.MineStrike);

		registerStatTrackers(
				new KillReasonStatTracker(this, "Headshot", "BoomHeadshot", true),
				new KillAllOpposingMineStrikeRoundStatTracker(this),
				new KaboomStatTracker(this),
				new KillReasonStatTracker(this, "Backstab", "Assassination", false),
				new MineStrikeLastAliveKillStatTracker(this),
				new KillFastStatTracker(this, 4, 5, "KillingSpree"),
				new KillsWithConditionStatTracker(this, "Blindfolded", ConditionType.BLINDNESS, "Flash Bang", 2),

				new TeamDeathsStatTracker(this),
				new TeamKillsStatTracker(this),
				
				new MineStrikeGunStats(this)
		);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				new ChatStatData("BoomHeadshot", "Headshots", true),
				Assists
		);
	}

	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;
		
		this.GetTeamList().get(0).SetColor(ChatColor.AQUA);
		this.GetTeamList().get(0).SetName("SWAT");

		this.GetTeamList().get(1).SetColor(ChatColor.RED);
		this.GetTeamList().get(1).SetName("Bombers");
	}
	
	@EventHandler
	public void SetScoreboardNameVisibility(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;

		hideNametags();
	}

	private void hideNametags() {
		for (Team curTeam : Scoreboard.getScoreboard().getTeams())
		{
			curTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
		}
	}

	@Override
	public void ParseData() 
	{
		_bombSites = WorldData.GetDataLocs("RED");
	}

	@EventHandler
	public void giveStartEquipment(PlayerKitGiveEvent event)
	{
		GameTeam team = GetTeam(event.getPlayer());
		if (team == null)
			return;

		GameCosmeticManager cosmeticManager = Manager.getCosmeticManager().getGadgetManager().getGameCosmeticManager();
		GameModifierMineStrikeSkin knifeSkin = (GameModifierMineStrikeSkin) cosmeticManager.getActiveCosmetic(event.getPlayer(), GameDisplay.MineStrike, "Knife");
		
		Material mat = Material.IRON_AXE;
		byte data = 0;
		String name = "Knife";
		
		if(knifeSkin != null)
		{
			mat = knifeSkin.getSkinMaterial();
			data = knifeSkin.getSkinData();
			name = knifeSkin.getName();
		}
		
		ItemStack knife = ItemStackFactory.Instance.CreateStack(mat, data, 1, name);

		if (team.GetColor() == ChatColor.RED)
		{
			if (IsAlive(event.getPlayer()))
			{
				event.getPlayer().setGameMode(GameMode.ADVENTURE);
				//Pistol
				Gun gun = new Gun(GunStats.GLOCK_18, _gunModule);
				_gunModule.registerGun(gun, event.getPlayer());
				gun.giveToPlayer(event.getPlayer(), true);
				gun.updateSkin(event.getPlayer(), getArcadeManager().getCosmeticManager().getGadgetManager());
				gun.updateWeaponName(event.getPlayer(), _gunModule);

				//Knife
				if(knifeSkin == null) knife.setType(Material.IRON_AXE);
				
				event.getPlayer().getInventory().setItem(2, knife);	

				//Armor
				giveTeamArmor(event.getPlayer(), Color.fromRGB(255, 75, 75));
			}
		}
		else if (team.GetColor() == ChatColor.AQUA)
		{
			if (IsAlive(event.getPlayer()))
			{
				event.getPlayer().setGameMode(GameMode.ADVENTURE);
				//Pistol
				Gun gun = new Gun(GunStats.P2000, _gunModule);
				_gunModule.registerGun(gun, event.getPlayer());
				gun.giveToPlayer(event.getPlayer(), true);
				gun.updateWeaponName(event.getPlayer(), _gunModule);
				gun.updateSkin(event.getPlayer(), getArcadeManager().getCosmeticManager().getGadgetManager());

				//Knife
				if(knifeSkin == null) knife.setType(Material.IRON_SWORD);
				
				event.getPlayer().getInventory().setItem(2, knife);

				//Armor
				giveTeamArmor(event.getPlayer(), Color.fromRGB(125, 200, 255));
			}
		}

		//Enter Shop
		_shopManager.enterShop(event.getPlayer());
	}

	public void giveTeamArmor(Player player, Color color)
	{
		if (_shopManager.isDisabled())
		{
			ItemStack armor = new ItemStack(Material.LEATHER_HELMET);
			LeatherArmorMeta meta = (LeatherArmorMeta)armor.getItemMeta();
			meta.setColor(color);
			armor.setItemMeta(meta);
			player.getInventory().setHelmet(armor);
		}
		
		ItemStack armor = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta)armor.getItemMeta();
		meta.setColor(color);
		armor.setItemMeta(meta);
		player.getInventory().setChestplate(armor);

		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta metaLegs = (LeatherArmorMeta)legs.getItemMeta();
		metaLegs.setColor(color);
		legs.setItemMeta(metaLegs);
		player.getInventory().setLeggings(legs);

		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta metaBoots = (LeatherArmorMeta)boots.getItemMeta();
		metaBoots.setColor(color);
		boots.setItemMeta(metaBoots);
		player.getInventory().setBoots(boots);
	}

	@EventHandler
	public void shopInventoryClick(InventoryClickEvent event)
	{
		_shopManager.inventoryClick(event);
	}

	@EventHandler
	public void shopUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_shopManager.update();
	}

	@EventHandler
	public void quitClean(PlayerQuitEvent event)
	{
		_shopManager.leaveShop(event.getPlayer(), false, true);
		_gunModule.undisguise(event.getPlayer());
		_gunModule.removeScope(event.getPlayer());
		_gunModule.dropInventory(event.getPlayer());
	}

	@EventHandler
	public void giveBombInitial(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;

		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				Player player = UtilAlg.Random(GetTeam(ChatColor.RED).GetPlayers(true));

				_gunModule.giveBombToPlayer(player);
			}
		}, 40);
	}


	@EventHandler
	public void killReward(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		Player killed = (Player)event.GetEvent().getEntity();

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			if (killer == null || killer.equals(killed))
				return;

			if (GetTeam(killed).equals(GetTeam(killer)))
				return;

			int amount;

			String gunType = (String) event.GetLog().GetLastDamager().GetDamage().getFirst().getMetadata().get("gunType");
			if (gunType == null)
			{
				amount = 300;
			}
			else
			{
				switch (gunType)
				{
					case "AWP":
						amount = 100;
						break;
					case "PPBIZON":
						amount = 600;
						break;
					case "NOVA":
						amount = 900;
						break;
					case "KNIFE":
						amount = 1500;
						break;
					default:
						amount = 300;
				}
			}

			_shopManager.addMoney(killer, amount, "kill with " + event.GetLog().GetLastDamager().GetReason());
			
			/*
			if(event.GetLog().GetLastDamager().GetReason().contains("Knife"))
			{
				GadgetManager gadgetManager = Manager.getCosmeticManager().getGadgetManager(); 
				GameModifierMineStrikeSkin knifeSkin = (GameModifierMineStrikeSkin) gadgetManager.getActiveGameModifier(event.getPlayer(), 
						GameModifierType.MineStrike, GameModifierMineStrikeSkin.getWeaponFilter("Knife"));
				
				if(knifeSkin != null)
				{
					int kills = GetStats().get(killer).get("")
					ItemStack item = killer.getInventory().getItem(2);
					ItemMeta im = item.getItemMeta();
					im.setDisplayName(C.cYellow + C.Bold + knifeSkin.GetName() + " - Kills: " + kills);
					
					AddStat(killer, "Knife." + knifeSkin.GetName() + ".Kills", 1, false, false);
				}
			}
			*/
		}
	}

	@EventHandler
	public void plantBomb(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!UtilGear.isMat(player.getItemInHand(), Material.GOLD_SWORD))
			return;

		// Fixed bomb able to be planted after the round is over.
		if (_roundOver)
		{
			UtilPlayer.message(player, F.main("Game", "You cannot plant the bomb once the round is over."));
			event.setCancelled(true);
			return;
		}
		
		// Added a small tip for players that are trying to right-click with the bomb.
		if (UtilEvent.isAction(event, ActionType.L))
		{
			UtilPlayer.message(player, F.main("Game", "You must " + F.elem("Right-Click") + " while holding the bomb to plant it."));
			event.setCancelled(true);
			return;
		}

		if (!UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Game", "You can only plant the bomb on the ground."));
			event.setCancelled(true);
			return;
		}

		//Should never occur with 1 Bomb
		if (_bombPlanter != null)
		{
			UtilPlayer.message(player, F.main("Game", "Someone else is planting the bomb."));
			event.setCancelled(true);
			return;
		}

		//Check Bomb Sites
		boolean near = false;
		for (Location loc : _bombSites)
		{
			if (UtilMath.offset(player.getLocation(), loc) < 5)
			{
				near = true;
				break;
			}
		}

		//Too Far
		if (!near)
		{
			UtilPlayer.message(player, F.main("Game", "You can only plant the bomb at a bomb site."));
			event.setCancelled(true);
			return;
		}

		_bombPlanter = player;
		_bombPlanter.setExp(0f);

		UtilPlayer.message(player, F.main("Game", "You are now placing the bomb."));

		//Radio
		_gunModule.playSound(Radio.T_BOMB_PLANT, null, GetTeam(_bombPlanter));
	}

	@EventHandler
	public void plantBombUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		// Added to check if the round is over when a bomb is being planted.
		if (_roundOver) 
			return;

		if (_bombPlanter == null)
			return;

		if (!_bombPlanter.isBlocking() || !_bombPlanter.isOnline())
		{
			_bombPlanter.setExp(0f);
			UtilTextMiddle.clear(_bombPlanter);
			_bombPlanter = null;
			return;
		}

		_bombPlanter.setExp(Math.min(_bombPlanter.getExp() + 0.017f, 0.99999f));

		if (Math.random() > 0.90)
			_bombPlanter.getWorld().playSound(_bombPlanter.getLocation(), Sound.NOTE_PLING, 2f, 3f);

		UtilTextMiddle.display(C.cRed + C.Bold + "Planting Bomb", UtilTextMiddle.progress(_bombPlanter.getExp()), 0, 10, 0, _bombPlanter);

		if (_bombPlanter.getExp() >= 0.98f)
		{
			_gunModule.setBomb(new Bomb(_bombPlanter));

			_shopManager.addMoney(_bombPlanter, 300, "planting the bomb");

			Announce(C.cRed + C.Bold + _bombPlanter.getName() + " has planted the bomb!");

			_bombPlantedBy = _bombPlanter;

			_bombPlanter.setExp(0f);
			_bombPlanter.setItemInHand(null);
			_bombPlanter = null;
			_gunModule.setBombHolder(null);

			//Sound
			_gunModule.playSound(Radio.BOMB_PLANT, null, null);

			//Title
			UtilTextMiddle.display(null, C.cRed + C.Bold + "Bomb has been planted!", 10, 50, 10);
		}
	}

	@EventHandler
	public void plantDefuseBombRestrictMovement(PlayerMoveEvent event)
	{
		if (_bombPlanter != null && _bombPlanter.equals(event.getPlayer()))
			if (UtilMath.offset(event.getFrom(), event.getTo()) > 0)
				event.setTo(event.getFrom());

		//		if (_bombDefuser != null && _bombDefuser.equals(event.getPlayer()))
		//			if (UtilMath.offset(event.getFrom(), event.getTo()) > 0)
		//				event.setTo(event.getFrom());
	}

	@EventHandler
	public void defuseKitMessage(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!UtilGear.isMat(player.getItemInHand(), Material.SHEARS))
			return;

		UtilPlayer.message(player, F.main("Game", "Look at the Bomb to defuse it."));
	}

	public void startDefuse()
	{
		for (Player player : GetTeam(ChatColor.AQUA).GetPlayers(true))
		{
			HashSet<Material> ignoreBlocks = new HashSet<Material>();
			ignoreBlocks.add(Material.AIR);
			ignoreBlocks.add(Material.PORTAL);
			
			Block block = player.getTargetBlock(ignoreBlocks, 5);

			if (block == null || !_gunModule.getBomb().isBlock(block))
				continue;
	
			if (UtilMath.offset(player.getLocation(), block.getLocation().add(0.5, 0, 0.5)) > 3)
				continue;

			if (_bombDefuser != null)
			{
				if (Recharge.Instance.use(player, "Defuse Message", 2000, false, false))
					UtilPlayer.message(player, F.main("Game", _bombDefuser.getName() + " is already defusing the Bomb."));

				continue;
			}

			_bombDefuser = player;
			_bombDefuser.setExp(0f);

			UtilPlayer.message(player, F.main("Game", "You are defusing the Bomb."));

			_bombDefuser.getWorld().playSound(_bombDefuser.getLocation(), Sound.PISTON_RETRACT, 2f, 1f);
		}
	}

	@EventHandler
	public void defuseBombUpdate(UpdateEvent event)
	{
		if (_gunModule.getBomb() == null)
			return;

		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		if (_bombDefuser == null)
		{
			startDefuse();
		}

		if (_bombDefuser == null)
			return;

		HashSet<Material> ignoreBlocks = new HashSet<Material>();
		ignoreBlocks.add(Material.AIR);
		ignoreBlocks.add(Material.PORTAL);
		
		Block block = _bombDefuser.getTargetBlock(ignoreBlocks, 5);
		
		if (!IsAlive(_bombDefuser) || block == null || !_gunModule.getBomb().isBlock(block)  || !_bombDefuser.isOnline() || UtilMath.offset(_bombDefuser.getLocation(), block.getLocation().add(0.5, 0, 0.5)) > 3)
		{
			_bombDefuser.setExp(0f);
			_bombDefuser = null;			
			return;
		}
		
		//Kit or Not?
		float defuseRate = 0.005f;
		if (UtilGear.isMat(_bombDefuser.getInventory().getItem(8), Material.SHEARS))
			defuseRate = 0.01f;

		_bombDefuser.setExp(Math.min(_bombDefuser.getExp() + defuseRate, 0.99999f));

		UtilTextMiddle.display(C.cAqua + C.Bold + "Defusing Bomb", UtilTextMiddle.progress(_bombDefuser.getExp()), 0, 10, 0, _bombDefuser);

		if (_bombDefuser.getExp() >= 0.98f)
		{
			_gunModule.getBomb().defuse();

			_winText = _bombDefuser.getName() + " defused the bomb!";

			_gunModule.setBomb(null);
			_bombDefuser.setExp(0f);
			_bombDefuser = null;

			//Sound
			_gunModule.playSound(Radio.BOMB_DEFUSE, null, null);

			setWinner(GetTeam(ChatColor.AQUA), true);
		}
	}

	@EventHandler
	public void bombUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		if (_gunModule.getBomb() == null)
			return;

		if (!_gunModule.getBomb().update())
			return;

		/*
		Set<Block> blocks = UtilBlock.getInRadius(_bomb.Block.getLocation(), 10d).keySet();

		Iterator<Block> blockIterator = blocks.iterator();
		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();

			if (block.getY() < 2)
				blockIterator.remove();
		}

		Manager.GetExplosion().BlockExplosion(blocks, _bomb.Block.getLocation(), false);
		 */


		HashMap<Player, Double> players = UtilPlayer.getInRadius(_gunModule.getBomb().Block.getLocation(), 48);
		for (Player player : players.keySet())
		{
			if (!IsAlive(player))
				continue;

			// Damage Event
			Manager.GetDamage().NewDamageEvent(player, null, null,
					DamageCause.CUSTOM, 1 + (players.get(player) * 40),
					true, true, false, "Bomb", "C4 Explosion");
		}


		_gunModule.setBomb(null);

		_winText = _bombPlantedBy.getName() + " destroyed the bomb site!";

		setWinner(GetTeam(ChatColor.RED), false);
	}

	public int getScore(GameTeam team)
	{
		if (!_score.containsKey(team))
			_score.put(team, 0);

		return _score.get(team);
	}

	public void addScore(GameTeam team)
	{
		_score.put(team, getScore(team) + 1);
	}

	@EventHandler
	public void roundTimerUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		if (_gunModule.getBomb() != null)
			return;

		if (UtilTime.elapsed(GetStateTime(), _roundTime))
		{
			_winText = "Bomb sites were successfully defended!";
			drawScoreboard();
			setWinner(GetTeam(ChatColor.AQUA), false);
		}
	}

	@EventHandler
	public void roundPlayerCheck(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : this.GetTeamList())
			if (team.GetPlayers(true).size() > 0)
				teamsAlive.add(team);

		if (teamsAlive.size() == 1)
		{
			//Bomb Planted - CT cannot win without defusing
			if (_gunModule.getBomb() != null)
			{
				if (teamsAlive.size() > 0)
				{
					if (teamsAlive.get(0).GetColor() == ChatColor.AQUA)
					{
						return;
					}
				}
			}

			setWinner(teamsAlive.get(0), false);
		}
		else if (teamsAlive.size() == 0)
		{
			if (_gunModule.getBomb() == null)
			{
				_winText = "Bomb sites were successfully defended!";
				setWinner(GetTeam(ChatColor.AQUA), false);
			}
			else
			{
				_winText = "Bomb site will be destroyed!";
				setWinner(GetTeam(ChatColor.RED), false);
			}
		}
	}

	public void setWinner(final GameTeam winner, boolean defuse)
	{
		if (_roundOver)
			return;

		Bukkit.getPluginManager().callEvent(new RoundOverEvent(this));

		_roundOver = true;

		String winnerLine = C.Bold + "The round was a draw!";
		ChatColor color = ChatColor.GRAY;
		if (winner != null)
		{
			if(winner.GetName().contains("Bombers")) {
				winnerLine= winner.GetColor() + C.Bold + winner.GetName() + " have won the round!";
			} else {
				winnerLine= winner.GetColor() + C.Bold + winner.GetName() + " has won the round!";
			}
			addScore(winner);
			drawScoreboard();
			color = winner.GetColor();


		}

		//Sound
		if (winner != null)
		{
			UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					if (winner.GetColor() == ChatColor.RED)
						_gunModule.playSound(Radio.T_WIN, null, null);
					else
						_gunModule.playSound(Radio.CT_WIN, null, null);
				}
			}, defuse ? 60 : 0);
		}

		//Record Streak for Money
		if (winner.GetColor() == ChatColor.RED)
		{
			_winStreak++;

			if (_ctWonLast)
			{
				_ctWonLast = false;
				_winStreak = 0;
			}
		}
		else
		{
			_winStreak++;

			if (!_ctWonLast)
			{
				_ctWonLast = true;
				_winStreak = 0;
			}
		}

		//Announce
		Announce("", false);
		Announce(color + "===================================", false);
		Announce("", false);
		Announce(winnerLine, false);
		if (_winText != null)
			Announce(_winText, false);
		Announce("", false);
		Announce(color + "===================================", false);

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
			_gunModule.removeScope(player);
			
			Recharge.Instance.Reset(player, "reload");
		}

		UtilTextMiddle.display(null, winnerLine, 20, 120, 20);

		//Check for total game win
		EndCheck();

		//Next Round (if not over)
		if (IsLive())
		{
			UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					restartRound();
				}
			}, 100);
		}
	}

	public void restartRound()
	{
		giveMoney();

		//Clean
		resetGame();

		//Teleport to Spawn
		for (GameTeam team : GetTeamList())
			team.SpawnTeleport(false);

		//Revive Dead Players
		for (Player player : GetPlayers(false))
			if (!IsAlive(player))
			{
				SetPlayerState(player, PlayerState.IN);

				GameTeam team = GetTeam(player);

				//Teleport
				team.SpawnTeleport(player);

				Manager.Clear(player);
				UtilInv.Clear(player);

				ValidateKit(player, GetTeam(player));

				if (GetKit(player) != null)
					GetKit(player).ApplyKit(player);		
			}

		//Remove Scope
		for (Player player : GetPlayers(false))
			_gunModule.removeScope(player);
		
		//Get Hit By Bullets
		for (Player player : GetPlayers(false))
			((CraftPlayer) player).getHandle().spectating = false;

		//Prepare Sound
		for (Player player : GetPlayers(false))
		{
			player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1f, 2f);
			Manager.GetCondition().Factory().Blind("Respawn", player, null, 2, 0, false, false, false);
			
			if (!_shopManager.isDisabled())
			{
				UtilPlayer.message(player, F.main("Game", "You have " + F.elem(C.cGreen + "$" + _shopManager.getMoney(player)) + ". Open your Inventory to spend it."));

				UtilTextMiddle.display(C.cGreen + "$" + _shopManager.getMoney(player), "Open your Inventory to buy new equipment", 10, 120, 10, player);
			}
		}

		//Reset grenades
		_shopManager.resetGrenades();
		
		//Update Scoreboard Teams
		for (GameTeam team : GetTeamList())
			for (Player teamMember : team.GetPlayers(true))
				GetScoreboard().setPlayerTeam(teamMember, team);

		//Alternate Bullets
		if (_gunModule.BulletAlternate)
			_gunModule.BulletInstant = (_gunModule.BulletInstant + 1)%3;

		//Debug Details
		if (_gunModule.Debug)
		{
			Announce(C.cDPurple + C.Bold + "ROUND SETTINGS:");

			if (_gunModule.CustomHitbox)
				Announce(C.cPurple + C.Bold + "Hitbox: " + ChatColor.RESET + "Accurate with Headshots");
			else
				Announce(C.cPurple + C.Bold + "Hitbox: " + ChatColor.RESET + "Default with No Headshot");

			if (_gunModule.BulletInstant == 0)
				Announce(C.cPurple + C.Bold + "Bullets: " + ChatColor.RESET + "Slow and Visible");
			else if (_gunModule.BulletInstant == 1)
				Announce(C.cPurple + C.Bold + "Bullets: " + ChatColor.RESET + "Instant and Invisible");
			else
				Announce(C.cPurple + C.Bold + "Bullets: " + ChatColor.RESET + "Slow and Visible with Instant Sniper");
		}

		hideNametags();
	}

	public void giveMoney()
	{
		if (_ctWonLast)
		{
			int ctMoney = 3250;
			int tMoney = 1400 + (Math.min(4,_winStreak) * 500);

			if (_bombPlanted)
			{
				ctMoney += 250;
				tMoney += 800;
			}

			//Award
			for (Player player : GetTeam(ChatColor.RED).GetPlayers(false))
				_shopManager.addMoney(player, tMoney, "losing the round");

			for (Player player : GetTeam(ChatColor.AQUA).GetPlayers(false))
				_shopManager.addMoney(player, ctMoney, "winning the round");
		}
		else
		{
			int tMoney = 3250;
			int ctMoney = 1400 + (Math.min(4,_winStreak) * 500);

			//Award
			for (Player player : GetTeam(ChatColor.RED).GetPlayers(false))
				_shopManager.addMoney(player, tMoney, "winning the round");

			for (Player player : GetTeam(ChatColor.AQUA).GetPlayers(false))
				_shopManager.addMoney(player, ctMoney, "losing the round");
		}
	}

	public void resetGame()
	{
		//General
		_roundOver = false;
		SetStateTime(System.currentTimeMillis());
		_gunModule.setFreezeTime(10);
		_winText = null;

		//Bomb
		if (_gunModule.getBomb() != null)
			_gunModule.getBomb().clean();

		if (_gunModule.getBombItem() != null)
			_gunModule.getBombItem().remove();

		if (_gunModule.getBombHolder() != null)
		{
			_gunModule.getBombHolder().getInventory().remove(Material.GOLD_SWORD);
			_gunModule.setBombHolder(null);
		}
		
		_gunModule.setBomb(null);
		_gunModule.setBombItem(null);

		_bombPlanter = null;
		_bombDefuser = null;
		_bombPlantedBy = null;
		_bombPlanted = false;

		_gunModule.reset();
		
		//Health
		for (Player player : UtilServer.getPlayers())
			player.setHealth(20);

		//Reset Shop
		for (Player player : UtilServer.getPlayers())
			_shopManager.leaveShop(player, false, false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void specToTeam(PlayerJoinEvent event)
	{
		if (inLobby() || Manager.isVanished(event.getPlayer()))
		{
			return;
		}

		Gadget activeCostume = Manager.getCosmeticManager().getGadgetManager().getActive(event.getPlayer(), GadgetType.COSTUME);
		
		if (activeCostume != null)
		{
			activeCostume.disable(event.getPlayer());
		}
		
		//Target Team
		GameTeam targetTeam = null;
		if (GetTeamList().get(0).GetPlayers(false).size() < GetTeamList().get(1).GetPlayers(false).size())
			targetTeam = GetTeamList().get(0);
		else if (GetTeamList().get(0).GetPlayers(false).size() > GetTeamList().get(1).GetPlayers(false).size())
			targetTeam = GetTeamList().get(1);
		else if (Math.random() > 0.5)
			targetTeam = GetTeamList().get(1);
		else
			targetTeam = GetTeamList().get(0);

		SetPlayerTeam(event.getPlayer(), targetTeam, false);
		
		((CraftPlayer) event.getPlayer()).getHandle().spectating = true;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void quitLeaveTeam(PlayerQuitEvent event)
	{
		GameTeam team = GetTeam(event.getPlayer());

		if (team != null)
		{
			team.RemovePlayer(event.getPlayer());
		}	
	}

	@EventHandler
	public void restartFreezeCountdown(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (_gunModule.getFreezeTime() <= 0)
			return;

		_gunModule.setFreezeTime(_gunModule.getFreezeTime() - 1);

		for (Player player : UtilServer.getPlayers())
		{
			if (_gunModule.getFreezeTime() > 0)
				player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1f, 1f);
			else
				player.playSound(player.getLocation(), Sound.NOTE_SNARE_DRUM, 1f, 1f);
		}

		if (_gunModule.getFreezeTime() == 0)
		{
			//Give Bomb
			Player bombPlayer = UtilAlg.Random(GetTeam(ChatColor.RED).GetPlayers(true));
			_gunModule.giveBombToPlayer(bombPlayer);

			//Sound
			_gunModule.playSound(Radio.CT_START, null, GetTeam(ChatColor.AQUA));
			_gunModule.playSound(Radio.T_START, null, GetTeam(ChatColor.RED));
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event != null && event.getType() != UpdateType.FAST)
			return;

		drawScoreboard();
	}

	public void drawScoreboard()
	{
		Scoreboard.reset();

		for (GameTeam team : this.GetTeamList())
		{

			Scoreboard.writeNewLine();

			Scoreboard.write(getScore(team) + " " + team.GetColor() + C.Bold + team.GetName());
			//Scoreboard.Write(team.GetColor() + "" + getScore(team) + "" + " Wins" + team.GetColor());
			Scoreboard.write(team.GetPlayers(true).size() + "" + " Alive" + team.GetColor());

		}

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGold + C.Bold + "Playing To");
		Scoreboard.write(_roundsToWin + " Wins");

		if (InProgress())
		{
			Scoreboard.writeNewLine();

			if (_gunModule.getBomb() == null)
			{
				Scoreboard.write(C.cGold + C.Bold + "Time Left");
				Scoreboard.write(UtilTime.MakeStr(_roundTime - (System.currentTimeMillis() - this.GetStateTime()), 1));
			}
			else
			{
				if (_bombScoreboardFlash)
					Scoreboard.write(C.cRed + C.Bold + "Bomb Active");
				else
					Scoreboard.write(C.cWhite + C.Bold + "Bomb Active");

				_bombScoreboardFlash = !_bombScoreboardFlash;
			}
		}


		Scoreboard.draw();
	}

	@Override
	public void EndCheck()
	{
		endCheckScore();
		endCheckPlayer();
	}

	public void endCheckScore()
	{
		if (!IsLive())
			return;

		for (GameTeam team : GetTeamList())
		{
			if (getScore(team) >= _roundsToWin)
			{
				//Announce
				AnnounceEnd(team);

				for (GameTeam other : GetTeamList())
				{
					if (WinnerTeam != null && other.equals(WinnerTeam))
					{
						for (Player player : other.GetPlayers(false))
							AddGems(player, 10, "Winning Team", false, false);
					}

					for (Player player : other.GetPlayers(false))
						if (player.isOnline())
							AddGems(player, 10, "Participation", false, false);
				}

				//End
				SetState(GameState.End);
			}
		}
	}

	public void endCheckPlayer()
	{
		if (!IsLive())
			return;

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : this.GetTeamList())
			if (team.GetPlayers(false).size() > 0)
				teamsAlive.add(team);

		if (teamsAlive.size() <= 1)
		{
			//Announce
			if (teamsAlive.size() > 0)
				AnnounceEnd(teamsAlive.get(0));

			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
						AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
						AddGems(player, 10, "Participation", false, false);
			}

			//End
			SetState(GameState.End);
		}
	}

	@EventHandler
	public void healthUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
			_scoreObj.getScore(player.getName()).setScore((int)(player.getHealth() * 5));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void damageHealth(CustomDamageEvent event)
	{
		Player player = event.GetDamagerPlayer(true);
		if (player == null)
			return;

		_scoreObj.getScore(player.getName()).setScore((int)(player.getHealth() * 5));
	}
	
	@EventHandler
	public void debug(PlayerCommandPreprocessEvent event)
	{
		if (!event.getPlayer().isOp())
			return;

		if (event.getMessage().contains("money"))
		{
			_shopManager.addMoney(event.getPlayer(), 16000, "Debug");
			event.setCancelled(true);
		}
	}

	//Used for fire grenade spread
	public int getRound()
	{
		int rounds = 0;
		
		for (int i : _score.values())
			rounds += i;
		
		return rounds;
	}
	
	@Override
	public Location GetSpectatorLocation()
	{
		if (SpectatorSpawn != null)
			return SpectatorSpawn;

		Vector vec = new Vector(0, 0, 0);
		double count = 0;

		for (GameTeam team : this.GetTeamList())
		{
			for (Location spawn : team.GetSpawns())
			{
				count++;
				vec.add(spawn.toVector());
			}
		}

		SpectatorSpawn = new Location(this.WorldData.World, 0, 0, 0);

		vec.multiply(1d / count);

		SpectatorSpawn.setX(vec.getX());
		SpectatorSpawn.setY(vec.getY() + 7); //ADD 7
		SpectatorSpawn.setZ(vec.getZ());
		
		// Move Up - Out Of Blocks
		while (!UtilBlock.airFoliage(SpectatorSpawn.getBlock())
				|| !UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
		{
			SpectatorSpawn.add(0, 1, 0);
		}

		int Up = 0;

		// Move Up - Through Air
		for (int i = 0; i < 15; i++)
		{
			if (UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
			{
				SpectatorSpawn.add(0, 1, 0);
				Up++;
			}
			else
			{
				break;
			}
		}

		// Move Down - Out Of Blocks
		while (Up > 0 && !UtilBlock.airFoliage(SpectatorSpawn.getBlock())
				|| !UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
		{
			SpectatorSpawn.subtract(0, 1, 0);
			Up--;
		}

		SpectatorSpawn = SpectatorSpawn.getBlock().getLocation().add(0.5, 0.1, 0.5);

		while (SpectatorSpawn.getBlock().getTypeId() != 0 || SpectatorSpawn.getBlock().getRelative(BlockFace.UP).getTypeId() != 0)
			SpectatorSpawn.add(0, 1, 0);

		return SpectatorSpawn;
	}
	
	public GunModule getGunModule()
	{
		return _gunModule;
	}
	
	public ShopManager getShopManager()
	{
		return _shopManager;
	}
}