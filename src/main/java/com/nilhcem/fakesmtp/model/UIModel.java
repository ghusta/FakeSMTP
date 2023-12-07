package com.nilhcem.fakesmtp.model;

import com.nilhcem.fakesmtp.core.I18n;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UI presentation model of the application.
 * <p>
 * The essence of a Presentation Model is of a fully self-contained class that represents all the data
 * and behavior of the UI window, but without any of the controls used to render that UI on the screen.
 * </p>
 *
 * @author Nilhcem
 * @since 1.0
 * @see <a href="link">http://martinfowler.com/eaaDev/PresentationModel.html</a>
 */
public enum UIModel {
	INSTANCE;

	@Getter
	@Setter
	private boolean serverStarted = false; // server is not started by default
	@Getter
	@Setter
	private String port;
	@Getter
	@Setter
	private String host;
	@Getter
	@Setter
	private int nbMessageReceived = 0;
	@Getter
	@Setter
	private String savePath = I18n.INSTANCE.get("emails.default.dir");
	@Getter
	private final Map<Integer, String> listMailsMap = new HashMap<>();
	@Getter
	@Setter
	private List<String> relayDomains;

	UIModel() {
	}

}
