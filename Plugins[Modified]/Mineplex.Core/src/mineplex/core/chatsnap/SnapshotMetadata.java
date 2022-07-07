package mineplex.core.chatsnap;

import java.util.Optional;

public class SnapshotMetadata
{
	private final int _id;
	protected String _token = null;
	protected Integer _creatorId = null;

	public SnapshotMetadata(int id, String token, Integer creatorId)
	{
		_id = id;
		_token = token;
		_creatorId = creatorId;
	}

	public SnapshotMetadata(int id, Integer creatorId)
	{
		_id = id;
		_creatorId = creatorId;
	}

	public SnapshotMetadata(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public Optional<String> getToken()
	{
		return Optional.ofNullable(_token);
	}

	public Optional<Integer> getCreatorId()
	{
		return Optional.ofNullable(_creatorId);
	}
}
