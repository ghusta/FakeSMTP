package com.nilhcem.fakesmtp.gui.listeners;

import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

/**
 * Implements the About action.
 *
 * @author Vest
 * @since 2.1
 */
public class AboutActionListener implements ActionListener {

	private static final Logger log = LoggerFactory.getLogger(AboutActionListener.class);

	private final I18n i18n = I18n.INSTANCE;
	private final Container parent;

	/**
	 * @param parent The parent container that is used for the About dialog window.
	 */
	public AboutActionListener(Container parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// for copying style
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuilder style = new StringBuilder("font-family:").append(font.getFamily()).append(";font-weight:");
		if (font.isBold()) {
			style.append("bold");
		} else {
			style.append("normal");
		}
		style.append(";font-size:").append(font.getSize()).append("pt;");

		// html content
		String link = i18n.get("menubar.about.dialog.link");
		String appVersion = Configuration.getInstance().get("application.version");
		JEditorPane ep = new JEditorPane("text/html",
				"<html><body style=\"%s\">%s<br /><a href=\"%s\">%s</a></body></html>".formatted(
						style, i18n.get("menubar.about.dialog").formatted(appVersion),
						link, link));

		// handle link events
		ep.addHyperlinkListener(e1 -> {
			if (e1.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				AboutActionListener.launchUrl(e1.getURL().toString());
			}
		});
		ep.setEditable(false);
		ep.setBackground(label.getBackground());

		// show
		JOptionPane.showMessageDialog(parent, ep, i18n.get("menubar.about.title").formatted(
				Configuration.getInstance().get("application.name")), JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Opens a web browser to launch the URL specified in parameters.
	 *
	 * @param url the URL to launch.
	 */
	private static void launchUrl(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop desktop = Desktop.getDesktop();
				if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
					desktop.browse(new URI(url));
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
