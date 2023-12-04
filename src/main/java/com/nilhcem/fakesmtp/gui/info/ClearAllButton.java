package com.nilhcem.fakesmtp.gui.info;

import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.I18n;
import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;

import javax.swing.*;
import java.util.Observable;

/**
 * Button to clear all the information from the main panel.
 * <p>
 * The button will ask the user if he wants to delete the received emails or not.<br>
 * If yes, emails will be deleted from file system.
 * </p>
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class ClearAllButton extends Observable {

	private final I18n i18n = I18n.INSTANCE;
	private final JButton button = new JButton(i18n.get("clearall.button"));

	/**
	 * Creates the "clear all" button"
	 * <p>
	 * The button will be disabled by default, since no email is received when the application starts.<br>
	 * The button will display a confirmation dialog to know if it needs to delete the received emails or not.<br>
	 * If yes, emails will be deleted from the file system.
	 * </p>
	 */
	public ClearAllButton() {
		button.setToolTipText(i18n.get("clearall.tooltip"));
		button.addActionListener(e -> {
			int answer = JOptionPane.showConfirmDialog(button.getParent(), i18n.get("clearall.delete.email"),
					i18n.get("clearall.title").formatted(Configuration.INSTANCE.get("application.name")),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer == JOptionPane.CLOSED_OPTION) {
				return;
			}

			synchronized (SMTPServerHandler.INSTANCE.getMailSaver().getLock()) {
				// Note: Should delete emails before calling observers, since observers will clean the model.
				if (answer == JOptionPane.YES_OPTION) {
					SMTPServerHandler.INSTANCE.getMailSaver().deleteEmails();
				}
				setChanged();
				notifyObservers();
				button.setEnabled(false);
			}
		});
		button.setEnabled(false);
	}

	/**
	 * Returns the JButton object.
	 *
	 * @return the JButton object.
	 */
	public JButton get() {
		return button;
	}

	public void onNewMail(EmailModel email) {
		if (!button.isEnabled()) {
			button.setEnabled(true);
		}
	}

}
