package mineplex.game.clans.clans.worldevent.boss.skeletonking;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.boss.BossWorldEvent;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.MinionType;

public class SkeletonBoss extends BossWorldEvent<SkeletonCreature>
{
	protected boolean canMove = false;
	
	public SkeletonBoss(WorldEventManager manager)
	{
		super("Skeleton King", manager.getBossArenaLocationFinder().getSkeletonKingCenter(), 50, manager.getBossArenaLocationFinder().getSkeletonKingPads().getLeft(), manager.getBossArenaLocationFinder().getSkeletonKingPads().getRight(), manager.getDisguiseManager(), manager.getClans().getProjectile(), manager.getClans().getDamageManager(), manager.getBlockRestore(), manager.getClans().getCondition());
	}
	
	@Override
	protected void customStart()
	{
		Bukkit.broadcastMessage(F.main(getName(), "The evils of the world have manifested in the form of the " + getName() + "! Become the champion of Light and destroy him!"));
		spawnSkeletonKing(getCenterLocation());
		Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), () ->
		{
			canMove = true;
		}, 20 * 3);
	}

	public EventCreature<?> spawnMinion(MinionType type, Location location)
	{
		EventCreature<?> minionCreature = type.getNewInstance(this, location);
		if (minionCreature != null)
		{
			registerCreature(minionCreature);
		}
		return minionCreature;
	}

	private SkeletonCreature spawnSkeletonKing(Location location)
	{
		SkeletonCreature kingCreature = new SkeletonCreature(this, location);
		registerCreature(kingCreature);
		setBossCreature(kingCreature);
		return kingCreature;
	}

	@Override
	public String getDeathMessage()
	{
		return F.main(getName(), "The demonic " + getName() + " has been slain!");
	}

	@Override
	protected void customTick() {}

	@Override
	public void customCleanup(boolean onDisable) {}

	@Override
	protected void customStop() {}
}