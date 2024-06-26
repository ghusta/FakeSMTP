package com.nilhcem.fakesmtp.gui.listeners;

import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.gui.MainFrame;
import com.nilhcem.fakesmtp.gui.TrayPopup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Responsible for window minimizing and closing. If SystemTray is supported,
 * the window MainFrame is minimized to an icon.
 *
 * @author Vest
 * @since 2.1
 */
public class MainWindowListener extends WindowAdapter {

	private static final Logger log = LoggerFactory.getLogger(MainWindowListener.class);

	private TrayIcon trayIcon;
	private final boolean useTray;

	/**
	 * @param mainFrame The MainFrame class used for closing actions from TrayPopup.
	 */
	public MainWindowListener(final MainFrame mainFrame) {
		useTray = (SystemTray.isSupported() && Boolean.parseBoolean(Configuration.getInstance().get("application.tray.use")));

		if (useTray) {
			final TrayPopup trayPopup = new TrayPopup(mainFrame);

			final Image iconImage = Toolkit.getDefaultToolkit().getImage(getClass().
				getResource(Configuration.getInstance().get("application.icon.path")));

			trayIcon = new TrayIcon(iconImage);

			trayIcon.setImageAutoSize(true);
			trayIcon.setPopupMenu(trayPopup.get());
		}
	}

	@Override
	public void windowStateChanged(WindowEvent e) {
		super.windowStateChanged(e);
		if (!useTray) {
			return;
		}

		final SystemTray tray = SystemTray.getSystemTray();
		final JFrame frame = (JFrame) e.getSource();

		if ((e.getNewState() & Frame.ICONIFIED) != 0) {
			try {
				/* Displays the window when the icon is clicked twice */
				trayIcon.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ae) {
						int state = frame.getExtendedState();
						state &= ~Frame.ICONIFIED;

						frame.setExtendedState(state);
						frame.setVisible(true);

						tray.remove(trayIcon);

						trayIcon.removeActionListener(this);
					}
				});

				tray.add(trayIcon);
				frame.dispose();
			} catch (AWTException ex) {
				log.error("Couldn't create a tray icon, the minimizing is not possible", ex);
			}
		} else {
			frame.setVisible(true);
		}
	}
}
