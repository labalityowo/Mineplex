package mineplex.core.profileCache;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;

public class ProfileCacheLookupCallback implements ProfileLookupCallback 
{
	private ProfileCacheManager _profileCacheManager;
	private ProfileLookupCallback _profileLookupCallback;
	
	public ProfileCacheLookupCallback(ProfileCacheManager profileCacheManager, ProfileLookupCallback profileLookupCallback) 
	{
		_profileCacheManager = profileCacheManager;
		_profileLookupCallback = profileLookupCallback;
	}

	@Override
	public void onProfileLookupFailed(GameProfile gameProfile, Exception exception) 
	{
		_profileLookupCallback.onProfileLookupFailed(gameProfile, exception);
	}

	@Override
	public void onProfileLookupSucceeded(GameProfile gameProfile) 
	{
		_profileCacheManager.cacheProfile(gameProfile);
		_profileLookupCallback.onProfileLookupSucceeded(gameProfile);
	}
}
