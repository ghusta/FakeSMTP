package com.nilhcem.fakesmtp.gui.info;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.I18n;
import com.nilhcem.fakesmtp.core.exception.*;
import com.nilhcem.fakesmtp.model.UIModel;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;

/**
 * Button to start the SMTP server.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class StartServerButton extends Observable implements Observer {
	private final I18n i18n = I18n.INSTANCE;

	private final JButton button = new JButton(i18n.get("startsrv.start"));

	/**
	 * Creates a start button to start the SMTP server.
	 * <p>
	 * If the user selects a wrong port before starting the server, the method will display an error message.
	 * </p>
	 */
	public StartServerButton() {
		button.addActionListener(e -> toggleButton());
	}

	/**
	 * Switches the text inside the button and calls the PortTextField observer to enable/disable the port field.
	 *
	 * @see PortTextField
	 */
	public void toggleButton() {
		try {
			startServerIfNeeded(UIModel.INSTANCE);
		} catch (InvalidHostException ihe) {
			displayError(i18n.get("startsrv.err.invalidHost").formatted(ihe.getHost()));
		} catch (InvalidPortException ipe) {
			displayError(i18n.get("startsrv.err.invalidPort").formatted());
		} catch (BindPortException bpe) {
			displayError(i18n.get("startsrv.err.bound").formatted(bpe.getPort()));
		} catch (OutOfRangePortException orpe) {
			displayError(i18n.get("startsrv.err.range").formatted(orpe.getPort()));
		} catch (RuntimeException re) {
			displayError(i18n.get("startsrv.err.default").formatted(re.getMessage()));
		}

		if (UIModel.INSTANCE.isServerStarted()) {
			button.setText(i18n.get("startsrv.started"));
			button.setEnabled(false);
		}
		setChanged();
		notifyObservers();
	}

	/**
	 * @throws InvalidPortException    when the port is invalid.
	 * @throws BindPortException       when the port cannot be bound.
	 * @throws OutOfRangePortException when the port is out of range.
	 * @throws InvalidHostException    when the address cannot be resolved.
	 */
	private void startServerIfNeeded(UIModel uiModel) throws OutOfRangePortException, BindPortException, InvalidPortException, InvalidHostException {
		if (uiModel.isServerStarted()) {
			// Do nothing. We can't stop the server. User has to quit the app (issue with SubethaSMTP)
		} else {
			try {
				int port = Integer.parseInt(uiModel.getPort());
				InetAddress host = null;
				if (uiModel.getHost() != null && !uiModel.getHost().isEmpty()) {
					host = InetAddress.getByName(uiModel.getHost());
				}

				SMTPServerHandler.INSTANCE.startServer(port, host);
			} catch (NumberFormatException e) {
				throw new InvalidPortException(e);
			} catch (UnknownHostException e) {
				throw new InvalidHostException(e, uiModel.getHost());
			}
		}
		uiModel.setServerStarted(!uiModel.isServerStarted());
	}

	/**
	 * Returns the JButton object.
	 *
	 * @return the JButton object.
	 */
	public JButton get() {
		return button;
	}

	/**
	 * Displays a message dialog displaying the error specified in parameter.
	 *
	 * @param error a string representing the error which will be displayed in a message dialog.
	 */
	private void displayError(String error) {
		JOptionPane.showMessageDialog(button.getParent(), error,
				i18n.get("startsrv.err.title").formatted(Configuration.getInstance().get("application.name")),
			JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof PortTextField) {
			toggleButton();
		}
	}
}
