package mineplex.core.projectile;


import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public interface IThrown 
{
	public void Collide(LivingEntity target, Block block, ProjectileUser data);
	public void Idle(ProjectileUser data);
	public void Expire(ProjectileUser data);
	public void ChunkUnload(ProjectileUser data);
}
