package nautilus.game.arcade.game.games.champions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.champions.kits.KitAssassin;
import nautilus.game.arcade.game.games.champions.kits.KitBrute;
import nautilus.game.arcade.game.games.champions.kits.KitKnight;
import nautilus.game.arcade.game.games.champions.kits.KitMage;
import nautilus.game.arcade.game.games.champions.kits.KitRanger;
import nautilus.game.arcade.game.games.common.CaptureTheFlag;
import nautilus.game.arcade.game.modules.ChampionsModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.CapturesStatTracker;
import nautilus.game.arcade.stats.ClutchStatTracker;
import nautilus.game.arcade.stats.ElectrocutionStatTracker;
import nautilus.game.arcade.stats.KillReasonStatTracker;
import nautilus.game.arcade.stats.SeismicSlamStatTracker;
import nautilus.game.arcade.stats.SpecialWinStatTracker;
import nautilus.game.arcade.stats.TheLongestShotStatTracker;

public class ChampionsCTF extends CaptureTheFlag
{
	public ChampionsCTF(ArcadeManager manager)
	{
		super(manager, GameType.ChampionsCTF,

				new Kit[]
						{
				new KitBrute(manager),
				new KitRanger(manager),
				new KitKnight(manager),
				new KitMage(manager),
				new KitAssassin(manager),
						});

		_help = new String[]
				{
				"Make sure you use all of your Skill/Item Tokens",
				"Collect Resupply Chests to restock your inventory",
				"Customize your Class to suit your play style",
				"Gold Sword boosts Sword Skill by 2 Levels",
				"Gold Axe boosts Axe Skill by 2 Levels",
				"Gold/Iron Weapons deal 6 damage",
				"Diamond Weapons deal 7 damage",
				  
				};

		Manager.GetDamage().UseSimpleWeaponDamage = false;

		Manager.getClassManager().GetItemFactory().getProximityManager().setProxyLimit(6);
		
		InventoryOpenChest = true;
		
		EloStart = 1000;

		this.DisableKillCommand = false;

		AllowParticles = false;

		registerStatTrackers(
				new KillReasonStatTracker(this, "Backstab", "Assassination", false),
				new ElectrocutionStatTracker(this),
				new TheLongestShotStatTracker(this),
				new SeismicSlamStatTracker(this),
				new CapturesStatTracker(this, "Captures"),
				new ClutchStatTracker(this, "Clutch"),
				new SpecialWinStatTracker(this, "SpecialWin")
		);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageDealt,
				DamageTaken,
				BlankLine,
				new ChatStatData("Captures", "Flag Captures", true)
		);
		
		new ChampionsModule().register(this);
	} 
	
	@Override    
	public void ValidateKit(Player player, GameTeam team)
	{ 
		//Set to Default Knight
		if (GetKit(player) == null)
		{
			SetKit(player, GetKits()[2], true);
			UtilPlayer.closeInventoryIfOpen(player);
		}
	}
	
	@Override
	public DeathMessageType GetDeathMessageType()
	{
		return DeathMessageType.Detailed;
	}
	
	@EventHandler
	public void cleanProximities(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		for (Location loc : getLocations(true))
		{
			Manager.getClassManager().GetItemFactory().getProximityManager().clean(loc, 12);
		}
	}
}