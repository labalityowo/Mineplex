package nautilus.game.arcade.game.games.bridge.animation;

import nautilus.game.arcade.game.games.bridge.Bridge;

public enum BridgeAnimationType
{

	WOOD(WoodBridgeAnimation.class, "BROWN", "GRAY"),
	ICE(IceBridgeAnimation.class, "LIGHT_BLUE"),
	LAVA(LavaBridgeAnimation.class, "BLACK", "RED", "ORANGE"),
	LILLY(LillyPadBridgeAnimation.class, "LIME"),
	MUSHROOM(MushroomBridgeAnimation.class, "PURPLE")
	
	;
	
	private final Class<? extends BridgeAnimation> _clazz;
	private final String[] _coloursUsed;
	
	private BridgeAnimationType(Class<? extends BridgeAnimation> clazz, String... coloursUsed)
	{
		_clazz = clazz;
		_coloursUsed = coloursUsed;
	}
	
	public BridgeAnimation createInstance(Bridge bridge)
	{
		try
		{
			return _clazz.cast(_clazz.getConstructor(Bridge.class).newInstance(bridge));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String[] getColoursUsed()
	{
		return _coloursUsed;
	}
	
}
