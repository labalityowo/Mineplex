package nautilus.game.arcade.game.games.moba.progression;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.hologram.Hologram;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.managers.lobby.current.NewGameLobbyManager;

public class MobaUnlockAnimation implements Listener
{

	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.withColor(Color.ORANGE)
			.withFlicker()
			.with(Type.BURST)
			.build();

	private final Moba _host;
	private final Player _player;
	private final HeroKit _kit;
	private final Location _spawn;
	private final Location _toTeleport;
	private final Location _npc;
	private final Location _info;
	private final long _start;

	private ArmorStand _npcEntity;
	private int _animationStage;
	private Hologram _skillInfo;

	public MobaUnlockAnimation(Moba host, Player player, HeroKit kit)
	{
		_host = host;
		_player = player;
		_kit = kit;

		Map<String, List<Location>> lobbyLocations = ((NewGameLobbyManager) host.getArcadeManager().GetLobby()).getCustomLocs();
		_spawn = lobbyLocations.get("SPAWN").get(0);
		_toTeleport = lobbyLocations.get("HERO_UNLOCK PLAYER").get(0);
		_npc = lobbyLocations.get("HERO_UNLOCK NPC").get(0);
		_info = lobbyLocations.get("HERO_UNLOCK INFO_1").get(0);

		Vector dir = UtilAlg.getTrajectory(_toTeleport, _npc);
		_toTeleport.setYaw(UtilAlg.GetYaw(dir));
		_npc.setYaw(UtilAlg.GetYaw(dir.clone().multiply(-1)));

		_start = System.currentTimeMillis();

		UtilServer.RegisterEvents(this);
		_host.getProgression().setCurrentAnimation(this);
		start();
	}

	public void start()
	{
		_player.sendMessage(F.main("Game", "Unlocking " + _kit.getRole().getChatColor() + _kit.GetName() + "."));
		_player.teleport(_toTeleport);

		_npcEntity = _npc.getWorld().spawn(_npc, ArmorStand.class);
		GameProfile profile = new GameProfile(UUID.randomUUID(), SkinData.getUnusedSkullName());
		profile.getProperties().clear();
		profile.getProperties().put("textures", _kit.getSkinData().getProperty());

		DisguisePlayer disguise = new DisguisePlayer(_npcEntity, profile);
		disguise.getHologram()
				.setText(_kit.getRole().getChatColor() + C.Bold + _kit.GetName());
		_host.getArcadeManager().GetDisguise().disguise(disguise);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		switch (_animationStage)
		{
			case 0:
				if (!UtilTime.elapsed(_start, 2000))
				{
					UtilParticle.PlayParticleToAll(ParticleType.CLOUD, _npcEntity.getLocation().add(0, 1.5, 0), 1, 1, 1, 0, 50, ViewDist.NORMAL);
				}
				else
				{
					_animationStage++;
				}
				break;
			case 1:
				_npcEntity.getWorld().strikeLightningEffect(_npcEntity.getLocation());

				String[] text = new String[_kit.GetPerks().length + 1];
				int i = 0;
				text[i++] = C.cAqua + "Skills";

				for (Perk perk : _kit.GetPerks())
				{
					text[i++] = C.cYellow + perk.GetName();
				}

				_skillInfo = new Hologram(_host.getArcadeManager().getHologramManager(), _info, text);
				_skillInfo.start();

				for (int j = 0; j < 10; j++)
				{
					UtilFirework.playFirework(UtilAlg.getRandomLocation(_npcEntity.getLocation(), 4, 0, 4), FIREWORK_EFFECT);
				}

				_animationStage++;
				break;
			case 2:
				if (UtilTime.elapsed(_start, 12000))
				{
					_player.sendMessage(F.main("Game", "Unlocked " + F.name(_kit.getRole().getChatColor() + _kit.GetName()) + ". You can now select them at the start of the game!"));
					_player.teleport(_spawn);
					_player.playSound(_player.getLocation(), Sound.LEVEL_UP, 1, 1.2F);
					cleanup();
					_animationStage++;
				}

				break;
		}
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		if (!event.getPlayer().equals(_player))
		{
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
		{
			return;
		}

		event.setTo(event.getFrom());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		if (event.getPlayer().equals(_player))
		{
			cleanup();
		}
	}

	private void cleanup()
	{
		UtilServer.Unregister(this);
		_npcEntity.remove();
		_skillInfo.stop();
		_host.getProgression().setCurrentAnimation(null);
	}
}
