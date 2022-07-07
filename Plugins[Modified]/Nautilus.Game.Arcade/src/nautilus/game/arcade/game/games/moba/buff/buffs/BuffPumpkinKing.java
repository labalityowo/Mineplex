package nautilus.game.arcade.game.games.moba.buff.buffs;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.buff.Buff;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public class BuffPumpkinKing extends Buff<Player>
{

	private static final long DURATION = TimeUnit.MINUTES.toMillis(1);
	private static final String DAMAGE_REASON = "Boss Buff";
	private static final double DAMAGE_FACTOR = 1.5;

	private final ItemStack _helmet;

	public BuffPumpkinKing(Moba host, Player entity, ItemStack helmet)
	{
		super(host, entity, DURATION);

		_helmet = helmet;
	}

	@Override
	public void onApply()
	{
		_entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60 * 20, 1));
		UtilParticle.PlayParticleToAll(ParticleType.LAVA, _entity.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.1F, 10, ViewDist.LONG);
		_entity.playSound(_entity.getLocation(), Sound.PORTAL_TRAVEL, 1, 0.5F);
		_entity.sendMessage(F.main("Game", "You feel a " + F.elem("Great Power") + " flow through you. Your " + F.elem("Damage") + " and " + F.elem("Regeneration") + " are increased!"));
	}

	@Override
	public void onExpire()
	{
		sendFakeHelmet(_entity, _entity.getInventory().getHelmet());
	}

	@EventHandler
	public void updateFakeHelmet(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		sendFakeHelmet(_entity, _helmet);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damageIncrease(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(true);

		if (damager == null || !damager.equals(_entity))
		{
			return;
		}

		damagee.getWorld().playEffect(damagee.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		event.AddMod(DAMAGE_REASON, DAMAGE_FACTOR);
	}

	private void sendFakeHelmet(Player player, ItemStack itemStack)
	{
		// Magic number 4 means helmet
		PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), 4, CraftItemStack.asNMSCopy(itemStack));

		for (Player other : Bukkit.getOnlinePlayers())
		{
			// Don't send wearer their own data
			if (other.equals(player))
			{
				continue;
			}

			UtilPlayer.sendPacket(other, packet);
		}
	}

}
