package mineplex.core.twofactor;

import java.util.Optional;

public class TwoFactorData
{
    private String _secretKey;
    private String _lastIp;

    public Optional<String> getSecretKey()
    {
        return Optional.ofNullable(_secretKey);
    }

    public Optional<String> getLastLoginIp()
    {
        return Optional.ofNullable(_lastIp);
    }

    public void setSecretKey(String secretKey)
    {
        _secretKey = secretKey;
    }

    public void setLastLoginIp(String lastIp)
    {
        _lastIp = lastIp;
    }
}
