package nautilus.game.arcade.game.games.baconbrawl.kits;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBaconBlast;
import nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitMamaPig extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkBaconBlast(),
					new PerkSpeed(1)
			};

	public KitMamaPig(ArcadeManager manager)
	{
		super(manager, GameKit.BACON_BRAWL_MAMA_PIG, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));

		//Disguise
		DisguisePig disguise = new DisguisePig(player);
		disguise.setName(C.cYellow + player.getName());
		disguise.setCustomNameVisible(false);
		Manager.GetDisguise().disguise(disguise);

		Manager.GetGame().CreatureAllowOverride = true;
		Pig pig = player.getWorld().spawn(player.getEyeLocation(), Pig.class);
		pig.setBaby();
		pig.setAgeLock(true);
		pig.setCustomName(C.cYellow + player.getName());
		pig.setCustomNameVisible(false);
		Manager.GetGame().CreatureAllowOverride = false;

		player.setPassenger(pig);

		Manager.runSyncLater(() -> UtilPlayer.sendPacket(player, new PacketPlayOutEntityDestroy(new int[]{pig.getEntityId()})), 2);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void damageTransfer(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (!(event.GetDamageeEntity() instanceof Pig))
		{
			return;
		}

		Pig pig = (Pig) event.GetDamageeEntity();

		if (pig.getVehicle() == null || !(pig.getVehicle() instanceof LivingEntity))
		{
			return;
		}

		event.setDamagee((LivingEntity) pig.getVehicle());
	}
}
