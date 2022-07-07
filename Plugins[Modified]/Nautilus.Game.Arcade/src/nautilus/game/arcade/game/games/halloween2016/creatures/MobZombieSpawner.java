package nautilus.game.arcade.game.games.halloween2016.creatures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobZombieSpawner extends CryptBreaker<Zombie>
{
	
	private static final int CRYPT_DAMAGE = 20;
	private static final int CRYPT_RATE = 20;
	private static final float SPEED = 0.5f;
	private static final int MAX_MINI_ZOMBIES = 4;
	
	private List<MobMiniZombie> _miniZombies = new ArrayList<>();

	public MobZombieSpawner(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow + "Zombie Spawner", Zombie.class, loc, CRYPT_DAMAGE, CRYPT_RATE, SPEED);
	}
	
	@Override
	public void SpawnCustom(Zombie ent)
	{
		ent.setVillager(true);
		
		ent.getEquipment().setHelmet(new ItemStack(Material.MOB_SPAWNER));
	}
	
	@Override
	public void Update(UpdateEvent event)
	{
		super.Update(event);
		
		if(event.getType() == UpdateType.SEC_05)
		{
			for(Iterator<MobMiniZombie> it = _miniZombies.iterator(); it.hasNext();)
			{
				if(!it.next().GetEntity().isValid()) it.remove();
			}
			
			if(_miniZombies.size() >= MAX_MINI_ZOMBIES) return;
			
			//Combat concurrency issues (can't add mobs while in the "tick" loop of mobs)
			Host.Manager.runSync(() -> 
			{
				if(GetEntity().isValid())
				{
					Location loc = GetEntity().getLocation().add(GetEntity().getLocation().getDirection().normalize());
					UtilParticle.PlayParticleToAll(ParticleType.SMOKE, loc, null, 0.1f, 20, ViewDist.NORMAL);
					loc.getWorld().playSound(loc, Sound.ZOMBIE_INFECT, 1, 0.6f);
					MobMiniZombie mini = new MobMiniZombie(Host, loc);
					_miniZombies.add(mini);
					Host16.AddCreature(mini, false); 
				}
			});
		}
	}

}
