package mineplex.game.clans.clans.nether.miniboss.bosses;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.amplifiers.AmplifierManager;
import mineplex.game.clans.clans.nether.miniboss.MinibossFireball;
import mineplex.game.clans.clans.nether.miniboss.NetherMiniBoss;
import mineplex.game.clans.items.runes.RuneManager.RuneAttribute;

/**
 * Class for running an individual Ghast miniboss
 */
public class GhastMiniboss extends NetherMiniBoss<Ghast>
{
	private static final long MAIN_FIREBALL_COOLDOWN = 5000;
	private static final long FIREBALL_LAUNCH_RATE = 500;
	private static final int FIREBALLS_PER_USE = 5;
	private static final double RUNE_DROP_CHANCE = .02;
	private static final int MAX_VALUABLE_DROPS = 5;
	private static final Material[] VALUABLE_DROP_TYPES = new Material[] {Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.LEATHER};
	private static final double MAX_TARGET_RANGE = 25;
	private static final Material[] SET_DROP_TYPES = new Material[] {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS};
	private static final double SET_DROP_CHANCE = .02;
	private long _lastFireballUse;
	private int _fireballsRemaining;
	
	public GhastMiniboss(String displayName, Double maxHealth, Location spawn, EntityType type)
	{
		super(displayName, maxHealth, spawn, type);
	}
	
	private void tryFireballVolley()
	{
		if (_fireballsRemaining > 0)
		{
			if (UtilTime.elapsed(_lastFireballUse, FIREBALL_LAUNCH_RATE))
			{
				_fireballsRemaining--;
				_lastFireballUse = System.currentTimeMillis();
				MinibossFireball.launchFireball(getEntity());
			}
		}
		else
		{
			if (UtilTime.elapsed(_lastFireballUse, MAIN_FIREBALL_COOLDOWN))
			{
				_fireballsRemaining = FIREBALLS_PER_USE;
			}
		}
	}
	
	@Override
	public void customSpawn()
	{
		_lastFireballUse = System.currentTimeMillis();
		_fireballsRemaining = 0;
	}
	
	@Override
	public void customDeath(Location deathLocation)
	{
		deathLocation.getWorld().dropItemNaturally(deathLocation, new ItemStack(VALUABLE_DROP_TYPES[UtilMath.r(VALUABLE_DROP_TYPES.length)], UtilMath.r(MAX_VALUABLE_DROPS) + 1));
		double runeDropChance = RUNE_DROP_CHANCE;
		if (ClansManager.getInstance().getAmplifierManager().hasActiveAmplifier())
		{
			runeDropChance *= AmplifierManager.AMPLIFIER_RUNE_DROP_MULTIPLIER;
		}
		if (new Random().nextDouble() <= runeDropChance)
		{
			RuneAttribute runeType = RuneAttribute.values()[UtilMath.r(RuneAttribute.values().length)];
			deathLocation.getWorld().dropItemNaturally(deathLocation, ClansManager.getInstance().getGearManager().getRuneManager().getRune(runeType));
		}
		if (new Random().nextDouble() <= SET_DROP_CHANCE)
		{
			deathLocation.getWorld().dropItemNaturally(deathLocation, new ItemStack(SET_DROP_TYPES[UtilMath.r(SET_DROP_TYPES.length)], 1));
		}
	}
	
	@Override
	public void update()
	{
		Player target = null;
		for (Player test : UtilPlayer.getInRadius(getEntity().getLocation(), MAX_TARGET_RANGE).keySet())
		{
			if (test.getGameMode() == GameMode.SURVIVAL && !ClansManager.getInstance().getIncognitoManager().Get(test).Hidden)
			{
				target = test;
				break;
			}
		}
		if (target != null)
		{
			UtilEnt.LookAt(getEntity(), target.getLocation());
			tryFireballVolley();
		}
	}
	
	@EventHandler
	public void onShoot(ProjectileLaunchEvent event)
	{
		if (event.getEntity().getShooter() != null && event.getEntity().getShooter().equals(getEntity()))
		{
			if (!(event.getEntity() instanceof LargeFireball))
			{
				event.setCancelled(true);
			}
			ClansManager.getInstance().runSyncLater(() ->
			{
				if (event.getEntity() == null || event.getEntity().isDead() || !event.getEntity().isValid())
				{
					return;
				}
				if (!MinibossFireball.isFireball(event.getEntity()))
				{
					event.getEntity().remove();
				}
			}, 1L);
		}
	}
}