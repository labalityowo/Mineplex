package mineplex.game.clans.clans.worldevent.boss.ironwizard;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import mineplex.core.common.util.F;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.boss.BossWorldEvent;

public class GolemBoss extends BossWorldEvent<GolemCreature>
{
	public GolemBoss(WorldEventManager manager)
	{
		super("Iron Wizard", manager.getBossArenaLocationFinder().getIronWizardCenter(), 50, manager.getBossArenaLocationFinder().getIronWizardPads().getLeft(), manager.getBossArenaLocationFinder().getIronWizardPads().getRight(), manager.getDisguiseManager(), manager.getClans().getProjectile(), manager.getClans().getDamageManager(), manager.getBlockRestore(), manager.getClans().getCondition());
	}

	@Override
	protected void customStart()
	{
		Bukkit.broadcastMessage(F.main(getName(), "The mighty " + getName() + " challenges you to face him!"));
		spawnGolem(getCenterLocation());
	}
	
	@Override
	public String getDeathMessage()
	{
		return F.main(getName(), "The mighty " + getName() + " has fallen!");
	}

	private GolemCreature spawnGolem(Location location)
	{
		GolemCreature golemCreature = new GolemCreature(this, location);
		registerCreature(golemCreature);
		setBossCreature(golemCreature);
		return golemCreature;
	}

	@Override
	protected void customTick() {}

	@Override
	public void customCleanup(boolean onDisable) {}

	@Override
	protected void customStop() {}
}