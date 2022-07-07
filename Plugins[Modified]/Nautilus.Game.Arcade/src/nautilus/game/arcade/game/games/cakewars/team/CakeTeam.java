package nautilus.game.arcade.game.games.cakewars.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;

import mineplex.core.common.util.C;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.hologram.HologramManager;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.shop.CakeItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeNetherItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeResource;
import nautilus.game.arcade.game.games.cakewars.shop.CakeTeamItem;

public class CakeTeam
{

	private static final String[] EDGE_HOLOGRAM_TEXT =
			{
					C.cGold + "Capturing the beacons around",
					C.cGold + "the map spawns resources at",
					C.cGold + "your base!"
			};
	private static final String SHOP_HOLOGRAM_TEXT = C.cGold + "Click the NPCs to buy better gear!";
	private static final String CAKE_HOLOGRAM_TEXT = C.cGold + "Protect your cake at all costs!";
	private static final String GENERATOR_HOLOGRAM_TEXT = C.cGold + "Your resources spawn here!";

	private final CakeWars _game;
	private final GameTeam _team;
	private final Location _edge;
	private final Location _shop;
	private final Location _chest;
	private final Location _cake;
	private final Location _generator;
	private final Map<CakeTeamItem, Integer> _upgrades;
	private final List<Hologram> _tipHolograms;
	private final List<Hologram> _otherHolograms;

	private Hologram _cakeHologram;
	private Hologram _generatorHologram;

	CakeTeam(CakeWars game, GameTeam team, Location edgeHologram, Location shopHologram, Location chest, Location generator)
	{
		_game = game;
		_team = team;
		_edge = edgeHologram.add(0, 2, 0);
		_shop = shopHologram.add(0, 2, 0);
		_chest = chest.getBlock().getLocation();
		_generator = generator.add(0, 1, 0);

		_cake = game.WorldData.GetDataLocs(team.GetName().toUpperCase()).get(0).getBlock().getLocation().add(0.5, 0.5, 0.5);
		_cake.getBlock().setType(Material.CAKE_BLOCK);
		_upgrades = new HashMap<>();

		for (CakeItem item : game.generateItems(CakeResource.STAR))
		{
			_upgrades.put((CakeTeamItem) item, 0);
		}

		_tipHolograms = new ArrayList<>(3);
		_otherHolograms = new ArrayList<>(5);

		setupHolograms();
	}

	private void setupHolograms()
	{
		HologramManager hologramManager = _game.getArcadeManager().getHologramManager();

		_tipHolograms.add(new Hologram(hologramManager, _edge, EDGE_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start());

		_tipHolograms.add(new Hologram(hologramManager, _shop, SHOP_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start());

		_cakeHologram = new Hologram(hologramManager, _cake.clone().add(0.5, 1.3, 0.5), CAKE_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start();
		_tipHolograms.add(_cakeHologram);

		_generatorHologram = new Hologram(hologramManager, _generator, GENERATOR_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start();
		_otherHolograms.add(_generatorHologram);
	}

	public boolean canRespawn()
	{
		return _cake.getBlock().getType() == Material.CAKE_BLOCK;
	}

	public GameTeam getGameTeam()
	{
		return _team;
	}

	public Location getShop()
	{
		return _shop;
	}

	public Location getChest()
	{
		return _chest;
	}

	public Location getCake()
	{
		return _cake;
	}

	public Location getGenerator()
	{
		return _generator;
	}

	public Map<CakeTeamItem, Integer> getUpgrades()
	{
		return _upgrades;
	}

	public List<Hologram> getTipHolograms()
	{
		return _tipHolograms;
	}

	public List<Hologram> getOtherHolograms()
	{
		return _otherHolograms;
	}

	public Hologram getCakeHologram()
	{
		return _cakeHologram;
	}

	public Hologram getGeneratorHologram()
	{
		return _generatorHologram;
	}
}
