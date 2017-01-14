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

	public final static File working_directory = Paths.get(".").toAbsolutePath().toFile();
	public final static int binary_version = 2;

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
		System.out.println("Working dir: " + working_directory.toString());
		JLabel lblXMLAuthor = new JLabel("No XML loaded.");
		Path xml = new File(working_directory, "download.xml").toPath();
		try {
			if (Files.exists(xml)) {
				File downloadXml = new File(working_directory, "download.xml");
				File updateXml = new File(working_directory, "update.xml");
				if (updateXml.exists())
					updateXml.delete();
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(xml.toFile());
				Element root = doc.getRootElement();
				String url = root.getChild("general").getChild("updatesite").getValue();
				lblXMLAuthor.setText("XML Author: " + root.getChild("general").getChildText("author"));
				int ver = Integer.parseInt(root.getChild("general").getChildText("version"));
				int bver = Integer.parseInt(root.getChild("general").getChildText("binaryversion"));
				if (binary_version != bver)
					error("This XML was made for another build!\nAlthough it might be compatible, update\nif you have any troubles before you complain!\nCurrent version: "
							+ binary_version + "\nXML build version: " + bver);
				if (url != "" || url != null) {
					if (!url.equals("") || !url.equals(null)) {
						System.out.println("Downloading update xml...");
						DownloadHandle handle = new DownloadHandle(new URL(url), updateXml, null);
						handle.run();
						while (!handle.isComplete())
							Thread.sleep(10);
						SAXBuilder updateBuilder = new SAXBuilder();
						Document updateDoc = updateBuilder.build(updateXml);
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
								downloadXml.delete();
								System.out.println("Downloading: " + nUrl.getFile());
								DownloadHandle nhandle = new DownloadHandle(nUrl, downloadXml, null);
								nhandle.run();
								while (!nhandle.isComplete())
									Thread.sleep(10);
								updateXml.delete();
								System.out.println("Update complete!");
							}
						} else
							System.out.println("Up to date.");
					} else
						System.out.println("Update site empty.");
				} else
					System.out.println("Update site empty.");
			} else
				error("No download.xml detected!");
		} catch (Exception e) {
			error("Failed to check for updates! Reason: " + e.getMessage());
		} finally {
			try {
				Files.deleteIfExists(new File(working_directory, "update.xml").toPath());
			} catch (IOException e) {
				error("Failed to delete update.xml!");
			}
		}

		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		statusPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, statusPanel, 20, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, statusPanel, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, statusPanel, 410, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, statusPanel, 0, SpringLayout.EAST, lblXMLAuthor);
		contentPane.add(statusPanel);
		statusPanel.setLayout(null);

		JPanel mainPanel = new JPanel();
		mainPanel.setBounds(78, 66, 384, 184);
		statusPanel.add(mainPanel, "push, align center");
		sl_contentPane.putConstraint(SpringLayout.NORTH, mainPanel, 67, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, mainPanel, 58, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, mainPanel, -106, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, mainPanel, 376, SpringLayout.WEST, contentPane);
		mainPanel.setLayout(null);

		JPanel verPanel = new JPanel();
		verPanel.setBounds(0, 0, 198, 75);
		mainPanel.add(verPanel);
		verPanel.setLayout(null);

		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(0, 0, 198, 14);
		verPanel.add(lblVersion);

		verMajor = new JSpinner();
		verMajor.setBounds(0, 25, 37, 50);
		verPanel.add(verMajor);
		verMajor.setFont(new Font("Tahoma", Font.BOLD, 11));
		verMajor.setModel(new SpinnerNumberModel(new Integer(11), null, null, new Integer(1)));

		JLabel label = new JLabel(".");
		label.setBounds(40, 61, 10, 14);
		verPanel.add(label);

		verMinor = new JSpinner();
		verMinor.setBounds(47, 25, 37, 50);
		verPanel.add(verMinor);
		verMinor.setFont(new Font("Tahoma", Font.BOLD, 11));
		verMinor.setModel(new SpinnerNumberModel(new Integer(2), null, null, new Integer(1)));

		JLabel label_1 = new JLabel(".");
		label_1.setBounds(91, 61, 10, 14);
		verPanel.add(label_1);

		verPatch = new JSpinner();
		verPatch.setBounds(94, 25, 40, 50);
		verPanel.add(verPatch);
		verPatch.setFont(new Font("Tahoma", Font.BOLD, 11));

		JLabel label_2 = new JLabel("-");
		label_2.setBounds(144, 43, 10, 14);
		verPanel.add(label_2);

		verIDK = new JSpinner();
		verIDK.setBounds(161, 25, 37, 50);
		verPanel.add(verIDK);
		verIDK.setFont(new Font("Tahoma", Font.BOLD, 11));
		verIDK.setModel(new SpinnerNumberModel(new Integer(35), null, null, new Integer(1)));

		JPanel typePanel = new JPanel();
		typePanel.setBounds(0, 99, 198, 85);
		mainPanel.add(typePanel);
		typePanel.setLayout(null);

		comboType = new JComboBox<String>();
		comboType.setBounds(0, 18, 198, 67);
		typePanel.add(comboType);
		comboType.setModel(new DefaultComboBoxModel<String>(new String[] { "New 3DS XL", "New 3DS", "3DS XL", "3DS" }));

		JLabel lblType = new JLabel("Type");
		lblType.setBounds(0, 0, 135, 14);
		typePanel.add(lblType);

		JPanel regionPanel = new JPanel();
		regionPanel.setBounds(239, 0, 145, 75);
		mainPanel.add(regionPanel);
		regionPanel.setLayout(null);

		comboRegion = new JComboBox<String>();
		comboRegion.setBounds(0, 22, 145, 53);
		regionPanel.add(comboRegion);
		comboRegion.setModel(new DefaultComboBoxModel<String>(new String[] { "USA", "EUR", "JPN", "TWN", "KOR" }));

		JLabel lblRegion = new JLabel("Region");
		lblRegion.setLabelFor(comboType);
		lblRegion.setBounds(0, 0, 68, 14);
		regionPanel.add(lblRegion);

		btnStart = new JButton("Start");
		btnStart.setBounds(239, 117, 145, 67);
		mainPanel.add(btnStart);
		btnStart.setAction(action);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(0, 343, 579, 36);
		progressBar.setForeground(new Color(6, 176, 37));
		statusPanel.add(progressBar);

		lblStatus = new JLabel("Idle...");
		lblStatus.setBounds(0, 318, 579, 14);
		statusPanel.add(lblStatus);
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel lblToolAuthor = new JLabel("Tool Author: shadowninja108");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblToolAuthor, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblToolAuthor, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblToolAuthor, 199, SpringLayout.WEST, contentPane);
		lblToolAuthor.setEnabled(false);
		lblToolAuthor.setHorizontalAlignment(SwingConstants.LEFT);
		contentPane.add(lblToolAuthor);
		lblXMLAuthor.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblXMLAuthor, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblXMLAuthor, 0, SpringLayout.EAST, contentPane);
		lblXMLAuthor.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPane.add(lblXMLAuthor);
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
				case "KOR":
					consoleInfo.region = region.KOR;
					break;
				case "TWN":
					consoleInfo.region = region.TWN;
					break;
				default:
					error("Region of 3DS undetermined! What the hell did you click?");
					break;
				}

				if (((String) comboType.getSelectedItem()).startsWith("New"))
					consoleInfo.type = type.NEW;
				else if (((String) comboType.getSelectedItem()).startsWith("3DS"))
					consoleInfo.type = type.OLD;
				else
					error("Type of 3DS undetermined! What the hell did you click?");
			}
			if (interpreter == null)
				interpreter = new Interpreter(new File(working_directory, "download.xml"), consoleInfo, frame);
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

	public static void error(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
}
