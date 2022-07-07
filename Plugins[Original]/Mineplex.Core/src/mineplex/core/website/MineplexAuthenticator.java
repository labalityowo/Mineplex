package mineplex.core.website;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class MineplexAuthenticator extends Authenticator
{
	private String _username, _password;
	
	public MineplexAuthenticator(String username, String password)
	{
		_username = username;
		_password = password;
	}
	
	@Override
	public PasswordAuthentication getPasswordAuthentication()
	{
		System.out.println("-==Mineplex Authentication In Progress==-");
		System.out.println("Requesting Host: " + getRequestingHost());
		System.out.println("Requesting Port: " + getRequestingPort());
		System.out.println("Requesting Prompt: " + getRequestingPrompt());
		System.out.println("Requesting Protocol: " + getRequestingProtocol());
		System.out.println("Requesting Scheme: " + getRequestingScheme());
		System.out.println("Requesting Site: " + getRequestingSite());
		return new PasswordAuthentication(_username, _password.toCharArray());
	}
}