package com.shadowninja108.main;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.shadowninja108.info.ConsoleInfo;
import com.shadowninja108.info.ConsoleInfo.region;
import com.shadowninja108.info.ConsoleInfo.type;
import com.shadowninja108.interpret.Interpreter;
import com.shadowninja108.util.DownloadHandle;

public class Frame extends JFrame {

	public final static int binary_version = 1;

	/**
	 * 
	 */
	private static final long serialVersionUID = -5304071061458215521L;
	private JPanel contentPane;
	public ConsoleInfo consoleInfo;
	private final Action action = new StartAction();
	public JButton btnStart;
	private JSpinner verMajor;
	private JSpinner verMinor;
	private JSpinner verPatch;
	private JSpinner verIDK;
	private JComboBox<String> comboType;
	public static JLabel lblStatus;
	public static JProgressBar progressBar;
	private JComboBox<String> comboRegion;
	private JPanel statusPanel;
	private Interpreter interpreter;
	private Frame frame;
	public static boolean sevenZipEnabled;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		/*
		 * sevenZipEnabled = true; try { SevenZip.initSevenZipFromPlatformJAR();
		 * System.out.println("7zip library initialized!"); } catch
		 * (SevenZipNativeInitializationException e1) { System.out.
		 * println("7zip library failed to initialize! 7zip compatibility disabled!"
		 * );
		 */sevenZipEnabled = false;
		// }
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Frame() {
		Path xml = Paths.get(System.getProperty("user.dir") + "/download.xml");
		try {
			if (Files.exists(xml)) {
				Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "/update.xml"));
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(xml.toFile());
				Element root = doc.getRootElement();
				String url = root.getChild("general").getChild("updatesite").getValue();
				int ver = Integer.parseInt(root.getChild("general").getChildText("version"));
				if (url != "" || url != null) {
					if (!url.equals("") || !url.equals(null)) {
						System.out.println("Downloading update xml...");
						DownloadHandle handle = new DownloadHandle(new URL(url),
								System.getProperty("user.dir") + "/update.xml", null);
						handle.run();
						while (handle.getStatus() != DownloadHandle.COMPLETE)
							Thread.sleep(10);
						SAXBuilder updateBuilder = new SAXBuilder();
						Document updateDoc = updateBuilder
								.build(new File(System.getProperty("user.dir") + "/update.xml"));
						Element updateRoot = updateDoc.getRootElement();
						int newVer = Integer.parseInt(updateRoot.getChild("general").getChildText("version"));
						if (newVer > ver) {
							int ans = JOptionPane.showConfirmDialog(null,
									"Updated xml detected! Would you like to update?", "Hey!",
									JOptionPane.YES_NO_OPTION);
							if (ans == JOptionPane.YES_OPTION) {
								System.out.println("Attempting to update...");
								String uURL = updateRoot.getChild("general").getChildText("url");
								URL nUrl = new URL(uURL);
								Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "/download.xml"));
								System.out.println("Downloading: " + nUrl.getFile());
								DownloadHandle nhandle = new DownloadHandle(nUrl,
										System.getProperty("user.dir") + "/download.xml", null);
								nhandle.run();
								while (nhandle.getStatus() != DownloadHandle.COMPLETE)
									Thread.sleep(10);
								Files.delete(Paths.get(System.getProperty("user.dir") + "/update.xml"));
							}
						} else
							System.out.println("Up to date.");
					} else
						System.out.println("Update site empty.");
				} else
					System.out.println("Update site empty.");
			} else
				System.out.println("No download.xml detected.");
		} catch (

		Exception e) {
			System.out.println("Failed to get update xml! Reason: " + e.getMessage());
		} finally {
			try {
				Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "/update.xml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JPanel mainPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, mainPanel, 67, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, mainPanel, 58, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, mainPanel, -106, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, mainPanel, 376, SpringLayout.WEST, contentPane);
		contentPane.add(mainPanel);
		mainPanel.setLayout(null);

		JPanel typePanel = new JPanel();
		typePanel.setBounds(0, 50, 173, 38);
		mainPanel.add(typePanel);
		typePanel.setLayout(null);

		comboType = new JComboBox<String>();
		comboType.setBounds(0, 18, 173, 20);
		typePanel.add(comboType);
		comboType.setModel(new DefaultComboBoxModel<String>(new String[] { "New 3DS XL", "New 3DS", "3DS XL", "3DS" }));

		JLabel lblType = new JLabel("Type");
		lblType.setBounds(0, 0, 46, 14);
		typePanel.add(lblType);

		JPanel verPanel = new JPanel();
		verPanel.setBounds(0, 0, 198, 39);
		mainPanel.add(verPanel);
		verPanel.setLayout(null);

		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(0, 0, 46, 14);
		verPanel.add(lblVersion);

		verMajor = new JSpinner();
		verMajor.setBounds(0, 19, 37, 20);
		verPanel.add(verMajor);
		verMajor.setFont(new Font("Tahoma", Font.BOLD, 11));
		verMajor.setModel(new SpinnerNumberModel(new Integer(11), null, null, new Integer(1)));

		JLabel label = new JLabel(".");
		label.setBounds(40, 22, 10, 14);
		verPanel.add(label);

		verMinor = new JSpinner();
		verMinor.setBounds(50, 19, 37, 20);
		verPanel.add(verMinor);
		verMinor.setFont(new Font("Tahoma", Font.BOLD, 11));
		verMinor.setModel(new SpinnerNumberModel(new Integer(2), null, null, new Integer(1)));

		JLabel label_1 = new JLabel(".");
		label_1.setBounds(94, 22, 10, 14);
		verPanel.add(label_1);

		verPatch = new JSpinner();
		verPatch.setBounds(104, 19, 40, 20);
		verPanel.add(verPatch);
		verPatch.setFont(new Font("Tahoma", Font.BOLD, 11));

		JLabel label_2 = new JLabel("-");
		label_2.setBounds(150, 22, 10, 14);
		verPanel.add(label_2);

		verIDK = new JSpinner();
		verIDK.setBounds(161, 19, 37, 20);
		verPanel.add(verIDK);
		verIDK.setFont(new Font("Tahoma", Font.BOLD, 11));
		verIDK.setModel(new SpinnerNumberModel(new Integer(35), null, null, new Integer(1)));

		JPanel regionPanel = new JPanel();
		regionPanel.setBounds(250, -3, 68, 42);
		mainPanel.add(regionPanel);
		regionPanel.setLayout(null);

		comboRegion = new JComboBox<String>();
		comboRegion.setBounds(0, 22, 68, 20);
		regionPanel.add(comboRegion);
		comboRegion.setModel(new DefaultComboBoxModel<String>(new String[] { "USA", "EUR", "JPN" }));

		JLabel lblRegion = new JLabel("Region");
		lblRegion.setBounds(0, 0, 46, 14);
		regionPanel.add(lblRegion);

		btnStart = new JButton("Start");
		btnStart.setBounds(229, 65, 89, 23);
		mainPanel.add(btnStart);
		btnStart.setAction(action);

		statusPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, statusPanel, 199, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, statusPanel, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, statusPanel, -1, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, statusPanel, 429, SpringLayout.WEST, contentPane);
		contentPane.add(statusPanel);
		statusPanel.setLayout(null);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(0, 25, 424, 36);
		progressBar.setForeground(new Color(6, 176, 37));
		statusPanel.add(progressBar);

		lblStatus = new JLabel("Nothing is happening...");
		lblStatus.setBounds(0, 0, 424, 14);
		statusPanel.add(lblStatus);
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		frame = this;
	}

	public synchronized ConsoleInfo getConsoleInfo() {
		return consoleInfo;
	}

	@Override
	public void dispose() {
		interpreter.dispose();
	}

	private class StartAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6524467947901829279L;

		public StartAction() {
			putValue(NAME, "Start");
			putValue(SHORT_DESCRIPTION, "Download files");
		}

		public void actionPerformed(ActionEvent e) {
			btnStart.setEnabled(false);
			if (consoleInfo == null) {
				consoleInfo = new ConsoleInfo();
				getConsoleInfo().ver.major = (int) verMajor.getValue();
				getConsoleInfo().ver.minor = (int) verMinor.getValue();
				getConsoleInfo().ver.patch = (int) verPatch.getValue();
				getConsoleInfo().ver.idk = (int) verIDK.getValue();

				switch ((String) comboRegion.getSelectedItem()) {
				case "USA":
					consoleInfo.region = region.USA;
					break;
				case "EUR":
					consoleInfo.region = region.EUR;
					break;
				case "JPN":
					consoleInfo.region = region.JPN;
					break;
				default:
					System.out.println("Region of 3DS undetermined! What the hell did you click?");
					break;
				}

				if (((String) comboType.getSelectedItem()).startsWith("New"))
					consoleInfo.type = type.NEW;
				else if (((String) comboType.getSelectedItem()).startsWith("3DS"))
					consoleInfo.type = type.OLD;
				else
					System.out.println("Type of 3DS undetermined! What the hell did you click?");
			}
			if (interpreter == null)
				interpreter = new Interpreter(new File(System.getProperty("user.dir") + "/download.xml"), consoleInfo,
						frame);
			progressBar.setStringPainted(true);
			interpreter.interpret();
		}
	}

	public static void setStatus(String status) {
		lblStatus.setText(status);
		lblStatus.paintImmediately(lblStatus.getVisibleRect());
	}

	public static void setProgress(int progress) {
		progressBar.setValue(progress);
		progressBar.repaint();
	}

	public static JProgressBar getProgressBar() {
		return progressBar;
	}
}
