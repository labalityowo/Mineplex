package mineplex.core.bonuses.animations;

import java.util.HashSet;
import java.util.Iterator;

import mineplex.core.bonuses.powerplay.PowerPlayAnimation;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AnimationCarl
{
//	private boolean _isDone;
//	private Block _creeper;
//	private Object _type;
//	private Player _player;
//
//	private HashSet<Item> _items = new HashSet<Item>();
//
//	public AnimationCarl(Entity creeper)
//	{
//		_creeper = creeper.getLocation().getBlock();
//	}
//
//	@Override
//	protected void tick()
//	{
//		if(_type instanceof String)
//		{
//			if(((String) _type).contentEquals("DAILY") || ((String) _type).contentEquals("POLL"))
//			{
//				for (int i = 50; i < 60; i++)
//				{
//					Item gem = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, " " + i));
//					Item shard = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.PRISMARINE_SHARD, (byte) 0, 1, " " + i));
//					_items.add(gem);
//					_items.add(shard);
//
//					Vector vel = new Vector(Math.sin(i * 9/5d), 0, Math.cos(i * 9/5d));
//					UtilAction.velocity(gem, vel, Math.abs(Math.sin(i * 12/3000d)), false, 0, 0.2 + Math.abs(Math.cos(i * 12/3000d))*0.6, 1, false);
//					UtilAction.velocity(shard, vel, Math.abs(Math.sin(UtilMath.r(i) * 30/3000d)), false, 0, 0.2 + Math.abs(Math.cos(UtilMath.r(i) * 30/3000d))*0.6, 1, false);
//
//				}
//			}
//			if(((String) _type).contentEquals("RANK"))
//			{
//				for (int i = 50; i < 60; i++)
//				{
//					Item shard = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.PRISMARINE_SHARD, (byte) 0, 1, " " + i));
//					_items.add(shard);
//
//					Vector vel = new Vector(Math.sin(UtilMath.r(i) * 7/5d), 0, Math.cos(UtilMath.r(i) * 7/5d));
//					UtilAction.velocity(shard, vel, Math.abs(Math.sin(UtilMath.r(i) * 7/3000d)), false, 0, 0.2 + Math.abs(Math.cos(UtilMath.r(i) * 7/3000d))*0.6, 1, false);
//
//				}
//			}
//			if(!((String) _type).contentEquals("DAILY") && !((String) _type).contentEquals("RANK") && !((String) _type).contentEquals("POLL"))
//			{
//
//				Item paper = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.PAPER, (byte) 0, 1, " " + 64));
//				_items.add(paper);
//
//				Vector vel = new Vector(Math.sin(64 * 8/5d), 0, Math.cos(64 * 8/5d));
//				UtilAction.velocity(paper, vel, Math.abs(Math.sin(64 * 9/3000d)), false, 0, 0.2 + Math.abs(Math.cos(64 + 9/3000d))*0.6, 1, false);
//
//				for (int i = 50; i < 60; i++)
//				{
//					Item gem = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, " " + i));
//					_items.add(gem);
//
//					Vector velo = new Vector(Math.sin(i * 8/5d), 0, Math.cos(i * 8/5d));
//					UtilAction.velocity(gem, velo, Math.abs(Math.sin(i * 8/3000d)), false, 0, 0.2 + Math.abs(Math.cos(i + 8/3000d))*0.6, 1, false);
//
//				}
//			}
//			finish();
//		}
//		if(_type instanceof Reward)
//		{
//			if(getTicks() == 0)
//			{
//				RewardData rewardData = ((Reward)_type).getFakeRewardData(_player);
//				ItemStack itemStack = rewardData.getDisplayItem();
//				Item item = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.7, 0.5), itemStack);
//				_items.add(item);
//
//				Vector vel = new Vector(_player.getLocation().getX() - _creeper.getLocation().getX(), 0, _player.getLocation().getZ() - _creeper.getLocation().getZ());
//
//				UtilAction.velocity(item, vel, 0.1, false, 0, 0.2 + 1*0.4, 1, false);
//			}
//
//			if(((Reward)_type).getRarity() == RewardRarity.RARE)
//			{
//				RareAnimation();
//			}
//			else if(((Reward)_type).getRarity() == RewardRarity.LEGENDARY)
//			{
//				LegendAnimation();
//			}
//			else if(((Reward)_type).getRarity() == RewardRarity.MYTHICAL)
//			{
//				MythicalAnimation();
//			}
//			else
//			{
//				finish();
//			}
//		}
//		if (_type instanceof PowerPlayAnimation)
//		{
//			for (int i = 50; i < 65; i++)
//			{
//				// Gem amplifier
//				Item gem = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, " " + i));
//				_items.add(gem);
//
//				Vector vel = new Vector(Math.sin(UtilMath.r(i) * 7/5d), 0, Math.cos(UtilMath.r(i) * 7/5d));
//				UtilAction.velocity(gem, vel, Math.abs(Math.sin(UtilMath.r(i) * 7/3000d)), false, 0, 0.2 + Math.abs(Math.cos(UtilMath.r(i) * 7/3000d))*0.6, 1, false);
//
//				// Omega chest
//				Item omega = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), SkinData.OMEGA_CHEST.getSkull());
//				_items.add(omega);
//
//				vel = new Vector(Math.sin(UtilMath.r(i) * 7/5d), 0, Math.cos(UtilMath.r(i) * 7/5d));
//				UtilAction.velocity(omega, vel, Math.abs(Math.sin(UtilMath.r(i) * 7/3000d)), false, 0, 0.2 + Math.abs(Math.cos(UtilMath.r(i) * 7/3000d))*0.6, 1, false);
//
//				// Monthly items
//				PowerPlayAnimation powerPlayAnimation = (PowerPlayAnimation) _type;
//				for (ItemStack itemStack : powerPlayAnimation.getAnimationItems())
//				{
//					Item monthly = _creeper.getWorld().dropItem(_creeper.getLocation().add(0.5, 1.5, 0.5), itemStack);
//					_items.add(monthly);
//
//					vel = new Vector(Math.sin(UtilMath.r(i) * 7/5d), 0, Math.cos(UtilMath.r(i) * 7/5d));
//					UtilAction.velocity(monthly, vel, Math.abs(Math.sin(UtilMath.r(i) * 7/3000d)), false, 0, 0.2 + Math.abs(Math.cos(UtilMath.r(i) * 7/3000d))*0.6, 1, false);
//				}
//			}
//			finish();
//		}
//	}
//
//	@Override
//	protected void onFinish() {
//		_isDone = true;
//		_player = null;
//		setTicks(0);
//	}
//
//	public boolean isDone()
//	{
//		return _isDone;
//	}
//
//	public void setDone(boolean b)
//	{
//		_isDone = b;
//	}
//
//	public void setType(Object type)
//	{
//		_type = type;
//	}
//
//	public void setPlayer(Player player)
//	{
//		_player = player;
//	}
//
//	public void LegendAnimation()
//	{
//		if (getTicks() < 1)
//		{
//			UtilFirework.playFirework(_creeper.getLocation().add(0.5, 0.5, 0.5), Type.BALL_LARGE, Color.LIME, true, true);
//		}
//
//		if (getTicks() == 1)
//		{
//			_creeper.getLocation().getWorld().playSound(_creeper.getLocation().add(0.5, 0.5, 0.5), Sound.ENDERDRAGON_DEATH, 10F, 2.0F);
//		}
//		else if (getTicks() < 35)
//		{
//			double radius = 2 - (getTicks() / 10D * 2);
//			int particleAmount = 20 - (getTicks() * 2);
//			Location _centerLocation = _creeper.getLocation().add(0.5, 0.1, 0.5);
//			for (int i = 0; i < particleAmount; i++)
//			{
//				double xDiff = Math.sin(i/(double)particleAmount * 2 * Math.PI) * radius;
//				double zDiff = Math.cos(i/(double)particleAmount * 2 * Math.PI) * radius;
//				for(double e = 0.1 ; e < 3 ; e += 0.6)
//				{
//					Location location = _centerLocation.clone().add(xDiff, e, zDiff);
//					UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER, location, 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
//				}
//			}
//		}
//		else
//		{
//			finish();
//		}
//	}
//
//	public void MythicalAnimation()
//	{
//		if (getTicks() < 30)
//		{
//			UtilFirework.playFirework(_creeper.getLocation().add(0.5, 0.5, 0.5), Type.BALL_LARGE, Color.RED, true, true);
//		}
//
//		if (getTicks() == 1)
//		{
//			_creeper.getLocation().getWorld().playSound(_creeper.getLocation().add(0.5, 0.5, 0.5), Sound.PORTAL_TRAVEL, 10F, 2.0F);
//			_creeper.getLocation().getWorld().playSound(_creeper.getLocation().add(0.5, 0.5, 0.5), Sound.ZOMBIE_UNFECT, 10F, 0.1F);
//		}
//		else if (getTicks() < 40)
//		{
//			UtilFirework.launchFirework(_creeper.getLocation().add(0.5, 0.5, 0.5), Type.BALL_LARGE, Color.RED, true, true,
//					new Vector((Math.random()-0.5)*0.05, 0.1, (Math.random()-0.5)*0.05), 1);
//
//			//Particle Spiral Up
//			double radius = getTicks() / 20D;
//			int particleAmount = getTicks() / 2;
//			for (int i = 0; i < particleAmount; i++)
//			{
//				double xDiff = Math.sin(i/(double)particleAmount * 2 * Math.PI) * radius;
//				double zDiff = Math.cos(i/(double)particleAmount * 2 * Math.PI) * radius;
//
//				Location location = _creeper.getLocation().add(0.5, 0, 0.5).clone().add(xDiff, -1.3, zDiff);
//				UtilParticle.PlayParticle(UtilParticle.ParticleType.WITCH_MAGIC, location, 0, 0, 0, 0, 1,
//						ViewDist.NORMAL, UtilServer.getPlayers());
//			}
//
//			Location _centerLocation = _creeper.getLocation().add(0.5, 0.1, 0.5);
//			for (int i = 0; i < particleAmount; i++)
//			{
//				double xDiff = Math.sin(i/(double)particleAmount * 2 * Math.PI) * radius;
//				double zDiff = Math.cos(i/(double)particleAmount * 2 * Math.PI) * radius;
//				for(double e = 0.1 ; e < 3 ; e += 0.5)
//				{
//					Location location = _centerLocation.clone().add(xDiff, e, zDiff);
//					UtilParticle.PlayParticle(UtilParticle.ParticleType.WITCH_MAGIC, location, 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
//				}
//			}
//		}
//		else
//		{
//			finish();
//		}
//	}
//
//	public void RareAnimation()
//	{
//		if (getTicks() == 1)
//		{
//			for(int i = 0; i < 3; i++)
//			{
//				UtilFirework.playFirework(_creeper.getLocation().add(0.5, i, 0.5), Type.BALL, Color.FUCHSIA, false, false);
//			}
//			_creeper.getWorld().playSound(_creeper.getLocation(), Sound.WITHER_SPAWN, 10F, 1.2F);
//		}
//		else if (getTicks() >= 60)
//		{
//			finish();
//		}
//
//		else if (getTicks() < 35)
//		{
//			double radius = 2 - (getTicks() / 10D * 2);
//			int particleAmount = 20 - (getTicks() * 2);
//			Location _centerLocation = _creeper.getLocation().add(0.5, 0.1, 0.5);
//			for (int i = 0; i < particleAmount; i++)
//			{
//				double xDiff = Math.sin(i/(double)particleAmount * 2 * Math.PI) * radius;
//				double zDiff = Math.cos(i/(double)particleAmount * 2 * Math.PI) * radius;
//				for(double e = 0.1 ; e < 3 ; e += 0.6)
//				{
//					Location location = _centerLocation.clone().add(xDiff, e, zDiff);
//					UtilParticle.PlayParticle(UtilParticle.ParticleType.WITCH_MAGIC, location, 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
//				}
//			}
//		}
//	}
//
//	public void itemClean()
//	{
//		Iterator<Item> itemIterator = _items.iterator();
//
//		while (itemIterator.hasNext())
//		{
//			Item item = itemIterator.next();
//
//			if (item.isOnGround() || !item.isValid() || item.getTicksLived() > 60)
//			{
//				item.remove();
//				itemIterator.remove();
//			}
//		}
//	}
}