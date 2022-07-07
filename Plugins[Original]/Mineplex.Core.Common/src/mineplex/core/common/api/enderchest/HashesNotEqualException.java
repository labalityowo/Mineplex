package mineplex.core.common.api.enderchest;

import mineplex.core.common.api.ApiException;

public class HashesNotEqualException extends ApiException
{
	private String _hashFromServer;
	private String _calculatedHash;

	public HashesNotEqualException(String hashFromServer, String calculatedHash)
	{
		_hashFromServer = hashFromServer;
		_calculatedHash = calculatedHash;
	}

	public String getHashFromServer()
	{
		return _hashFromServer;
	}

	public String getGeneratedHash()
	{
		return _calculatedHash;
	}
}
