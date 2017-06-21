package robotcontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Directive;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.IntentRequest.DialogState;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.dialog.directives.DelegateDirective;
import com.amazon.speech.speechlet.dialog.directives.DialogIntent;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

/**
 * This sample shows how to control a robot through speech with Amazon Alexa.
 */
public class RobotControllerSpeechlet implements Speechlet {
	private static final Logger log = LoggerFactory
			.getLogger(RobotControllerSpeechlet.class);

	// Konstanten
	private static final String ROBOTNAME_SLOT = "RobotName";
	private static final String DIRECTION_SLOT = "Direction";
	private static final String PERCENT_SLOT = "Percent";
	private static final String MOVETO_SLOT = "Moveto";
	private static final String IPADRESSE_NAMESERVER = "31.19.157.151";
	private static final int PORT_NAMESERVER_LOOKUP = 9091;
	private static final int PORT_NAMESERVER = 9090;

	/**
	 * Komponente für die Kommunikation mit den Nameserver für den Lookup.
	 */
	private Kommunikation senderLookup;

	/**
	 * Komponente für die Kommunikation mit den Nameserver für die verschiedenen
	 * Dienste des Roboters.
	 */
	private Kommunikation sender;

	@Override
	public void onSessionStarted(final SessionStartedRequest request,
			final Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}",
				request.getRequestId(), session.getSessionId());
		senderLookup = new Kommunikation(IPADRESSE_NAMESERVER,
				PORT_NAMESERVER_LOOKUP);
		sender = new Kommunikation(IPADRESSE_NAMESERVER, PORT_NAMESERVER);
		// any initialization logic goes here
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request,
			final Session session) throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		return getWelcomeResponse();
	}

	@Override
	public SpeechletResponse onIntent(final IntentRequest request,
			final Session session) throws SpeechletException {
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		log.info(request.getIntent().getName());
		SpeechletResponse response = null;

		// The intentRequest variable here is the IntentRequest object sent to
		// the skill.
		if (request.getDialogState() == DialogState.STARTED) {
			DialogIntent dialogIntent = new DialogIntent(request.getIntent());
			response = new SpeechletResponse();
			DelegateDirective delegateDirective = new DelegateDirective();
			delegateDirective.setUpdatedIntent(dialogIntent);
			List<Directive> liste = new ArrayList<>();
			liste.add(delegateDirective);
			response.setDirectives(liste);
			response.setShouldEndSession(false);
			return response;
		} else if (request.getDialogState() != DialogState.COMPLETED) {
			response = new SpeechletResponse();
			DelegateDirective delegateDirective = new DelegateDirective();
			List<Directive> liste = new ArrayList<>();
			liste.add(delegateDirective);
			response.setDirectives(liste);
			response.setShouldEndSession(false);
			return response;
		} else {
			// Dialog is now complete and all required slots should be filled,
			// so call your normal intent handler.
			// Read the Intent
			Intent intent = request.getIntent();
			response = handleIntent(intent);
		}

		return response;
	}

	@Override
	public void onSessionEnded(final SessionEndedRequest request,
			final Session session) throws SpeechletException {
		senderLookup.teardown();
		sender.teardown();
		log.info("onSessionEnded requestId={}, sessionId={}",
				request.getRequestId(), session.getSessionId());
		// any cleanup logic goes here
	}

	private SpeechletResponse handleIntent(Intent intent)
			throws SpeechletException {

		String intentName = (intent != null) ? intent.getName() : null;

		switch (intentName) {
		case "DoCloseGripperIntent":
			return getDoCloseGripperResponse(intent);
		case "DoMoveArmIntent":
			return getDoMoveArmResponse(intent);
		case "DoOpenGripeperIntent":
			return getDoOpenGripperResponse(intent);
		case "DoMoveArmWithPercentIntent":
			return getDoMoveArmWithPercentResponse(intent);
		case "GetRobotNamesIntent":
			return getRobotNamesResponse();
		case "AMAZON.HelpIntent":
			return getHelpResponse();
		case "AMAZON.StopIntent":
			return getStopResponse();
		case "AMAZON.CancelIntent":
			return getCancelResponse();
		default:
			throw new SpeechletException("Invalid Intent");

		}
	}

	/**
	 * Creates and returns a {@code SpeechletResponse} with a welcome message.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getWelcomeResponse() {
		String speechText = "Willkommen zur Roboter Steuerung, "
				+ "du kannst dem Roboter jetzt Befehle erteilen.";
		String repromptText = "Zum Beispiel öffne den Greifer von ..."
				+ " oder bewege den Arm von ... nach oben.";
		return newAskResponse(speechText, false, repromptText, false);
	}

	/**
	 * Creates a {@code SpeechletResponse}
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getDoOpenGripperResponse(Intent intent) {
		String speechText;

		// Get the slots from the intent.
		Map<String, Slot> slots = intent.getSlots();
		Slot robotnameSlot = slots.get(ROBOTNAME_SLOT);

		if (robotnameSlot != null) {
			String robotName = robotnameSlot.getValue();

			boolean success = sender.openGripper(robotName);
			if (success) {
				speechText = "Der Greifer von " + robotName
						+ " wird jetzt geöffnet.";
			} else {
				speechText = "Der Greifer von " + robotName
						+ " konnte nicht geöffnet werden.";
			}
		} else {
			speechText = "falscher oder kein Robotername";
		}
		String repromptText = "Hast du noch weitere Befehle?";

		return newAskResponse(speechText, false, repromptText, false);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the close Gripper intent
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getDoCloseGripperResponse(Intent intent) {
		String speechText;

		// Get the slots from the intent.
		Map<String, Slot> slots = intent.getSlots();
		Slot robotnameSlot = slots.get(ROBOTNAME_SLOT);

		if (robotnameSlot != null) {
			String robotName = robotnameSlot.getValue();
			boolean success = sender.closeGripper(robotName);
			if (success) {
				speechText = "Der Greifer von " + robotName
						+ " wird jetzt geschlossen.";
			} else {
				speechText = "Der Greifer von " + robotName
						+ " konnte nicht geschlossen werden.";
			}

		} else {
			speechText = "falscher oder kein Robotername";
		}

		String repromptText = "Hast du noch weitere Befehle?";

		return newAskResponse(speechText, false, repromptText, false);
	}

	/**
	 * Creates a {@code SpeechletResponse}
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getDoMoveArmResponse(Intent intent) {
		String speechText;

		// Get the slots from the intent.
		Map<String, Slot> slots = intent.getSlots();
		Slot robotnameSlot = slots.get(ROBOTNAME_SLOT);
		Slot movetoSlot = slots.get(MOVETO_SLOT);

		if (robotnameSlot != null && movetoSlot != null) {
			String robotName = robotnameSlot.getValue();
			String moveto = movetoSlot.getValue();

			speechText = "Der Arm von " + robotName + " wurde nach " + moveto
					+ " bewegt.";
			boolean success = false;
			switch (moveto) {
			case "oben":
				success = sender.moveVertical(robotName, 100);
				break;
			case "unten":
				success = sender.moveVertical(robotName, 1);
				break;
			case "rechts":
				success = sender.moveHorizontal(robotName, 1);
				break;
			case "links":
				success = sender.moveHorizontal(robotName, 100);
				break;

			default:
				break;
			}

			if (success == false) {
				speechText = "Der Arm von " + robotName + " konnte nicht "
						+ moveto + " bewegt werden.";
			}
		} else {
			speechText = "Unbekannter RoboterName oder keine Richtung";
		}

		String repromptText = "Hast du noch weitere Befehle?";

		return newAskResponse(speechText, false, repromptText, false);
	}

	/**
	 * Creates a {@code SpeechletResponse}
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getDoMoveArmWithPercentResponse(Intent intent) {
		String speechText;

		// Get the slots from the intent.
		Map<String, Slot> slots = intent.getSlots();
		Slot robotnameSlot = slots.get(ROBOTNAME_SLOT);
		Slot directionSlot = slots.get(DIRECTION_SLOT);
		Slot percentSlot = slots.get(PERCENT_SLOT);

		if (robotnameSlot != null && directionSlot != null
				&& percentSlot != null) {
			String robotName = robotnameSlot.getValue();
			String direction = directionSlot.getValue();
			String percents = percentSlot.getValue();
			int percent = Integer.parseInt(percents);
			speechText = "Der Arm von " + robotName + " wird jetzt" + direction
					+ " auf " + percent + " bewegt";
			boolean success = false;
			if (direction.equalsIgnoreCase("vertikal")) {
				sender.moveVertical(robotName, percent);
			} else {
				if (direction.equalsIgnoreCase("horizontal")) {
					sender.moveHorizontal(robotName, percent);
				}
			}
		} else {
			speechText = "Unbekannter Roboter oder unbekannte Bewegungsrichtung";
		}

		String repromptText = "Hast du noch weitere Befehle?";

		return newAskResponse(speechText, false, repromptText, false);
	}

	/**
	 * Creates a {@code SpeechletResponse} for Request of the Robot names.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getRobotNamesResponse() {

		String roboterNamen = senderLookup.getRobotNames();
		String speechText = "Die Namen der Roboter sind " + roboterNamen + " .";

		if (roboterNamen == null || roboterNamen.equals("")) {
			speechText = "Es stehen keine Roboter zu Verfügung.";
		}

		String repromptText = "Was soll der Roboter machen?";

		return newAskResponse(speechText, false, repromptText, false);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the help intent.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getHelpResponse() {
		String speechText = "Du kannst mir Befehle zur Steuerung des Roboters geben!";
		String repromptText = "Zum Beispiel kannst du sagen: Greifer aufmachen oder"
				+ " abfragen welche Roboter es gibt.";
		return newAskResponse(speechText, repromptText);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the stop intent.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getStopResponse() {
		String speechText = "Die Roboter Steuerung wird beendet. Auf Wiedersehen!";

		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle("Ende");
		card.setContent(speechText);

		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		return SpeechletResponse.newTellResponse(speech, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the cancel intent.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getCancelResponse() {
		String speechText = "Roboter Steuerung wird beendet. Auf Wiedersehen!";

		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle("Ende");
		card.setContent(speechText);

		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		return SpeechletResponse.newTellResponse(speech, card);
	}

	/**
	 * Wrapper for creating the Ask response from the input strings with plain
	 * text output and reprompt speeches.
	 *
	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput,
			String repromptText) {
		return newAskResponse(stringOutput, false, repromptText, false);
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 *
	 * @param stringOutput
	 *            the output to be spoken
	 * @param isOutputSsml
	 *            whether the output text is of type SSML
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @param isRepromptSsml
	 *            whether the reprompt text is of type SSML
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput,
			boolean isOutputSsml, String repromptText, boolean isRepromptSsml) {
		OutputSpeech outputSpeech, repromptOutputSpeech;
		if (isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
		}

		if (isRepromptSsml) {
			repromptOutputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
		} else {
			repromptOutputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) repromptOutputSpeech)
					.setText(repromptText);
		}

		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

}
