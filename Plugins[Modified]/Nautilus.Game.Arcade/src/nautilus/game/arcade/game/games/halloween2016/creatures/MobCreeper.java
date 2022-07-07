package nautilus.game.arcade.game.games.halloween2016.creatures;

import java.lang.reflect.Method;

import net.minecraft.server.v1_8_R3.EntityCreeper;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreeper;
import org.bukkit.entity.Creeper;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilReflection;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobCreeper extends CryptBreaker<Creeper>
{

	public static final int CRYPT_DAMAGE = 200;
	public static float SPEED = 0.8f;
	
	private boolean exploded = false;

	public MobCreeper(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow + "Creeper", Creeper.class, loc, 0, 0, SPEED);
		
		_extraDamage = 5;
		
		_targetPlayers = false;
	}


	@Override
	public void Update(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
	
		if(_crypt.isDestroyed()) return;
		
		double width = UtilEnt.getWidth(GetEntity());
		
		EntityCreeper c = ((CraftCreeper)GetEntity()).getHandle();
		if(getClosestDoor().distanceSquared(GetEntity().getLocation()) <= width*width)
		{
			c.co();
		}
		
		int fuse = (int) UtilReflection.getValueOfField(c, "fuseTicks");
		int max = (int) UtilReflection.getValueOfField(c, "maxFuseTicks");
				
		if(fuse >= max-1)
		{
			_crypt.tryDamage(GetEntity(), CRYPT_DAMAGE, 0);
			exploded = true;
		}
	}
	
	@Override
	public void remove()
	{
		if(!exploded && GetEntity().isDead())
		{
			//Make creeper explode, even if dead using NMS code
			try
			{	
				EntityCreeper nms = ((CraftCreeper)GetEntity()).getHandle();
				Method explodeMethod = nms.getClass().getDeclaredMethod("cr");
				explodeMethod.setAccessible(true);
				explodeMethod.invoke(nms);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		super.remove();
	}
	
}
