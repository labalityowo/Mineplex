package mineplex.core.gadget.gadgets.wineffect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.animation.AnimationPoint;
import mineplex.core.common.animation.AnimatorEntity;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectLoveIsABattlefield extends WinEffectGadget
{

	private DisguisePlayer _npc;
	private int _ticks = 0;
	private List<ArmorStand> _nonTeamArmorStands = new ArrayList<>();

	public WinEffectLoveIsABattlefield(GadgetManager manager)
	{
		super(manager, "Love is a Battlefield", UtilText.splitLineToArray(C.cGray + "Don't hate the players. Hate the game.", LineFormat.LORE),
				-17, Material.WOOL, (byte) 6);
		_schematicName = "WinRoomLove";
	}

	@Override
	public void teleport()
	{
		Location loc = getBaseLocation().add(getBaseLocation().getDirection().normalize().multiply(17)).add(0, 3, 0);
		loc.setDirection(getBaseLocation().clone().subtract(loc).toVector());
		super.teleport(loc);
	}

	@Override
	public void play()
	{
		_npc = getNPC(this._player, getBaseLocation());

		AnimatorEntity animator = new AnimatorEntity(Manager.getPlugin(), _npc.getEntity().getBukkitEntity());

		animator.addPoint(new AnimationPoint(20, new Vector(0,0,0), new Vector(-1, 0.5, 0)));
		animator.addPoint(new AnimationPoint(40, new Vector(0,0,0), new Vector( 0, 0.5,-1)));
		animator.addPoint(new AnimationPoint(60, new Vector(0,0,0), new Vector( 1, 0.5, 0)));
		animator.addPoint(new AnimationPoint(80, new Vector(0,0,0), new Vector( 0, 0.5, 1)));

		animator.setRepeat(true);

		Location loc = _npc.getEntity().getBukkitEntity().getLocation();
		loc.setDirection(new Vector(0,  0.5,  1));
		animator.start(loc);

		spawnNonTeam();
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{

		if(!isRunning()) return;

		if (event.getType() == UpdateType.TICK)
		{
			_ticks++;
			if (_ticks == 70)
				knockPlayers();
		}

		if(event.getType() == UpdateType.FASTER)
		{
			_npc.sendHit();
		}

		if(event.getType() != UpdateType.FAST) return;

		Location loc = getBaseLocation();

		for(int i = 0; i < 3; i++)
		{
			double r = 3;
			double rad = (((Math.PI*2)/3.0)*i) + ((event.getTick()%240) * Math.PI/120.0);
			double x = Math.sin(rad) * r;
			double z = Math.cos(rad) * r;

			Location l = loc.clone().add(x, 0, z);
			UtilParticle.PlayParticle(UtilParticle.ParticleType.HEART, l, 0.75f, 0.75f, 0.75f, 0.5f, 5, UtilParticle.ViewDist.NORMAL);
		}
	}

	@Override
	public void finish()
	{
		Manager.getDisguiseManager().undisguise(_npc);
		_npc = null;
	}

	private void knockPlayers()
	{
		for (ArmorStand armorStand : _nonTeamArmorStands)
		{
			armorStand.setVelocity(armorStand.getLocation().getDirection().multiply(-1).multiply(5));
		}
	}

	private void spawnNonTeam()
	{
		int i = 0;
		List<Location> circle = UtilShapes.getPointsInCircle(getBaseLocation(), _nonTeam.size(), 2.5);
		for (Player player : _nonTeam)
		{
			Location loc = circle.get(i);
			DisguisePlayer disguisePlayer = getNPC(player, loc);
			ArmorStand armorStand = (ArmorStand) disguisePlayer.getEntity().getBukkitEntity();
			Vector direction = _player.getEyeLocation().toVector().subtract(armorStand.getEyeLocation().toVector());
			Location teleport = armorStand.getLocation().setDirection(direction);
			armorStand.teleport(teleport);
			armorStand.setGravity(true);
			_nonTeamArmorStands.add(armorStand);
			i++;
		}
	}

}