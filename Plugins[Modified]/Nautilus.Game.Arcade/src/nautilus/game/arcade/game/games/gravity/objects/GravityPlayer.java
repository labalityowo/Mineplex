package nautilus.game.arcade.game.games.gravity.objects;

import java.util.Collection;
import java.util.HashSet;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.gravity.Gravity;
import nautilus.game.arcade.game.games.gravity.GravityObject;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class GravityPlayer extends GravityObject
{

	public GravityPlayer(Gravity host, Entity ent, double mass, Vector vel) 
	{
		super(host, ent, mass, 2, vel);
		SetMovingBat(false);
	}
	
	@Override
	public void PlayCollideSound(double power) 
	{
		Ent.getWorld().playSound(Ent.getLocation(), Sound.HURT_FLESH, 0.8f, 0.75f);
	}
	
	public boolean NearBlock()
	{ 
		return !NearBlockList().isEmpty();
	}
	
	public Collection<Block> NearBlockList()
	{
		HashSet<Block> blocks = new HashSet<Block>();
		
		for (Block block : UtilBlock.getSurrounding(Base.getLocation().getBlock(), true))
		{
			if (UtilBlock.airFoliage(block))
				continue;
			
			blocks.add(block);
		}
		
		for (Block block : UtilBlock.getSurrounding(Base.getLocation().getBlock().getRelative(BlockFace.UP), true))
		{
			if (UtilBlock.airFoliage(block))
				continue;
			
			blocks.add(block);
		}
		
		for (Block block : UtilBlock.getSurrounding(Base.getLocation().getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP), true))
		{
			if (UtilBlock.airFoliage(block))
				continue;
			
			blocks.add(block);
		}
		
		return blocks;
	}

	public void AutoGrab()
	{
		//UtilPlayer.message(this.Ent, "Bat: " + Base.getLocation().getY());
		
		if (Vel.length() == 0)
			return;
		
		if (!UtilTime.elapsed(GrabDelay, 1000))
			return;
		
		if (!NearBlock())
			return;
		
		Vel.multiply(0);
		Base.setVelocity(new Vector(0,0.1,0));
		
		GrabDelay = System.currentTimeMillis();
		
		//Sound
		Ent.getWorld().playSound(Ent.getLocation(), Sound.STEP_STONE, 2f, 0.5f);
		
		//Effect
		Ent.getWorld().playEffect(Ent.getLocation(), Effect.STEP_SOUND, 1);
		
		//Bat
		SetMovingBat(false);
		
		//UtilPlayer.message(Ent, "You grabbed onto a Block");
	}
	
	public void KickOff(Player player) 
	{
		if (!Ent.equals(player))
			return;
		
		boolean nearBlock = false;
		for (Block block : UtilBlock.getSurrounding(Base.getLocation().getBlock(), true))
		{
			if (block.getType() != Material.AIR)
			{
				nearBlock = true;
				break;
			}
		}
		
		//Requires near block OR batsit
		if (!Bat.isSitting() && !nearBlock)
			return;
		
		GrabDelay = System.currentTimeMillis();
		
		AddVelocity(player.getLocation().getDirection().multiply(0.5), 0.5);
		
		//Sound
		Ent.getWorld().playSound(Ent.getLocation(), Sound.STEP_WOOD, 2f, 0.5f);
		
		//Effect
		Ent.getWorld().playEffect(Ent.getLocation(), Effect.STEP_SOUND, 1);
		
		//UtilPlayer.message(Ent, "You kicked off a Block");
	}
	
	public void Jetpack() 
	{
		if (!Ent.isValid())
			return;
		
		if (!(Ent instanceof Player))
			return;
		
		Player player = ((Player)Ent);
		
		if (!player.isBlocking())
			return;
		
		if (player.getExp() <= 0)
		{
			if (Recharge.Instance.use(player, "Fuel", 1000, false, false))
			{
				UtilTextMiddle.display(C.cRed + "Jetpack Empty", "Collect Fuel at Gold Blocks with Fireworks", 0, 80, 5, (Player)Ent);
			}
				
			return;
		}
		
		player.setExp((float) Math.max(0, player.getExp()-0.005));
		
		AddVelocity(player.getLocation().getDirection().multiply(0.025), 0.5);
		
		//Sound
		Ent.getWorld().playSound(Ent.getLocation(), Sound.GHAST_FIREBALL, 0.3f, 2f);
		
		//Effect
		UtilParticle.PlayParticle(ParticleType.FLAME, Ent.getLocation().add(0, 0.5, 0), 0.1f, 0.1f, 0.1f, 0, 2,
				ViewDist.MAX, UtilServer.getPlayers());
	}

	public void Oxygen() 
	{
		boolean near = false;
		for (Block block : UtilBlock.getInRadius(Ent.getLocation(), 4d).keySet())
		{
			if (block.getType() == Material.EMERALD_BLOCK)
			{
				near = true;
				break;
			}
		}
		
		//Restore
		if (near)
		{
			if (Base.getHealth() < 58)
				UtilTextMiddle.display(null, "Refilling Oxygen...", 0, 60, 5, (Player)Ent);
			else
				UtilTextMiddle.display(null, C.cGreen + "Oxygen Full", 0, 60, 5, (Player)Ent);
			
			Ent.getWorld().playSound(Ent.getLocation(), Sound.CAT_HISS, 0.2f, 0.5f);
			
			Base.setHealth(Math.min(60, Base.getHealth() + 10));
		}
		//Lose
		else
		{
			Base.setHealth(Math.max(1, Base.getHealth() - 1));
		}
		
		//Die
		if (Base.getHealth() <= 1)
		{
			Host.Manager.GetDamage().NewDamageEvent((Player)Ent, null, null, 
					DamageCause.CUSTOM, 2, false, true, false,
					"Oxygen Depleted", "Oxygen Depleted");
			
			UtilTextMiddle.display(C.cRed + "Suffocating", "Refill Oxygen at Emerald Blocks", 0, 60, 5, (Player)Ent);
		}
	}
	
	@Override
	public void CustomCollide(GravityObject other) 
	{
		UtilPlayer.health((Player)Ent, -1);
	}
}
