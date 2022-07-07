package nautilus.game.arcade.game.games.paintball.kits.perks;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.common.util.C;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargedEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.kit.Perk;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;

public class PerkPaintballSniper extends Perk implements IPacketHandler
{
	/**
	 * Created by: Mysticate
	 * Timestamp: October 27, 2015
	 */
	
	private static class Bullet
	{
		public Player Shooter;
		public int Damage;
		public Location LastSeen;
	}
	
	private HashSet<Player> _crouching = new HashSet<Player>();
	private NautHashMap<Arrow, Bullet> _fired = new NautHashMap<Arrow, Bullet>();
	
	private double _velocity = 15;
	
	private boolean _spawning = false;
	
	public PerkPaintballSniper() 
	{
		super("Sniper Rifle", new String[] 
				{
				C.cYellow + "Crouch" + C.cGray + " to use " + C.cGreen + "Scope",
				C.cYellow + "Right-Click" + C.cGray + " to use " + C.cGreen + "Sniper Rifle",
				"Experience Bar represents damage."
				});		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void registeredEvents()
	{
		Manager.getPacketHandler().addPacketHandler(this, PacketPlayOutSpawnEntity.class);
	}

	@EventHandler
	public void Recharge(RechargedEvent event)
	{
		if (!event.GetAbility().equals(GetName()))
			return;
		
		event.GetPlayer().playSound(event.GetPlayer().getLocation(), Sound.NOTE_STICKS, 2f, 1f);
		event.GetPlayer().playSound(event.GetPlayer().getLocation(), Sound.NOTE_STICKS, 2f, 1.5f);
	}
	
	@EventHandler
	public void Shoot(PlayerInteractEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getItem(), Material.STONE_HOE))
			return;

		Player player = event.getPlayer();

		if (!Manager.IsAlive(player))
			return;
		
		if (UtilPlayer.isSpectator(player))
			return;
		
		if (!Kit.HasKit(player))
			return;
		
		event.setCancelled(true);

		if (!Recharge.Instance.use(player, GetName(), 1000, true, true))
			return;

		net.minecraft.server.v1_8_R3.World world = ((CraftWorld) player.getWorld()).getHandle();
		EntityArrow launch = new EntityArrow(world, ((CraftPlayer) player).getHandle(), 0F);
		Location location = player.getEyeLocation();
		
		launch.projectileSource = player;
		launch.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

		((Projectile) launch.getBukkitEntity()).setVelocity(player.getEyeLocation().getDirection().clone().multiply(_velocity));

		_spawning = true;
		world.addEntity(launch);
		_spawning = false;
		
		Arrow proj = (Arrow) launch.getBukkitEntity();
		
		proj.setMetadata("color", new FixedMetadataValue(Manager.getPlugin(), Manager.GetGame().GetTeam(player).GetColor().ordinal()));
		
		Bullet bullet = new Bullet();
		bullet.Shooter = player;
		bullet.Damage = getPaintDamage(player);
		bullet.LastSeen = proj.getLocation();
		
		_fired.put(proj, bullet);

		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.8f, 1f);
		
		player.setSneaking(false);
		
		//Effects 
		removeEffects(player);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		//Cleanup check
		for (Player player : new HashSet<Player>(_crouching))
		{
			boolean remove = false;
			if (!player.isOnline())
				remove = true;
			
			if (!Manager.GetGame().IsAlive(player))
				remove = true;

			if (UtilPlayer.isSpectator(player))
				remove = true;

			if (!player.isSneaking())
				remove = true;
			
			if (!UtilInv.IsItem(player.getItemInHand(), Material.STONE_HOE, (byte) -1))
				remove = true;
			
			if (!Recharge.Instance.usable(player, GetName()))
				remove = true;

			if (remove)
			{
				if (_crouching.contains(player))
					_crouching.remove(player);
				
				player.setExp(0F);
				player.setSneaking(false);
				
				// Zoom
				removeEffects(player);
			}
		}
		
		//Add check
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player))
				continue;
			
			if (!Kit.HasKit(player))
				continue;
			
			if (!player.isSneaking())
				continue;
			
			if (!UtilInv.IsItem(player.getItemInHand(), Material.STONE_HOE, (byte) -1))
				continue;
			
			if (_crouching.contains(player))
				continue;
			
			_crouching.add(player);
			
			// Zoom
			addEffects(player);
		}
		
		// Exp check
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player))
				continue;
			
			if (!Kit.HasKit(player))
				continue;
			
			if (!_crouching.contains(player))
				continue;
			
			player.setExp((float) Math.min(.999F, player.getExp() + .03332));
		}
	}
	
	@EventHandler
	public void doArrowEffects(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		for (Arrow proj : new ArrayList<Arrow>(_fired.keySet()))
		{	
			Bullet bullet = _fired.get(proj);
			
			double curRange = 0;
			double distance = Math.abs(UtilMath.offset(proj.getLocation(), bullet.LastSeen));
						
			while (curRange <= distance)
			{
				Location newTarget = bullet.LastSeen.add(UtilAlg.getTrajectory(bullet.LastSeen, proj.getLocation()).multiply(curRange));
				
				//Progress Forwards
				curRange += 0.8;
				
				if (UtilMath.offset(bullet.Shooter.getLocation(), newTarget) < 2)
					continue;
				
				ChatColor color = ChatColor.values()[proj.getMetadata("color").get(0).asInt()];
				if (color == ChatColor.BLUE || color == ChatColor.AQUA)
				{
					UtilParticle.PlayParticle(ParticleType.RED_DUST, newTarget, -1, 1, 1, 1, 0,
							ViewDist.NORMAL, UtilServer.getPlayers());			
				}
				else
				{
					UtilParticle.PlayParticle(ParticleType.RED_DUST, newTarget, 0, 0, 0, 0, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());			
				}
			}
			
			if (!proj.isValid())
			{
				_fired.remove(proj);
				continue;
			}
			
			bullet.LastSeen = proj.getLocation();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void clean(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		
		for (Player player : _crouching)
		{
			removeEffects(player);
		}
		
		_crouching.clear();
		
		Manager.getPacketHandler().removePacketHandler(this);
	}
	
	private void removeEffects(Player player)
	{
		// Zoom
		if (Manager.GetCondition().HasCondition(player, ConditionType.SLOW, GetName()))
			Manager.GetCondition().EndCondition(player, ConditionType.SLOW, GetName());
		
//		if (Manager.GetCondition().HasCondition(player, ConditionType.JUMP, getName()))
//			Manager.GetCondition().EndCondition(player, ConditionType.JUMP, getName());
	}
	
	private void addEffects(Player player)
	{
		// Zoom
		if (!Manager.GetCondition().HasCondition(player, ConditionType.SLOW, GetName()))
			Manager.GetCondition().Factory().Slow(GetName(), player, null, Double.MAX_VALUE, 8, false, false, false, true);
	}
	
	public int getPaintDamage(Arrow proj)
	{
		if (!_fired.containsKey(proj))
			return 1;
		
		return _fired.get(proj).Damage;
	}
	
	private int getPaintDamage(Player player)
	{
		if (!_crouching.contains(player))
			return 1;
		
		return 1 + (int) Math.floor(player.getExp() * 10 / 3);		
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (!_spawning)
			return;
		
		packetInfo.setCancelled(true);
	}
}