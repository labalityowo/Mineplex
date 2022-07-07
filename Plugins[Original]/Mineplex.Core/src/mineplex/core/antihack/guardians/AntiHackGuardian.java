package mineplex.core.antihack.guardians;

import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;

import com.mineplex.spigot.ChunkAddEntityEvent;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.event.StackerEvent;

public class AntiHackGuardian implements Listener
{
	private static final boolean DEBUG = false;

	private static final double DELTA_MOVE_PER_TICK = 0.2;

	private static final Function<Double, Double> MAGICAL_FUNCTION = in ->
	{
		return Math.pow(100, in - 1);
	};

	protected Random _random = new Random();

	private Location _center;

	private double _targetX;
	private double _targetY;
	private double _targetZ;

	private int _ticksUntilReset;

	private ArmorStand _armorStand;
	private EntityArmorStand _nmsEntity;

	private DisguiseGuardian _disguise;

	private UUID _entityUUID;

	private Player _target;
	private int _stalkTime;

	private final double MAX_DISTANCE_X;
	private final double MIN_DISTANCE_X;
	private final double MAX_DISTANCE_Y;
	private final double MIN_DISTANCE_Y;
	private final double MAX_DISTANCE_Z;
	private final double MIN_DISTANCE_Z;

	private final double CENTER_X;
	private final double CENTER_Y;
	private final double CENTER_Z;

	public AntiHackGuardian(Location center, int maxX, int minX, int maxY, int minY, int maxZ, int minZ)
	{
		this(center,maxX,minX, maxY, minY, maxZ, minZ, true);
	}

	public AntiHackGuardian(Location center, int maxX, int minX, int maxY, int minY, int maxZ, int minZ, boolean hideForStaff)
	{
		UtilServer.RegisterEvents(this);

		MAX_DISTANCE_X = maxX;
		MIN_DISTANCE_X = minX;
		MAX_DISTANCE_Y = maxY;
		MIN_DISTANCE_Y = minY;
		MAX_DISTANCE_Z = maxZ;
		MIN_DISTANCE_Z = minZ;

		CENTER_X = MIN_DISTANCE_X + ((MAX_DISTANCE_X - MIN_DISTANCE_X) / 2.0);
		CENTER_Y = MIN_DISTANCE_Y + ((MAX_DISTANCE_Y - MIN_DISTANCE_Y) / 2.0);
		CENTER_Z = MIN_DISTANCE_Z + ((MAX_DISTANCE_Z - MIN_DISTANCE_Z) / 2.0);

		//debug("Spawning ArmorStand at " + center + "");

		CoreClientManager clientManager = Managers.get(CoreClientManager.class);
		DisguiseManager disguiseManager = Managers.get(DisguiseManager.class);

		_center = center;
		_center.getChunk().load();

		_armorStand = (ArmorStand) new EntityArmorStand(((CraftWorld) _center.getWorld()).getHandle(), _center.getX(), _center.getY(), _center.getZ()).getBukkitEntity();
		_armorStand.setGravity(false);
		_armorStand.setVisible(false);
		_armorStand.setRemoveWhenFarAway(false);
		_nmsEntity = ((CraftArmorStand) _armorStand).getHandle();
		_nmsEntity.maxNoDamageTicks = 86400;
		_nmsEntity.noDamageTicks = 86400;

		_entityUUID = _armorStand.getUniqueId();

		_disguise = new DisguiseGuardian(_armorStand);
		_disguise.setHideIfNotDisguised(true);

		disguiseManager.disguise(_disguise, player ->
		{
			if (!hideForStaff) return true;

			// Don't let Builder -> Admin see it
			if (!clientManager.Get(player).hasPermission(AntiHack.Perm.SEE_GUARDIANS))
			{
				return false;
			}
			return true;
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onSpawn(EntitySpawnEvent event)
	{
		if (event.getEntity() instanceof ArmorStand)
		{
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void onLoad(ChunkAddEntityEvent event)
	{
		if (event.getEntity().getUniqueId().equals(_entityUUID))
		{
			_armorStand = (ArmorStand) event.getEntity();
			_nmsEntity = ((CraftArmorStand) _armorStand).getHandle();
		}
	}

	@EventHandler
	public void onStack(StackerEvent event)
	{
		if (event.getEntity().getUniqueId().equals(_entityUUID))
		{
			event.setCancelled(true);
		}
	}

	public void tick()
	{
		if (_nmsEntity.dead || !_nmsEntity.valid)
		{
			//debug("Skipping because " + _armorStand.isDead() + " " + _armorStand.isValid());
			return;
		}

		if (_target == null)
		{
			regularTick();
		}
		else
		{
			_stalkTime++;
			targetTick();
		}
		//debug("Ticking " + _armorStand + " " + _armorStand.isDead() + " " + _armorStand.getLocation() + " " + _ticksUntilReset);
	}

	private void regularTick()
	{
		if (_ticksUntilReset <= 0)
		{
			reset();
		}

		//debug("===== Begin Calculations =====");


		//debug("Target: " + _targetX + " " + _targetY + " " + _targetZ);
		//debug("Start: " + _armorStand.getLocation());
		double deltaX = _targetX - _nmsEntity.locX;
		double deltaY = _targetY - _nmsEntity.locY;
		double deltaZ = _targetZ - _nmsEntity.locZ;

		//debug("Delta Location: " + deltaX + " " + deltaY + " "+ deltaZ);

		double dx = 0;
		if (deltaX > 0.1) dx = DELTA_MOVE_PER_TICK;
		else if (deltaX < -0.1) dx = -DELTA_MOVE_PER_TICK;
		double dy = 0;
		if (deltaY > 0.1) dy = DELTA_MOVE_PER_TICK;
		else if (deltaY < -0.1) dy = -DELTA_MOVE_PER_TICK;
		double dz = 0;
		if (deltaZ > 0.1) dz = DELTA_MOVE_PER_TICK;
		else if (deltaZ < -0.1) dz = -DELTA_MOVE_PER_TICK;


		_nmsEntity.locX += dx;
		_nmsEntity.locY += dy;
		_nmsEntity.locZ += dz;

		//debug("Dest: " + _nmsEntity.locX + " " + _nmsEntity.locY + " " + _nmsEntity.locZ);
		//debug("===== End Calculations =====");

		// Only send look update every second
		if (_nmsEntity.ticksLived % 20 == 0)
		{
			UtilEnt.CreatureLook(_armorStand, _nmsEntity.locX, _nmsEntity.locY, _nmsEntity.locZ, _targetX, _targetY, _targetZ);
		}

		_ticksUntilReset--;
	}

	private void targetTick()
	{
		//debug("===== Stalking " + _target.getName() + " =====");
		EntityPlayer entityPlayer = ((CraftPlayer) _target).getHandle();

		Vector direction = _target.getLocation().getDirection().normalize().multiply(-6);

		_nmsEntity.locX = entityPlayer.locX + direction.getX();
		_nmsEntity.locZ = entityPlayer.locZ + direction.getZ();
		_nmsEntity.locY = entityPlayer.locY + 10.0 + nextDouble(-1.0, 1.0);

		UtilEnt.CreatureLook(_armorStand, _nmsEntity.locX, _nmsEntity.locY, _nmsEntity.locZ, entityPlayer.locX, entityPlayer.locY, entityPlayer.locZ);
	}

	public void reset()
	{
		//debug("======= BEGIN RESET ======");
		final double x = _nmsEntity.locX;
		final double y = _nmsEntity.locY;
		final double z = _nmsEntity.locZ;

		double cx = 0, cy = 0, cz = 0;
		if (x > CENTER_X)
			cx = (x - CENTER_X) / (MAX_DISTANCE_X - CENTER_X);
		else if (x < CENTER_X)
			cx = (CENTER_X - x) / (CENTER_X - MIN_DISTANCE_X);
		if (y > CENTER_Y)
			cy = (y - CENTER_Y) / (MAX_DISTANCE_Y - CENTER_Y);
		else if (y < CENTER_Y)
			cy = (CENTER_Y - y) / (CENTER_Y - MIN_DISTANCE_Y);
		if (z > CENTER_Z)
			cz = (z - CENTER_Z) / (MAX_DISTANCE_Z - CENTER_Z);
		else if (z < CENTER_Z)
			cz = (CENTER_Z - z) / (CENTER_Z - MIN_DISTANCE_Z);

		cx = MAGICAL_FUNCTION.apply(cx) * (x > CENTER_X ? -(MAX_DISTANCE_X - CENTER_X) : (CENTER_X - MIN_DISTANCE_X));
		cy = MAGICAL_FUNCTION.apply(cy) * (y > CENTER_Y ? -(MAX_DISTANCE_Y - CENTER_Y) : (CENTER_Y - MIN_DISTANCE_Y));
		cz = MAGICAL_FUNCTION.apply(cz) * (z > CENTER_Z ? -(MAX_DISTANCE_Z - CENTER_Z) : (CENTER_Z - MIN_DISTANCE_Z));

		//debug("Start: " + _armorStand.getLocation());
		//debug("Changes: " + cx + " " + cy + " " + cz);

		int ex = nextInt(8, 12);
		int ey = nextInt(0, 3);
		int ez = nextInt(8, 12);

		if (_random.nextBoolean())
			ex = -ex;
		if (_random.nextBoolean())
			ey = -ey;
		if (_random.nextBoolean())
			ez = -ez;

		ex += cx;
		ey += cy;
		ez += cz;

		int dx = ex;
		int dy = ey;
		int dz = ez;

		//debug("Deltas: " + dx + " " + dy + " " + dz);

		_targetX = x + dx;
		_targetY = y + dy;
		_targetZ = z + dz;
		//debug("End: " + _targetX + " " + _targetY + " " + _targetZ);


		// If we can't find a good position, just go to the center
		if (!locCheck())
		{
			_targetX = CENTER_X;
			_targetY = CENTER_Y;
			_targetZ = CENTER_Z;

			dx = (int) (CENTER_X - x);
			dy = (int) (CENTER_Y - y);
			dz = (int) (CENTER_Z - z);
		}

		double maxDelta = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));

		_ticksUntilReset = (int) (maxDelta / DELTA_MOVE_PER_TICK);

		// Send look update for new target
		UtilEnt.CreatureLook(_armorStand, _nmsEntity.locX, _nmsEntity.locY, _nmsEntity.locZ, _targetX, _targetY, _targetZ);

		//debug("Ticks: " + _ticksUntilReset);
		//debug("======= END RESET ======");
	}

	public void target(Player player)
	{
		_target = player;
	}

	public boolean isTargeting()
	{
		return _target != null;
	}

	public int getTargetingTime()
	{
		return _stalkTime;
	}

	public void stopTargeting()
	{
		_target = null;
		_stalkTime = 0;
		reset();
	}

	public void shoot(Player player)
	{
		_disguise.setTarget(player == null ? 0 : player.getEntityId());
		Managers.get(DisguiseManager.class).updateDisguise(_disguise);
	}

	public Player getTarget()
	{
		return _target;
	}

	private boolean locCheck()
	{
		if (_targetX >= MAX_DISTANCE_X ||
				_targetX <= MIN_DISTANCE_X ||
				_targetY >= MAX_DISTANCE_Y ||
				_targetY <= MIN_DISTANCE_Y ||
				_targetZ >= MAX_DISTANCE_Z ||
				_targetZ <= MIN_DISTANCE_Z)
			return false;
		return true;
	}

	public int nextInt(int lower, int upper)
	{
		return _random.nextInt(1 + upper - lower) + lower;
	}

	public double nextDouble(double lower, double upper)
	{
		return lower + (upper - lower) * _random.nextDouble();
	}

	public void debug(String s)
	{
		if (DEBUG) System.out.println(s);
	}

	public void remove()
	{
		_target = null;
		UtilServer.Unregister(this);
		Managers.get(DisguiseManager.class).undisguise(_disguise);
		_armorStand.remove();
		_nmsEntity = null;
		_armorStand = null;
		_center = null;
	}

	public ArmorStand getEntity()
	{
		return _armorStand;
	}

	public void moveDelta(double dx, double dy, double dz)
	{
		_nmsEntity.locX += dx;
		_nmsEntity.locY += dy;
		_nmsEntity.locZ += dz;
	}

	public void move(double x, double y, double z)
	{
		_nmsEntity.locX = x;
		_nmsEntity.locY = y;
		_nmsEntity.locZ = z;
	}
}
