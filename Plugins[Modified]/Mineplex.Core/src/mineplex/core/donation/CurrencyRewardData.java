package mineplex.core.donation;

import java.util.UUID;
import java.util.function.Consumer;

class CurrencyRewardData
{
	private final String _playerName;
	private final UUID _playerUUID;
	private final String _reason;
	private final int _amount;
	private final Consumer<Boolean> _callback;
	private int _attempts;

	CurrencyRewardData(String playerName, UUID playerUUID, String reason, int amount, Consumer<Boolean> callback)
	{
		_playerName = playerName;
		_playerUUID = playerUUID;
		_reason = reason;
		_amount = amount;
		_callback = callback;
	}

	int getAttempts()
	{
		return _attempts;
	}

	void incrementAttempts()
	{
		_attempts++;
	}

	String getPlayerName()
	{
		return _playerName;
	}

	UUID getPlayerUUID()
	{
		return _playerUUID;
	}

	String getReason()
	{
		return _reason;
	}

	int getAmount()
	{
		return _amount;
	}

	Consumer<Boolean> getCallback()
	{
		return _callback;
	}
}
