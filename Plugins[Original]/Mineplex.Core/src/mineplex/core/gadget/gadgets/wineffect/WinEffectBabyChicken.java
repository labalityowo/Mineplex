package mineplex.core.gadget.gadgets.wineffect;

import java.util.*;
import java.util.Map.Entry;

import mineplex.core.Managers;
import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.hologram.Hologram;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_8_R3.PathfinderGoal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftChicken;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class WinEffectBabyChicken extends WinEffectGadget
{
	private Map<Hologram, Vector> _text = new HashMap<>();
	private Chicken _chicken;
	private DisguisePlayer _npc;
	private List<Chicken> _teamChickens = new ArrayList<>();
	private int _tick;

	public WinEffectBabyChicken(GadgetManager manager)
	{
		super(manager, "Baby Chicken", UtilText.splitLineToArray(C.cGray + "Bawk Bawk strikes again! Summon his minions and run around as an itty bitty chicken.", LineFormat.LORE),
                -2, Material.EGG, (byte) 0);
		
		_schematicName = "ChickenPodium";
	}

	@Override
	public void play()
	{
		Location loc = getBaseLocation();
		loc.setDirection(_player.getLocation().subtract(loc).toVector());
		
		_npc = getNPC(getPlayer(), loc);
		
		_tick = 0;

		List<Location> circle = UtilShapes.getPointsInCircle(getBaseLocation(), _team.size(), 3);
		for (int i = 0; i < _team.size(); i++)
		{
			Player p = _team.get(i);
			Location l = circle.get(i);
			l.setDirection(getBaseLocation().toVector().subtract(l.toVector()));
			Chicken c = spawnChicken(p, l);
			c.setBaby();
			UtilEnt.setBoundingBox(c, 0, 0);
			_teamChickens.add(c);
		}
	}

	public Chicken spawnChicken(Player player, Location loc)
	{
		Chicken chicken = loc.getWorld().spawn(loc, Chicken.class);
		String rank = getRank(player);
		chicken.setCustomName(rank + player.getName());
		chicken.setCustomNameVisible(true);

		UtilEnt.removeGoalSelectors(chicken);
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl -> vm.hidePlayer(pl, player, "Baby Chicken Win Effect"));

		return chicken;
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isRunning()) return;

		if (event.getType() != UpdateType.TICK) return;
		
		_tick++;
		
		if (_tick < 20*2)
		{
			return;
		}
		else if (_tick == 20*2)
		{
			Location loc = _npc.getEntity().getBukkitEntity().getLocation();
			
			_npc.getEntity().getBukkitEntity().remove();
			
			UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, loc.clone().add(0, 1, 0), 0.3f, 0.6f, 0.3f, 0.07f, 200, ViewDist.NORMAL);
			UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, loc.clone().add(0, 0.3, 0), 0.7f, 0.1f, 0.7f, 0.07f, 200, ViewDist.NORMAL);
			
			for (int i = 0; i < 10; i++)
			{
				Vector v = Vector.getRandom().subtract(Vector.getRandom()).multiply(0.25).setY(0.5);
				UtilItem.dropItem(new ItemStack(Material.EGG), loc, false, false, 8*20, false).setVelocity(v);
			}
			
			_chicken = spawnChicken(_player, loc);
			UtilEnt.addGoalSelector(_chicken, 0, new PathfinderRandomRun(((CraftChicken)_chicken).getHandle(), getBaseLocation(), 4, 2.8));
			
			_chicken.getWorld().playSound(_chicken.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
			
			return;
		}
		
		
		// Flap Chicken Wings
		PacketPlayOutRelEntityMove packet = new PacketPlayOutRelEntityMove(_chicken.getEntityId(), (byte) 0, (byte) 0, (byte) 0, false);
		UtilPlayer.getNearby(_chicken.getLocation(), 64).stream().forEach(p -> UtilPlayer.sendPacket(_player, packet));

		((CraftChicken) _chicken).getHandle().lastDamager = ((CraftChicken) _chicken).getHandle();

		if (UtilMath.r(10) == 0 || _text.size() < 3)
		{
			_chicken.getWorld().playSound(_chicken.getLocation(), Sound.CHICKEN_IDLE, 1, (float) (1 + Math.random()*0.6));

			Hologram hologram = new Hologram(Manager.getHologramManager(), _chicken.getEyeLocation(), C.Italics + "BAWK");
			_text.put(hologram, Vector.getRandom().subtract(Vector.getRandom()).normalize().multiply(0.4).setY(0.6));
			hologram.start();
		}

		for (Iterator<Entry<Hologram, Vector>> it = _text.entrySet().iterator(); it.hasNext();)
		{
			Entry<Hologram, Vector> e = it.next();
			e.getValue().setY(e.getValue().getY() - 0.1);
			if(e.getKey().getLocation().getY() < _chicken.getLocation().getY())
			{
				e.getKey().stop();
				it.remove();
			}
			e.getKey().setLocation(e.getKey().getLocation().add(e.getValue()));
		}
		
		for (Chicken c : _teamChickens)
		{
			UtilEnt.CreatureLook(c, _chicken);
		}
	}

	@Override
	public void finish()
	{
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl ->
		{
			vm.showPlayer(pl, _player, "Baby Chicken Win Effect");
			_team.forEach(p ->
			{
				vm.showPlayer(pl, p, "Baby Chicken Win Effect");
			});
		});
		_text.keySet().forEach(h -> h.stop());
		_text.clear();
		_chicken.remove();
		UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, _chicken.getLocation().add(0, 0.15, 0), 0.3f, 0.3f, 0.3f, 0.1f, 50, ViewDist.NORMAL);
		_chicken = null;
		_teamChickens.forEach(c -> c.remove());
	}
	
	@Override
	public void teleport()
	{
		Location loc = getBaseLocation().add(0, 3, 5);
		loc.setDirection(getBaseLocation().subtract(loc).toVector());
		super.teleport(loc);
	}

	private static class PathfinderRandomRun extends PathfinderGoal
	{

		private EntityCreature _ent;
		private Location _base;
		private double _radi;
		private double _speed;
		private long forceNext = 0;

		public PathfinderRandomRun(EntityCreature ent, Location base, double radi, double speed)
		{
			_ent = ent;
			_base = base;
			_radi = radi;
			_speed = speed;
		}

		public boolean a()
		{
			return true;
		}

		public boolean b()
		{
			return !(_ent.getNavigation().m() || forceNext > System.currentTimeMillis());
		}

		public void c()
		{
			forceNext = System.currentTimeMillis() + 2000;
			Vector ran = Vector.getRandom().subtract(Vector.getRandom()).setY(0).normalize().multiply(_radi);
			Location loc = _base.clone().add(ran);
			_ent.getNavigation().a(loc.getX(), loc.getY(), loc.getZ(), _speed);
		}
	}
}