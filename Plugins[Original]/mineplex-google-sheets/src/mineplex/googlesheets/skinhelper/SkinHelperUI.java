package mineplex.googlesheets.skinhelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mineplex.googlesheets.util.SkinFetcher;
import mineplex.googlesheets.util.UUIDFetcher;

public class SkinHelperUI extends JFrame
{

	private static final Map<String, String> UUID_CACHE = new HashMap<>();

	private static final Font FONT = new Font("Verdana", Font.PLAIN, 12);
	private static final long FETCH_WAIT_TIME = 30;
	private static final long FETCH_WAIT_MILLISECONDS = TimeUnit.SECONDS.toMillis(FETCH_WAIT_TIME);

	private long _lastFetch;

	public SkinHelperUI()
	{
		setTitle("Skin Helper");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 150, 300);
		JPanel contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTextField txtMinecraftName = new JTextField();
		txtMinecraftName.setFont(FONT);
		txtMinecraftName.setForeground(Color.WHITE);
		txtMinecraftName.setBackground(Color.GRAY);
		txtMinecraftName.setBounds(10, 55, 124, 20);
		txtMinecraftName.setColumns(10);
		contentPane.add(txtMinecraftName);
		
		JLabel lblMinecraftName = new JLabel("Minecraft Name");
		lblMinecraftName.setForeground(Color.WHITE);
		lblMinecraftName.setFont(FONT);
		lblMinecraftName.setBounds(10, 30, 100, 14);
		contentPane.add(lblMinecraftName);
		
		JButton btnOk = new JButton("OK");
		btnOk.setForeground(Color.WHITE);
		btnOk.setBackground(Color.DARK_GRAY);
		btnOk.setBounds(10, 86, 124, 23);
		contentPane.add(btnOk);
		
		JLabel lblSkinValue = new JLabel("Skin Value");
		lblSkinValue.setForeground(Color.WHITE);
		lblSkinValue.setFont(FONT);
		lblSkinValue.setBounds(10, 120, 100, 14);
		contentPane.add(lblSkinValue);
		
		JTextField txtSkinValue = new JTextField();
		txtSkinValue.setEditable(false);
		txtSkinValue.setForeground(Color.WHITE);
		txtSkinValue.setBackground(Color.GRAY);
		txtSkinValue.setFont(FONT);
		txtSkinValue.setColumns(10);
		txtSkinValue.setBounds(10, 145, 124, 20);
		contentPane.add(txtSkinValue);
		
		JLabel lblSkinSignature = new JLabel("Skin Signature");
		lblSkinSignature.setForeground(Color.WHITE);
		lblSkinSignature.setFont(FONT);
		lblSkinSignature.setBounds(10, 176, 100, 14);
		contentPane.add(lblSkinSignature);

		JTextField txtSkinSignature = new JTextField();
		txtSkinSignature.setEditable(false);
		txtSkinSignature.setForeground(Color.WHITE);
		txtSkinSignature.setBackground(Color.GRAY);
		txtSkinSignature.setFont(FONT);
		txtSkinSignature.setColumns(10);
		txtSkinSignature.setBounds(10, 201, 124, 20);
		contentPane.add(txtSkinSignature);

		btnOk.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent event)
			{
				if (System.currentTimeMillis() - _lastFetch < FETCH_WAIT_MILLISECONDS)
				{
					JOptionPane.showMessageDialog(SkinHelperUI.this, "You must wait a minimum of " + FETCH_WAIT_TIME + " seconds between each skin fetch to prevent you from being blocked from using the Mojang API.");
					return;
				}

				txtSkinValue.setText("Fetching...");
				txtSkinSignature.setText(txtSkinValue.getText());

				try
				{
					String input = txtMinecraftName.getText();
					String uuid = UUID_CACHE.get(input);

					if (uuid == null)
					{
						uuid = UUIDFetcher.getPlayerUUIDNoDashes(input);
						UUID_CACHE.put(input, uuid);
					}

					String[] skinData = SkinFetcher.getSkinData(uuid);

					txtSkinValue.setText(skinData[0]);
					txtSkinSignature.setText(skinData[1]);

					_lastFetch = System.currentTimeMillis();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(SkinHelperUI.this, "Please check the Minecraft Name you have entered. If it is correct please wait a minute and try again.");
				}
			}
		});
	}
}
