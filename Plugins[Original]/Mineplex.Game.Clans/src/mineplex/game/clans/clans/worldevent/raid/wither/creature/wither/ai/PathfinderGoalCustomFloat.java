package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither.ai;

import mineplex.game.clans.clans.worldevent.raid.wither.creature.wither.CharlesWitherton;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.Navigation;
import net.minecraft.server.v1_8_R3.PathfinderGoal;

public class PathfinderGoalCustomFloat extends PathfinderGoal
{
	private CharlesWitherton _boss;
	private EntityInsentient a;

	public PathfinderGoalCustomFloat(CharlesWitherton boss, EntityInsentient ent)
	{
		_boss = boss;
		this.a = ent;
		this.a(4);
		((Navigation)ent.getNavigation()).d(true);
	}

	public boolean a()
	{
		return _boss.Flying;
	}

	public void e()
	{
		if (this.a.bc().nextFloat() < 0.8F)
		{
			this.a.getControllerJump().a();
		}
	}
}