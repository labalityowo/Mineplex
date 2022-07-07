package nautilus.game.arcade.game.modules.antixray;

import mineplex.core.common.util.UtilServer;
import nautilus.game.arcade.game.modules.Module;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/*
 * This module will enable antixray for this specific game
 *
 * NOTE: The game server must also have the Mineplex Orebfuscator plugin installed
 */
public class AntiXrayModule extends Module
{
	private AntiXrayService _service;

	@Override
	protected void setup()
	{
		RegisteredServiceProvider<AntiXrayService> rsp = UtilServer.getServer().getServicesManager().getRegistration(AntiXrayService.class);
		if (rsp == null)
		{
			getGame().unregisterModule(this);
			System.out.println("!!!ERROR!!! AntiXray module was registered but the Mineplex Orebfuscator Service was not registered");
			return;
		}
		AntiXrayService service = rsp.getProvider();
		if (service == null)
		{
			getGame().unregisterModule(this);
			System.out.println("!!!ERROR!!! AntiXray module was registered but the Mineplex Orebfuscator Service was null");
			return;
		}
		_service = service;
	}

	@Override
	public void cleanup()
	{
		_service.setEnabled(false);
		_service.clearCache();
	}

	public AntiXrayModule setEnabled(boolean enabled)
	{
		_service.setEnabled(enabled);
		return this;
	}

	public AntiXrayModule setUpdateOnDamage(boolean updateOnDamage)
	{
		_service.setUpdateOnDamage(updateOnDamage);
		return this;
	}

	public AntiXrayModule setEngineMode(int engineMode)
	{
		_service.setEngineMode(engineMode);
		return this;
	}

	public AntiXrayModule setInitialRadius(int initialRadius)
	{
		_service.setInitialRadius(initialRadius);
		return this;
	}

	public AntiXrayModule setUpdateRadius(int updateRadius)
	{
		_service.setUpdateRadius(updateRadius);
		return this;
	}

	public AntiXrayModule setUseProximityHider(boolean useProximityHider)
	{
		_service.setUseProximityHider(useProximityHider);
		return this;
	}

	public AntiXrayModule setAntiTexturePacksAndFreecam(boolean antiTexturePacksAndFreecam)
	{
		_service.setAntiTexturePacksAndFreecam(antiTexturePacksAndFreecam);
		return this;
	}

	public AntiXrayModule setDarknessHideBlocks(boolean darknessHideBlocks)
	{
		_service.setDarknessHideBlocks(darknessHideBlocks);
		return this;
	}

	public AntiXrayModule setObfuscateBlocks(Material... materials)
	{
		return setObfuscateBlocks(Arrays.asList(materials));
	}

	public AntiXrayModule setObfuscateBlocks(List<Material> materials)
	{
		_service.setObfuscateBlocks(materials);
		return this;
	}

	public AntiXrayModule setRandomBlocks(Material... materials)
	{
		return setRandomBlocks(Arrays.asList(materials));
	}

	public AntiXrayModule setRandomBlocks(List<Material> materials)
	{
		_service.setRandomBlocks(materials);
		return this;
	}

	public AntiXrayModule setProximityBlocks(Material... materials)
	{
		return setProximityBlocks(Arrays.asList(materials));
	}

	public AntiXrayModule setProximityBlocks(List<Material> materials)
	{
		_service.setProximityBlocks(materials);
		return this;
	}
	public AntiXrayModule setDarknessBlocks(Material... materials)
	{
		return setDarknessBlocks(Arrays.asList(materials));
	}

	public AntiXrayModule setDarknessBlocks(List<Material> materials)
	{
		_service.setDarknessBlocks(materials);
		return this;
	}

	public AntiXrayModule setUseCache(boolean useCache)
	{
		_service.setUseCache(true);
		return this;
	}
}
