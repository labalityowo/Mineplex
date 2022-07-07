package nautilus.game.minekart.kart;

import java.util.HashMap;
import java.util.HashSet;

import nautilus.game.minekart.gp.GP;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.kart.Kart.DriftDirection;
import nautilus.game.minekart.kart.condition.Condition;
import nautilus.game.minekart.kart.condition.ConditionData;
import nautilus.game.minekart.kart.condition.ConditionType;
import nautilus.game.minekart.kart.control.*;
import nautilus.game.minekart.kart.crash.Crash;
import nautilus.game.minekart.track.Track.TrackState;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.MathHelper;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.Packet34EntityTeleport;

import org.bukkit.Location;
import org.bukkit.Sound;

import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;

public class KartManager extends MiniPlugin
{
	private Recharge _recharge;
	
	public KartItemManager ItemManager;

	private HashMap<Player, Kart> _karts = new HashMap<Player, Kart>();

	public KartManager(JavaPlugin plugin, Recharge recharge)
	{
		super("Kart Manager", plugin);
		
		_recharge = recharge;
		ItemManager = new KartItemManager(plugin, this);
	}

	@EventHandler
	public void KartUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTER)
		{
			for (Kart kart : GetKarts().values())
			{
				Condition.WolfHeart(kart);
				Condition.SuperMushroom(kart);
			}
		}

		if (event.getType() == UpdateType.FAST)
		{
			for (Kart kart : GetKarts().values())
			{
				Location location = kart.GetDriver().getLocation();
				Packet teleportPacket = new Packet34EntityTeleport(
						kart.GetDriver().getEntityId(),
						MathHelper.floor(location.getX() * 32.0D),
						MathHelper.floor(location.getY() * 32.0D),
						MathHelper.floor(location.getZ() * 32.0D),
						(byte) ((int) (MathHelper.d(location.getYaw() * 256.0F / 360.0F))),
						(byte) ((int) (MathHelper.d(location.getPitch() * 256.0F / 360.0F))));

				for (Kart otherPlayer : kart.GetGP().GetKarts())
				{
					if (kart.GetGP().GetTrack().GetState() != TrackState.Live)
						break;

					if (otherPlayer.GetDriver() == kart.GetDriver())
						continue;

					EntityPlayer entityPlayer = ((CraftPlayer) otherPlayer
							.GetDriver()).getHandle();

					entityPlayer.playerConnection.sendPacket(teleportPacket);
				}
			}
		}

		if (event.getType() == UpdateType.TICK)
		{
			for (Kart kart : GetKarts().values())
			{
				// Physics
				World.Gravity(kart);

				// Drag
				World.AirDrag(kart);
				World.FireDrag(kart);
				World.RoadDrag(kart);
				World.BlockDrag(kart);

				// Conditions
				Condition.Boost(kart);
				Condition.LightningSlow(kart);
				Condition.StarEffect(kart);
				Condition.StarCollide(kart);
				Condition.BlazeFire(kart);

				// Collision
				Collision.CollideBlock(kart);
				Collision.CollidePlayer(kart);

				// Movement
				if (kart.GetKartState() == KartState.Drive)
				{
					// Start/Stop
					Drive.Accelerate(kart);
					Drive.Brake(kart);

					// Turn
					if (kart.GetDrift() == DriftDirection.None)
						Drive.Turn(kart);
					else
						DriveDrift.Drift(kart);

					// Speed Limit
					Drive.TopSpeed(kart);

					// Move Player
					Drive.Move(kart);

					kart.GetDriver()
							.getWorld()
							.playSound(kart.GetDriver().getLocation(),
									Sound.PIG_IDLE, .15f - (float)kart.GetSpeed() / 10,
									.5f + (float)kart.GetSpeed());
				}
				if (kart.GetKartState() == KartState.Crash)
				{
					Crash crash = kart.GetCrash();

					if (crash == null
							|| (crash.CrashEnd() && KartUtil.IsGrounded(kart)))
					{
						// State Return
						kart.SetKartState(KartState.Drive);

						// Restore Stability
						if (crash == null || crash.StabilityRestore())
							kart.GetDriver().setFoodLevel(20);

						continue;
					}

					crash.Move(kart);
				}
				if (kart.GetKartState() == KartState.Lakitu)
				{
					if (UtilTime.elapsed(kart.GetKartStateTime(), 8000))
					{
						kart.SetKartState(KartState.Drive);
					} else
					{
						kart.GetDriver().playSound(
								kart.GetDriver().getLocation(),
								Sound.NOTE_BASS, 0.3f, 0.1f);
					}

					// Move Player
					Drive.Move(kart);
				}
			}
		}
	}

	@EventHandler
	public void ConditionExpire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Kart kart : GetKarts().values())
		{
			HashSet<ConditionData> remove = new HashSet<ConditionData>();

			for (ConditionData data : kart.GetConditions())
			{
				if (data.IsExpired())
					remove.add(data);
			}

			for (ConditionData data : remove)
			{
				kart.GetConditions().remove(data);

				if (data.IsCondition(ConditionType.Star)
						|| data.IsCondition(ConditionType.Ghost))
				{
					kart.SetPlayerArmor();
				}

				if (data.IsCondition(ConditionType.SuperMushroom))
				{
					kart.SetItemStored(null);
				}
			}
		}
	}

	@EventHandler
	public void StabilityRecover(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Kart kart : GetKarts().values())
			UtilPlayer.hunger(kart.GetDriver(), 1);
	}

	@EventHandler
	public void DriftHop(PlayerToggleSneakEvent event)
	{
		if (_recharge.use(event.getPlayer(), "Drift Hop", 250, false))
			DriveDrift.Hop(GetKart(event.getPlayer()), event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Damage(EntityDamageEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Damage(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() != RegainReason.CUSTOM)
			event.setCancelled(true);
	}

	public HashMap<Player, Kart> GetKarts()
	{
		return _karts;
	}

	public Kart GetKart(Player player)
	{
		return _karts.get(player);
	}

	public void AddKart(Player player, KartType type, GP gp)
	{
		RemoveKart(player);

		_karts.put(player, new Kart(player, type, gp));
	}

	public void RemoveKart(Player player)
	{
		_karts.remove(player);
		UtilInv.Clear(player);
	}
}
