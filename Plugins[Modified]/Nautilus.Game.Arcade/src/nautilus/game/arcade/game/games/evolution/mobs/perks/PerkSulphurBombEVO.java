package nautilus.game.arcade.game.games.evolution.mobs.perks;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkSulphurBombEVO extends Perk implements IThrown
{
	/**
	 * @author Mysticate
	 */
	
	public PerkSulphurBombEVO()
	{
		super("Sulphur Bomb", new String[]
				{
				
				});
	}

	@EventHandler
	public void fire(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!UtilInv.IsItem(event.getItem(), Material.SULPHUR, (byte) 0))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(player, GetName(), 3000);
		Bukkit.getServer().getPluginManager().callEvent(useEvent);
		
		if (useEvent.isCancelled())
			return;
		
		if (!Recharge.Instance.use(player, useEvent.getAbility(), useEvent.getCooldown(), true, true))
			return;
		
		event.setCancelled(true);

		Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.COAL, (byte)0));

		UtilAction.velocity(ent, player.getLocation().getDirection(), 1, false, 0, 0.2, 10, false);	

		Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, true, 
				null, 1f, 1f, 
				Effect.SMOKE, 1, UpdateType.SLOW, 
				1f);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.CREEPER_DEATH, 2f, 1.5f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Explode(data);

		if (target == null)
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.PROJECTILE, 5, true, true, false,
				UtilEnt.getName(data.getThrower()), GetName());
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Explode(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Explode(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	public void Explode(ProjectileUser data)
	{
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, data.getThrown().getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.EXPLODE, 1f, 1.5f);
		data.getThrown().remove();
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;

		event.AddKnockback(GetName(), 1.5);
	}	
}