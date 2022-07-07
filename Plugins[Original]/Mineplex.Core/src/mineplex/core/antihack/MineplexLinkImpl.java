package mineplex.core.antihack;

import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.MobEffect;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.mineplex.anticheat.api.AABB;
import com.mineplex.anticheat.api.MineplexLink;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseBase;

public class MineplexLinkImpl implements MineplexLink
{
	private final DisguiseManager _disguiseManager = Managers.require(DisguiseManager.class);

	private final RegisteredServiceProvider<RelationProvider> _relationProvider = Bukkit.getServicesManager().getRegistration(RelationProvider.class);

	@Override
	public EntityType getActiveDisguise(Player player)
	{
		DisguiseBase disguise = _disguiseManager.getActiveDisguise(player);
		return disguise != null ? disguise.getDisguiseType() : null;
	}

	@Override
	public boolean isSpectator(Player player)
	{
		return UtilPlayer.isSpectator(player);
	}

	@Override
	public double getTPS()
	{
		return MinecraftServer.getServer().recentTps[0]; // Return the average TPS from the last minute
	}

	@Override
	public int getPing(Player player)
	{
		return Math.min(((CraftPlayer) player).getHandle().ping, 1000);
	}

	@Override
	public boolean isUsingItem(Player player)
	{
		return ((CraftPlayer) player).getHandle().bS(); // See Anticheat javadoc
	}

	@Override
	public int allocateNewEntityID()
	{
		return UtilEnt.getNewEntityId();
	}

	@Override
	public boolean isUsingElytra(Player player)
	{
		return ((CraftPlayer) player).getHandle().isGliding();
	}

	@Override
	public int getLevitationAmplifier(Player player)
	{
		MobEffect effect = ((CraftPlayer) player).getHandle().effects.get(PotionEffectType.LEVITATION.getId());
		return effect == null ? -1 : effect.getAmplifier();
	}

	@Override
	public boolean canDamage(Player attacker, Entity target)
	{
		return _relationProvider != null && _relationProvider.getProvider().canDamage(attacker, target);
	}

	@Override
	public Set<AABB> nmsGetCubes(World world, AABB aabb, Player player)
	{
		Vector min = aabb.getMin();
		Vector max = aabb.getMax();

		return ((CraftWorld)world).getHandle().getCubes(((CraftEntity)player).getHandle(), new AxisAlignedBB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()))
				.stream()
				.map(axisAlignedBB -> new AABB(axisAlignedBB.a, axisAlignedBB.b, axisAlignedBB.c, axisAlignedBB.d, axisAlignedBB.e, axisAlignedBB.f))
				.collect(Collectors.toSet());
	}
}