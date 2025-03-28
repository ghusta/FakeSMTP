package com.nilhcem.fakesmtp.gui.tab;

import com.nilhcem.fakesmtp.core.ArgsHandler;
import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.I18n;
import com.nilhcem.fakesmtp.gui.info.ClearAllButton;
import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.model.UIModel;
import com.nilhcem.fakesmtp.server.MailSaver;
import jakarta.mail.internet.MimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Observable;
import java.util.Observer;

/**
 * Scrolled table where will be displayed every received email (one line for each email).
 * <p>
 * The user can double-click on any row to see the selected email.
 * </p>
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class MailsListPane implements Observer {

	private int nbElements = 0;
	private Desktop desktop = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(MailsListPane.class);
	private final I18n i18n = I18n.INSTANCE;
	private final JScrollPane mailsListPane = new JScrollPane();
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
	private final int[] widths = new int[] {85, 140, 140}; // widths of columns in tab

	/**
	 * Table with non-editable cells.
	 */
	private final JTable table = new JTable() {
		@Serial
		private static final long serialVersionUID = 6332956458868628779L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	/**
	 * Table model with non-editable cells.
	 */
	private final DefaultTableModel model = new DefaultTableModel() {
		@Serial
		private static final long serialVersionUID = -6716294637919469299L;

		@Override
		public boolean isCellEditable(int rowIndex, int mColIndex) {
			return false;
		}
	};

	/**
	 * Creates the table and sets its cells as non-editable.
	 * <p>
	 * Adds some mouse events on the table, to display emails, when a user clicks on
	 * a specific row.<br>
	 * If the email can't be found, an error message will be displayed.<br>
	 * The table will reset the size of its column every time the size of the table changed
	 * (for example when the user maximizes the window).
	 * </p>
	 */
	public MailsListPane() {
		// Init desktop (Java 6 Desktop API)
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		}

		if (!ArgsHandler.INSTANCE.isMemoryModeEnabled()) {
			table.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {

					String emlViewer = ArgsHandler.INSTANCE.getEmlViewer();

					if (e.getClickCount() == 2 && (emlViewer != null || desktop != null)) {
						File file = null;
						JTable target = (JTable) e.getSource();
						String fileName = UIModel.INSTANCE.getListMailsMap().get(target.getSelectedRow());
						if (fileName == null) {
							LOGGER.error("Can't find any associated email for row #{}", target.getSelectedRow());
						} else {
							file = new File(fileName);
						}

						if (file != null && file.exists()) {
							try {
								if (emlViewer != null) {
									Runtime.getRuntime().exec(emlViewer + " " + file.getAbsolutePath());
								} else {
									desktop.open(file);
								}
							} catch (IOException ioe) {
								LOGGER.error("", ioe);
								displayError(i18n.get("mailslist.err.open").formatted(file.getAbsolutePath()));
							}
						} else {
							displayError(i18n.get("mailslist.err.find").formatted(file.getAbsolutePath()));
						}
					}
				}
			});
        }

		// Auto scroll tab to bottom when a new element is inserted
		table.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				table.scrollRectToVisible(new Rectangle(table.getCellRect(nbElements, 0, true)));
			}
		});

		model.addColumn(i18n.get("mailslist.col.received"));
		model.addColumn(i18n.get("mailslist.col.from"));
		model.addColumn(i18n.get("mailslist.col.to"));
		model.addColumn(i18n.get("mailslist.col.subject"));
		table.setModel(model);

		mailsListPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// When the width of a column is changed, only the columns to the left and right of the margin change
				table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

				// Set width for each column
				int total = 0;
				int length = widths.length;
				for (int i = 0; i < length; i++) {
					table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
					total += widths[i];
				}
				table.getColumnModel().getColumn(length).setPreferredWidth(table.getWidth() - total);
			}
		});
		mailsListPane.getViewport().add(table, null);
	}

	/**
	 * Returns the JScrollPane object.
	 *
	 * @return the JScrollPane object.
	 */
	public JScrollPane get() {
		return mailsListPane;
	}

	/**
	 * Updates the content of the table.
	 * <p>
	 * This method will be called by an observable element.
     * </p>
	 * <ul>
	 *   <li>If the observable is a {@link MailSaver} object, a new row will be added
	 *   to the table, and the {@link UIModel} will be updated;</li>
	 *   <li>If the observable is a {@link ClearAllButton} object, all the cells
	 *   of the table will be removed, and the {@link UIModel} will be updated.</li>
	 * </ul>
	 *
	 * @param o the observable element which will notify this class.
	 * @param arg optional parameters (an {@code EmailModel} object, for the case of
	 * a {@code MailSaver} observable) containing all the information about the email.
	 */
	@Override
	public void update(Observable o, Object arg) {
			if (o instanceof ClearAllButton) {
			// Delete information from the map
			UIModel.INSTANCE.getListMailsMap().clear();

			// Remove elements from the list
			try {
				while (nbElements > 0) {
					model.removeRow(--nbElements);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				LOGGER.error("", e);
			}
		}
	}

	public void onNewMail(EmailModel email ) {
		String subject;
		try {
			subject = MimeUtility.decodeText(email.subject());
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
			subject = email.subject();
		}

		String recipients = String.join(";", email.recipients());
		model.addRow(new Object[]{dateTimeFormatter.format(email.receivedDate()),
				email.from(), recipients, subject});
		UIModel.INSTANCE.getListMailsMap().put(nbElements++, String.valueOf(email.filePath()));
	}

	/**
	 * Displays a message dialog containing the error specified in parameter.
	 *
	 * @param error a String representing an error message to display.
	 */
	private void displayError(String error) {
		JOptionPane.showMessageDialog(mailsListPane.getParent(), error,
				i18n.get("mailslist.err.title").formatted(Configuration.getInstance().get("application.name")),
			JOptionPane.ERROR_MESSAGE);
	}
}
