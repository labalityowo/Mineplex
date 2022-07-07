package mineplex.core.gadget.gadgets.wineffect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import net.minecraft.server.v1_8_R3.Packet;

public class WinEffectLightningStrike extends WinEffectGadget
{
	
	public WinEffectLightningStrike(GadgetManager manager)
	{
		super(manager, "Lightning Strike", UtilText.splitLineToArray(C.cGray + "They say lightning doesn't strike twice, but they must lose a lot.", LineFormat.LORE),
				-2, Material.DIAMOND_SWORD, (byte) 0);
		
	}

	@Override
	public void play()
	{
		final DisguisePlayer player = getNPC(this._player, getBaseLocation());
		Bukkit.getScheduler().runTaskLater(Manager.getPlugin(),
		new Runnable()
		{
			public void run()
			{
				getBaseLocation().getWorld().strikeLightningEffect(getBaseLocation());
				UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, getBaseLocation().add(0, 1, 0), 0.3f, 0.6f, 0.3f, 0.07f, 400, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, getBaseLocation().add(0, 0.3, 0), 0.7f, 0.1f, 0.7f, 0.07f, 400, ViewDist.NORMAL);
				
				
				player.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
				player.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
				player.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE)); 
				player.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
				player.setHeldItem(new ItemStack(Material.DIAMOND_SWORD));
				
				player.sendPacket(player.getEquipmentPackets().toArray(new Packet[5]));
			}
		}, 20*2);
	}

	
	
	@Override
	public void finish()
	{
	}


}
