package nautilus.game.arcade.game.games.paintball;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.itemstack.ItemStackFactory;
import nautilus.game.arcade.game.Game;

public class PlayerCopyPaintball 
{
	private Game Host;

	private Player _owner;
	
	private ArmorStand _ent;
	private Hologram _holo;
	private Hologram _saveMe;
	
	private boolean _saveMeFlop = false;
	
	public PlayerCopyPaintball(Game host, Player owner, Player paintedBy, ChatColor nameColor)
	{
		Host = host;
		
		_owner = owner;
		
		Location entLoc = owner.getLocation();
		entLoc.setPitch(0F);
		
		Host.CreatureAllowOverride = true;
		_ent = owner.getWorld().spawn(owner.getLocation(), ArmorStand.class);
		Host.CreatureAllowOverride = false;
		
		UtilEnt.ghost(_ent, true, false);
		
		UtilEnt.vegetate(_ent);
		
		_ent.setArms(true);
		_ent.setBasePlate(false);
		_ent.setVisible(false);
		
		//Rand pose
		int rA = UtilMath.r(20) - 3;
		int lA = UtilMath.r(20) - 3;
		int rL = UtilMath.r(20) - 3;
		int lL = UtilMath.r(20) - 3;
		_ent.setRightArmPose(new EulerAngle(Math.toRadians(rA < 0 ? 360 - rA : rA), 0, Math.toRadians(Math.abs(rA))));
		_ent.setRightLegPose(new EulerAngle(Math.toRadians(rL < 0 ? 360 - rL : rL), 0, Math.toRadians(Math.abs(rL))));
		_ent.setLeftArmPose(new EulerAngle(Math.toRadians(lA < 0 ? 360 - lA : lA), 0, Math.toRadians(360 - Math.abs(lA))));
		_ent.setLeftLegPose(new EulerAngle(Math.toRadians(lL < 0 ? 360 - lL : rA), 0, Math.toRadians(360 - Math.abs(lL))));
		
		//Armor
		_ent.getEquipment().setArmorContents(owner.getInventory().getArmorContents());
		_ent.setItemInHand(owner.getItemInHand());
		
		//Player skull
		ItemStack skull = ItemStackFactory.Instance.CreateStack(Material.SKULL_ITEM, (byte) 3, 1);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(owner.getName());
		skull.setItemMeta(meta);
		_ent.setHelmet(skull);
		
		//Name
		_holo = new Hologram(host.Manager.getHologramManager(), _ent.getLocation().clone().add(0, 2.2, 0));
		_holo.setText(C.cWhite + C.Bold + C.Scramble + "XX" + ChatColor.RESET + " " + nameColor + owner.getName() + " " + C.cWhite + C.Bold + C.Scramble + "XX", C.cWhite + "Painted by " + host.GetTeam(paintedBy).GetColor() + paintedBy.getName());
		_holo.setHologramTarget(HologramTarget.WHITELIST);
		_holo.setFollowEntity(_ent);
		_holo.start();
		
		//Save me
		_saveMe = new Hologram(host.Manager.getHologramManager(), _ent.getLocation().clone().add(0, 2.8, 0));
		_saveMe.setText(C.cRedB + "SAVE ME!");
		_saveMe.setHologramTarget(HologramTarget.WHITELIST);
		_saveMe.setFollowEntity(_ent);
		_saveMe.start();
	}

	public LivingEntity GetEntity() 
	{
		return _ent;
	}

	public Player GetPlayer() 
	{
		return _owner;
	}
	
	public Hologram GetHolo()
	{
		return _holo;
	}
	
	public Hologram GetSaveMe()
	{
		return _saveMe;
	}
	
	public boolean getSaveMeFlop()
	{
		return _saveMeFlop;
	}
	
	public void setSaveMeFlop(boolean flop)
	{
		_saveMeFlop = flop;
	}
}
