package mineplex.core.account.repository.token;

import java.util.HashSet;
import java.util.List;


public class AccountToken
{
    public int AccountId;
    public String Name;
    public Rank Rank;
    
    public int LoginCount;
    public long LastLogin;
    public long TotalPlayingTime;
    public HashSet<String> IpAdddresses = new HashSet<String>();
    
    public boolean Banned;
    public String Reason;
    
    public int BlueGems;
    public int GreenGems;
    public List<Integer> SalesPackageIds;
}