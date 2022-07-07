package nautilus.game.arcade.game.games.evolution.mobs.perks;

import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.data.BlockTossData;
import nautilus.game.arcade.kit.perks.event.PerkBlockThrowEvent;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkBlockTossEVO extends Perk implements IThrown
{
	/**
	 * @author Mysticate
	 */
	
	private HashMap<Player, BlockTossData> _hold = new HashMap<Player, BlockTossData>();
	private HashSet<Player> _charged = new HashSet<Player>();
	private HashMap<FallingBlock, Player> _falling = new HashMap<FallingBlock, Player>();
	
	public PerkBlockTossEVO()
	{
		super("Block Toss", new String[]
				{
				
				});
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void Grab(PlayerInteractEvent event)
	{	 	
		if (!Manager.GetGame().IsLive())
			return;
		
		Player player = event.getPlayer();

		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;

		if (!Manager.IsAlive(player))
			return;
		
		if (!Kit.HasKit(player))
			return;

		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(player, GetName(), 6000);
		Bukkit.getServer().getPluginManager().callEvent(useEvent);
		
		if (useEvent.isCancelled())
			return;
		
		if (!UtilInv.IsItem(event.getItem(), Material.IRON_SWORD, (byte) 0))
			return;

		if (_hold.containsKey(player))
			return;

		Block grab = event.getClickedBlock();

		if (UtilBlock.usable(grab))
			return;

		if (!UtilBlock.airFoliage(grab.getRelative(BlockFace.UP)) || Manager.GetBlockRestore().contains(grab.getRelative(BlockFace.UP)))
		{
			UtilPlayer.message(player, F.main("Game", "You may not pick up that block!"));
			return;
		}

		if (!Recharge.Instance.use(event.getPlayer(), useEvent.getAbility(), useEvent.getCooldown(), true, true))
			return;
		
		//Block to Data
		int id = grab.getTypeId();
		byte data = grab.getData();
		
		//Remove Block
		//Manager.GetBlockRestore().Add(event.getClickedBlock(), 0, (byte)0, 10000);
		event.getClickedBlock().getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, event.getClickedBlock().getType());
		
		_hold.put(player, new BlockTossData(id, data, System.currentTimeMillis()));
		
		//Effect
		player.getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, id);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void Throw(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		HashSet<Player> throwSet = new HashSet<Player>();

		for (Player cur : _hold.keySet())
		{
			//Throw
			if (!cur.isBlocking())
				throwSet.add(cur);

			//Charged Tick
			if (!_charged.contains(cur))
				if (System.currentTimeMillis() - _hold.get(cur).Time > 1200)
				{
					_charged.add(cur);
					cur.playEffect(cur.getLocation(), Effect.CLICK1, 0);
				}
		}

		for (Player cur : throwSet)
		{
			BlockTossData data = _hold.remove(cur);
			
			FallingBlock block  = cur.getWorld().spawnFallingBlock(cur.getEyeLocation().add(cur.getLocation().getDirection()), data.Type, data.Data);
			
			_falling.put(block, cur);
			
			_charged.remove(cur);
			
			long charge = System.currentTimeMillis() - data.Time;

			//Throw 
			double mult = 1.4;
			if (charge < 1200)
				mult = mult * ((double)charge/1200d);
			
			//Action
			UtilAction.velocity(block, cur.getLocation().getDirection(), mult, false, 0.2, 0, 1, true);
			Manager.GetProjectile().AddThrow(block, cur, this, -1, true, true, true, true, 
					null, 0, 0, null, 0, UpdateType.FASTEST, 1f);

			//Event
			PerkBlockThrowEvent blockEvent = new PerkBlockThrowEvent(cur);
			UtilServer.getServer().getPluginManager().callEvent(blockEvent);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (target == null)
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.PROJECTILE, data.getThrown().getVelocity().length() * 4, true, true, false,
				UtilEnt.getName(data.getThrower()), GetName());

		//Block to Item
		if (data.getThrown() instanceof FallingBlock)
		{
			FallingBlock thrown = (FallingBlock) data.getThrown();

			FallingBlock newThrown  = data.getThrown().getWorld().spawnFallingBlock(data.getThrown().getLocation(), thrown.getMaterial(), (byte)0);

			//Remove Old
			_falling.remove(thrown);
			thrown.remove();

			//Add New
			if (data.getThrower() instanceof Player)
				_falling.put(newThrown, (Player)data.getThrower());
		}
	}

	@Override
	public void Idle(ProjectileUser data) 
	{

	}

	@Override
	public void Expire(ProjectileUser data) 
	{

	}	
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void BlockForm(EntityChangeBlockEvent event)
	{
		if (!(event.getEntity() instanceof FallingBlock))
			return;

		FallingBlock falling = (FallingBlock)event.getEntity();	

		falling.getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, falling.getBlockId());

		_falling.remove(falling);
		falling.remove();

		event.setCancelled(true);
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;

		event.AddKnockback(GetName(), 2.5);
	}

}
