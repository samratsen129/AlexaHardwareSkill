package com.sdb.da.test;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

public class SessionSpeechlet implements Speechlet{
	private static final Logger log = LoggerFactory.getLogger(SessionSpeechlet.class);

    private static final String COLOR_KEY = "COLOR";
    private static final String COLOR_SLOT = "Color";
    private static final String PUBNUB_CHANNEL = "sbd_987654321";
    
    private PNConfiguration pnConfiguration = new PNConfiguration();
    
     
    private PubNub pubnub;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        pnConfiguration.setSubscribeKey("sub-cxxxxxxxxxxxxx");
        pnConfiguration.setPublishKey("pub-c-xxxxxxx");
        pubnub = new PubNub(pnConfiguration);
        
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        if ("MyColorIsIntent".equals(intentName)) {
            return setColorInSession(intent, session);
        } else if ("WhatsMyColorIntent".equals(intentName)) {
            return getColorFromSession(intent, session);
        } else if ("FlashMyColorIntent".equals(intentName)) {
            return splashColor(intent);
        }  else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText =
                "Welcome to Stan's Colors. Please tell me your favorite color by "
                        + "saying, my favorite color is red";
        String repromptText =
                "Please tell me your favorite color by saying, my favorite color is red";

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted color in the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse setColorInSession(final Intent intent, final Session session) {
        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot favoriteColorSlot = slots.get(COLOR_SLOT);
        String speechText, repromptText;

        // Check for favorite color and create output to user.
        if (favoriteColorSlot != null) {
            // Store the user's favorite color in the Session and create response.
            String favoriteColor = favoriteColorSlot.getValue();
            session.setAttribute(COLOR_KEY, favoriteColor);
            speechText =
                    String.format("I now know that your favorite color is %s. You can ask me your "
                            + "favorite color by saying, what's my favorite color?", favoriteColor);
            repromptText =
                    "You can ask me your favorite color by saying, what's my favorite color?";

        } else {
            // Render an error since we don't know what the users favorite color is.
            speechText = "I'm not sure what your favorite color is, please try again";
            repromptText =
                    "I'm not sure what your favorite color is. You can tell me your favorite "
                            + "color by saying, my favorite color is red";
        }

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's favorite color from the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
    private SpeechletResponse getColorFromSession(final Intent intent, final Session session) {
        String speechText;
        boolean isAskResponse = false;

        // Get the user's favorite color from the session.
        String favoriteColor = (String) session.getAttribute(COLOR_KEY);

        // Check to make sure user's favorite color is set in the session.
        if (StringUtils.isNotEmpty(favoriteColor)) {
            speechText = String.format("Your favorite color is %s. Goodbye.", favoriteColor);
            sendToChannel(favoriteColor);
        } else {
            // Since the user's favorite color is not set render an error message.
            speechText =
                    "I'm not sure what your favorite color is. You can say, my favorite color is "
                            + "red";
            isAskResponse = true;
        }

        return getSpeechletResponse(speechText, speechText, isAskResponse);
    }
    
    private SpeechletResponse splashColor(final Intent intent) {
        String speechText;

        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot colorSlot = slots.get(COLOR_SLOT);

        // Check to make sure user's favorite color is set in the session.
        if (colorSlot!=null) {
        	String splashColor = colorSlot.getValue();
            speechText = String.format("Splashing requested color, %s.", splashColor);
            sendToChannel(splashColor);
        } else {
            // Since the user's favorite color is not set render an error message.
            speechText =
                    "I'm sorry I could not understand your color. You can say, splash color red";
        }

        return getSpeechletResponse(speechText, "How can I help you?", true);
    }

    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
            boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }
    
    private void subscribeChannel(){
    	pubnub.subscribe()
        .channels(Arrays.asList(PUBNUB_CHANNEL)) // subscribe to channels
        .execute();
    }
    
    private void unscribeChannel(){
    	pubnub.unsubscribe()
        .channels(Arrays.asList(PUBNUB_CHANNEL))
        .execute();
    }
    
    private void sendToChannel(final String message){
    	pubnub.publish()
        .message(message)
        .channel(PUBNUB_CHANNEL)
        .shouldStore(true)
        .ttl(1)
        .usePOST(true)
        .async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult result, PNStatus status) {
                if (status.isError()) {
                    // something bad happened.
                    System.out.println("error happened while publishing: " + status.toString());
                } else {
                    System.out.println("publish worked! timetoken: " + result.getTimetoken());
                }
            }
        });
    }
}
