package mineplex.ddos;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import mineplex.ddos.api.ApiDeleteCall;
import mineplex.ddos.api.ApiGetCall;
import mineplex.ddos.api.ApiPostCall;
import mineplex.ddos.api.ApiPutCall;
import mineplex.ddos.api.token.DnsRecord;
import mineplex.ddos.api.token.DomainRecords;

public class DDoSProtectionSwitcher
{
	private static DnsMadeEasyRepository _repository = null;
	private static HashSet<ProcessRunner> _processes = new HashSet<ProcessRunner>();

	public static void main(String args[])
	{		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}

		_repository = new DnsMadeEasyRepository();
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");		

		//while (true)
		//{
			//if (_repository.switchToDDOSProt())
			//{

				System.out.println("Starting DDoS Protection Switch at " + dateFormat.format(new Date()));
				
				DomainRecords records = new ApiGetCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728,
						"/records", "").Execute(DomainRecords.class);
				List<DnsRecord> recordsToDelete = new ArrayList<DnsRecord>();
				List<DnsRecord> recordsToAdd = new ArrayList<DnsRecord>();
				List<DnsRecord> recordsToModify = new ArrayList<DnsRecord>();

				// Switch on ddos protection
				for (DnsRecord record : records.data)
				{
					if (record.type.equalsIgnoreCase("A"))
					{
						if (record.name.equalsIgnoreCase("neustar"))
						{
							record.name = "us";
							recordsToModify.add(record);
						}
						else if (record.name.equalsIgnoreCase("us"))
						{
							record.name = "us2";
							recordsToModify.add(record);
						}
						else if (record.name.equalsIgnoreCase("eu"))
						{
							record.name = "eu2";
							recordsToModify.add(record);
						}
					}
				}

				if (recordsToAdd.size() > 0)
				{
					new ApiPostCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728, "/records/", "createMulti")
							.Execute(records);
					System.out.println("Created " + recordsToAdd.size() + " records.");
				}

				if (recordsToModify.size() > 0)
				{
					new ApiPutCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728, "/records/", "updateMulti")
							.Execute(recordsToModify);
					System.out.println("Modified " + recordsToModify.size() + " records.");
				}

				if (recordsToDelete.size() > 0)
				{
					StringBuilder idBuilder = new StringBuilder();

					for (DnsRecord record : recordsToDelete)
					{
						if (idBuilder.length() != 0)
							idBuilder.append("&");

						idBuilder.append("ids=" + record.id);
					}

					new ApiDeleteCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728, "/records?"
							+ idBuilder.toString()).Execute();
					System.out.println("Deleted " + recordsToDelete.size() + " records.");
				}
/*
				// Switching US Bungees
				switchServer("10.35.74.130", "108.178.20.166", "108.163.222.202", "108.178.20.165", "108.163.222.201");
				switchServer("10.35.74.132", "108.163.217.110", "108.178.44.50", "108.163.217.109", "108.178.44.49");
				switchServer("10.35.74.142", "108.178.16.90", "108.178.16.90", "108.178.16.89", "108.178.16.89");
				switchServer("10.35.74.135", "108.163.254.134", "108.178.16.106", "108.163.254.133", "108.178.16.105");
				switchServer("10.35.74.137", "108.163.216.250", "108.178.34.162", "108.163.216.249", "108.178.34.161");
				switchServer("10.35.74.147", "108.163.216.106", "184.154.39.126", "108.163.216.105", "184.154.39.125");
				switchServer("10.35.74.143", "184.154.215.170", "108.178.17.6", "184.154.215.169", "108.178.17.5");
				switchServer("10.35.74.145", "96.127.174.206", "108.178.7.118", "96.127.174.205", "108.178.7.117");
				switchServer("10.35.74.144", "184.154.127.10", "184.154.39.154", "184.154.127.9", "184.154.39.153");
				switchServer("10.35.74.146", "96.127.174.146", "108.178.16.26", "96.127.174.145", "108.178.16.25");
				switchServer("10.35.74.149", "108.178.7.206", "107.6.158.198", "108.178.7.205", "107.6.158.197");
				switchServer("10.35.74.136", "184.154.39.146", "184.154.13.218", "184.154.39.145", "184.154.13.217");
				switchServer("10.35.74.139", "108.163.217.250", "108.178.44.134", "108.163.217.249", "108.178.44.133");
				switchServer("10.35.74.140", "69.175.15.242", "108.163.216.38", "69.175.15.241", "108.163.216.37");
				switchServer("10.35.74.141", "107.6.129.126", "96.127.182.218", "107.6.129.125", "96.127.182.217");
				switchServer("10.35.74.134", "108.163.222.174", "108.163.216.82", "108.163.222.173", "108.163.216.81");
				switchServer("10.32.214.248", "108.178.34.118", "107.6.129.170", "108.178.34.117", "107.6.129.169");
				switchServer("10.32.214.250", "69.175.4.38", "107.6.129.250", "69.175.4.37", "107.6.129.249");
				switchServer("10.32.214.249", "107.6.158.78", "184.154.13.38", "107.6.158.77", "184.154.13.37");
				switchServer("10.32.214.247", "184.154.13.118", "108.163.242.98", "184.154.13.117", "108.163.242.97");
*/
				/*
				// Switching EU Bungees
				switchServer("10.82.2.202", "107.6.176.194", "107.6.176.34", "107.6.176.193", "107.6.176.33");
				switchServer("10.82.2.204", "107.6.176.122", "107.6.176.50", "107.6.176.121", "107.6.176.49");
				switchServer("10.82.2.206", "107.6.176.166", "107.6.176.126", "107.6.176.165", "107.6.176.125");
				switchServer("10.83.27.77", "107.6.176.14", "107.6.176.98", "107.6.176.13", "107.6.176.97");
				switchServer("10.82.2.225", "107.6.176.114", "107.6.176.58", "107.6.176.113", "107.6.176.57");
				switchServer("10.82.2.227", "107.6.176.26", "107.6.176.46", "107.6.176.25", "107.6.176.45");
				switchServer("10.82.2.228", "107.6.176.110", "107.6.176.70", "107.6.176.109", "107.6.176.69");
				switchServer("10.82.2.226", "107.6.176.138", "107.6.176.234", "107.6.176.137", "107.6.176.233");
					*/			
				//sendMail();				
			//}

			int processWaits = 0;

			while (_processes.size() > 0)
			{
				for (Iterator<ProcessRunner> iterator = _processes.iterator(); iterator.hasNext();)
				{
					ProcessRunner pr = iterator.next();

					try
					{
						pr.join(100);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

					if (pr.isDone())
						iterator.remove();
				}

				if (_processes.size() > 0)
				{
					try
					{
						Thread.sleep(6000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				if (processWaits >= 60)
				{
					System.out.println("Killing stale processes.");

					for (Iterator<ProcessRunner> iterator = _processes.iterator(); iterator.hasNext();)
					{
						iterator.next().abort();
						iterator.remove();
					}
				}

				processWaits++;
			}

			processWaits = 0;

			try
			{
				Thread.sleep(60000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		//}
		/*
		 * // Switch off ddos protection for (DnsRecord record : records.data) {
		 * if (record.type.equalsIgnoreCase("CNAME")) { if
		 * (record.name.equalsIgnoreCase("eu")) recordsToDelete.add(record); }
		 * else if (record.type.equalsIgnoreCase("A")) { if
		 * (record.name.equalsIgnoreCase("us")) { record.name = "neustar";
		 * recordsToModify.add(record); } else if
		 * (record.name.equalsIgnoreCase("us2")) { record.name = "us";
		 * recordsToModify.add(record); } else if
		 * (record.name.equalsIgnoreCase("eu2")) { record.name = "eu";
		 * recordsToModify.add(record); } } }
		 * 
		 * 
		 * 
		 * recordsToAdd.add(new CNameRecord("eu", "us", 300));
		 */
	}

	private static void sendMail()
	{
		Message message = new MimeMessage(getSession());

		try
		{
			message.addRecipient(RecipientType.TO, new InternetAddress("ultrasupport@neustar.biz"));
			message.addFrom(new InternetAddress[] { new InternetAddress("it@mineplex.com") });

			message.setSubject("Start Mitigation Incident");
			message.setText("We need to start mitigation.\n\n"
					+ "Jonathan Williams\n"
					+ "Director of Gaming Software Development\n"
					+ "Mineplex, LLC\n"
					+ "PH: 805.231.0407\n"
					+ "http://www.mineplex.com");
			
			
			Transport.send(message);
			
			System.out.println("Sent Neustar Mitigation Email at " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
		}
		catch (AddressException e)
		{
			e.printStackTrace();
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
		}
	}

	private static Session getSession()
	{
		Authenticator authenticator = new Authenticator();

		Properties properties = new Properties();
		properties.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());		
		
		properties.setProperty("mail.smtp.host", "smtp.fatcow.com");
		properties.setProperty("mail.smtp.socketFactory.port", "465");
		properties.setProperty("mail.smtp.socketFactory.class",	"javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.port", "465");
		
		return Session.getInstance(properties, authenticator);
	}

	private static void switchServer(final String privateIp, String currentIp, String newIp, String currentGateway,
			String newGateway)
	{
		String cmd = "/usr/bin/ssh";
		String args = "-to StrictHostKeyChecking=no -o ServerAliveInterval=10 mineplex@" + privateIp + " -p 5191";
		String remoteCmd = "\"sh /home/mineplex/config/switchBungeeIpRemote.sh";
		String remoteCmdEnd = "\"";

		ProcessRunner pr = new ProcessRunner(new String[] { cmd, args, remoteCmd, currentIp, newIp,
				currentGateway, newGateway, remoteCmdEnd });
		pr.start(new GenericRunnable<Boolean>()
		{
			public void run(Boolean error)
			{
				if (error)
					System.out.println("[" + privateIp + "] Errored!");
				else
					System.out.println("[" + privateIp + "] Switched!");
			}
		});

		try
		{
			pr.join(500);
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}

		if (!pr.isDone())
			_processes.add(pr);
	}

	private static class Authenticator extends javax.mail.Authenticator
	{
		private PasswordAuthentication authentication;

		public Authenticator()
		{
			String username = "it@mineplex.com";
			String password = "BearT4bl312ust";
			authentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication()
		{
			return authentication;
		}
	}
}
