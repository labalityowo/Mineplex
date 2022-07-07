package nautilus.game.arcade.game.games.barbarians;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.barbarians.kits.*;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.BlockBreakStatTracker;

public class Barbarians extends SoloGame
{
	public Barbarians(ArcadeManager manager) 
	{
		super(manager, GameType.Barbarians,

				new Kit[]
						{
				new KitBrute(manager),
				new KitArcher(manager),
				new KitBomber(manager),
						},

						new String[]
								{
				"Free for all fight to the death!",
				"Wooden blocks are breakable.",
				"Attack people to restore hunger!",
				"Last player alive wins!"
								});
	
		this.DamageTeamSelf = true;

		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);
		
		this.BlockBreakAllow.add(5);
		this.BlockBreakAllow.add(17);
		this.BlockBreakAllow.add(18);
		this.BlockBreakAllow.add(20);
		this.BlockBreakAllow.add(30);
		this.BlockBreakAllow.add(47);
		this.BlockBreakAllow.add(53);
		this.BlockBreakAllow.add(54);
		this.BlockBreakAllow.add(58);
		this.BlockBreakAllow.add(64);
		this.BlockBreakAllow.add(83);
		this.BlockBreakAllow.add(85);
		this.BlockBreakAllow.add(96);
		this.BlockBreakAllow.add(125);
		this.BlockBreakAllow.add(126);
		this.BlockBreakAllow.add(134);
		this.BlockBreakAllow.add(135);
		this.BlockBreakAllow.add(136);

		registerStatTrackers(
				new BlockBreakStatTracker(this, true)
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
				new ChatStatData("BlockBreak", "Blocks Broken", true)
		);
	}
	
	@EventHandler
	public void BlockDamage(BlockDamageEvent event)
	{
		event.setInstaBreak(true);
	}
	
	@EventHandler
	public void ItemSpawn(ItemSpawnEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void Hunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;
		
		if (!IsLive())
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (player.getFoodLevel() <= 0)
			{
				Manager.GetDamage().NewDamageEvent(player, null, null, 
						DamageCause.STARVATION, 1, false, true, false,
						"Starvation", GetName());
			}
			
			UtilPlayer.hunger(player, -2);
		}
	}
	
	@EventHandler
	public void HungerRestore(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);
		if (damager != null)
			UtilPlayer.hunger(damager, 2);
	}
}
