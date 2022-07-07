package mineplex.game.clans.clans.worldevent.boss;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.api.EventState;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Blink;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Recall;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

public abstract class BossWorldEvent<T extends EventCreature<?>> extends WorldEvent
{
	private static final double TELEPORT_PAD_RANGE = 1.5;
	private static final long DELAY_TILL_DROP_REWARD = 40;
	
	private T _boss;
	private List<Location> _teleportFrom;
	private List<Location> _teleportTo;
	
	public BossWorldEvent(String name, Location centerLocation, double radius, List<Vector> teleportFrom, List<Vector> teleportTo, DisguiseManager disguiseManager, ProjectileManager projectileManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager)
	{
		super(name, centerLocation, radius, true, disguiseManager, projectileManager, damageManager, blockRestore, conditionManager);
		
		_teleportFrom = teleportFrom.stream().map(vec -> vec.toLocation(centerLocation.getWorld())).collect(Collectors.toList());
		_teleportTo = teleportTo.stream().map(vec -> vec.toLocation(centerLocation.getWorld())).collect(Collectors.toList());
	}
	
	public abstract String getDeathMessage();
	
	public T getBossCreature()
	{
		return _boss;
	}
	
	public void setBossCreature(T boss)
	{
		_boss = boss;
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		if (getState() != EventState.LIVE)
		{
			return;
		}
		if (_teleportFrom.isEmpty() || _teleportTo.isEmpty())
		{
			return;
		}
		for (Location from : _teleportFrom)
		{
			for (Player player : UtilPlayer.getInRadius(from, TELEPORT_PAD_RANGE).keySet())
			{
				if (ClansManager.getInstance().hasTimer(player))
				{
					if (Recharge.Instance.use(player, "PvP Timer Inform NoBoss", 5000, false, false))
					{
						UtilPlayer.message(player, F.main(getName(), "You cannot enter a World Boss whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
					}
					continue;
				}
				player.teleport(UtilMath.randomElement(_teleportTo));
				for (ISkill skill : ClansManager.getInstance().getClassManager().Get(player).GetSkills())
				{
					if (skill instanceof Recall)
					{
						((Recall)skill).Reset(player);
					}
					if (skill instanceof Blink)
					{
						((Blink)skill).Reset(player);
					}
				}
				sendMessage(player, "You have teleported inside the arena!");
			}
		}
	}
	
	@EventHandler
	public void onBossDeath(EventCreatureDeathEvent event)
	{
		if (_boss == null)
		{
			return;
		}
		if (event.getCreature().equals(_boss))
		{
			Location drop = event.getCreature().getLastKnownLocation();
			UtilServer.CallEvent(new BossDeathEvent(this, drop));
			ClansManager.getInstance().runSyncLater(() ->
			{
				ClansManager.getInstance().getLootManager().dropBoss(drop);
				drop.getWorld().dropItem(drop, new ItemBuilder(Material.IRON_INGOT).setTitle(C.cDRedB + "Old Silver Token").setLore(C.cRed + "This token pulses with an evil aura.").setGlow(true).build());
			}, DELAY_TILL_DROP_REWARD);
			Bukkit.broadcastMessage(getDeathMessage());
			stop();
		}
	}
}