package mineplex.core.gadget.gadgets.kitselector;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.KitSelectorGadget;

public class SingleParticleKitSelector extends KitSelectorGadget
{

	private UtilParticle.ParticleType _particleType;
	private int _particleCount;

	public SingleParticleKitSelector(GadgetManager manager, String name, String[] lore, int cost, Material mat, byte data,
									 UtilParticle.ParticleType particleType, int particleCount, String... alternativeSalesPackageNames)
	{
		super(manager, name, lore, cost, mat, data, alternativeSalesPackageNames);
		_particleType = particleType;
		_particleCount = particleCount;
	}

	@Override
	public void playParticle(Entity entity, Player playTo)
	{
		UtilParticle.PlayParticle(_particleType, entity.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, _particleCount, UtilParticle.ViewDist.NORMAL, playTo);
	}

	public enum SingleParticleSelectors
	{
		FLAMES_OF_FURY("Flames Of Fury", UtilText.splitLinesToArray(new String[]{C.cGray + "Through the Fire and the Flames we will ride"}, LineFormat.LORE),
				0, Material.BLAZE_POWDER, (byte) 0, UtilParticle.ParticleType.FLAME),
		EMBER("Ember", UtilText.splitLinesToArray(new String[]{C.cGray + "I'd like my kit well done."}, LineFormat.LORE),
				0, Material.COAL, (byte) 0, UtilParticle.ParticleType.SMOKE, 3),
		LOVE("Kit Love", UtilText.splitLinesToArray(new String[]{C.cGray + "I think I LIKE this kit, if you know what I mean."}, LineFormat.LORE),
				0, Material.POTION, (byte) 8233, UtilParticle.ParticleType.HEART),
		;

		private String _name;
		private String[] _lore;
		private int _cost;
		private Material _material;
		private byte _data;
		private UtilParticle.ParticleType _particleType;
		private int _particleCount = 1;
		private String[] _alternativeSalesPackageNames;

		SingleParticleSelectors(String name, String[] lore, int cost, Material material, byte data, UtilParticle.ParticleType particleType, String... alternativeSalesPackageNames)
		{
			_name = name;
			_lore = lore;
			_cost = cost;
			_material = material;
			_data = data;
			_particleType = particleType;
			_alternativeSalesPackageNames = alternativeSalesPackageNames;
		}

		SingleParticleSelectors(String name, String[] lore, int cost, Material material, byte data, UtilParticle.ParticleType particleType, int particleCount, String... alternativeSalesPackageNames)
		{
			_name = name;
			_lore = lore;
			_cost = cost;
			_material = material;
			_data = data;
			_particleType = particleType;
			_alternativeSalesPackageNames = alternativeSalesPackageNames;
			_particleCount = particleCount;
		}

		public KitSelectorGadget getKitSelectorGadget(GadgetManager manager)
		{
			return new SingleParticleKitSelector(manager, _name, _lore, _cost, _material, _data, _particleType, _particleCount, _alternativeSalesPackageNames);
		}
	}

}
