package com.sdb.da.test;

import java.util.Arrays;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

public class Test {

	private PNConfiguration pnConfiguration = new PNConfiguration();
	private static final String PUBNUB_CHANNEL = "sbd_987654321";
    
    private PubNub pubnub;
    
    public Test(){
    	pnConfiguration.setSubscribeKey("sub-c-xxxxx");
        pnConfiguration.setPublishKey("pub-c-xxxxxxx");
    	pubnub = new PubNub(pnConfiguration);
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Test test = new Test();
		
		test.subscribeChannel();

		//test.sendToChannel("blue");
	}

	
    private void subscribeChannel(){
    	
    	pubnub.addListener(new SubscribeCallback() {
	        @Override
	        public void status(PubNub pubnub, PNStatus status) {
	 
	 
	            if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
	                // This event happens when radio / connectivity is lost
	            }
	 
	            else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
	 
	                // Connect event. You can do stuff like publish, and know you'll get it.
	                // Or just use the connected event to confirm you are subscribed for
	                // UI / internal notifications, etc
	             
	                if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
	                    pubnub.publish().channel(PUBNUB_CHANNEL).message("hello!!").async(new PNCallback<PNPublishResult>() {
	                        @Override
	                        public void onResponse(PNPublishResult result, PNStatus status) {
	                            // Check whether request successfully completed or not.
	                            if (!status.isError()) {
	 
	                                // Message successfully published to specified channel.
	                            	System.out.println("Published");
	                            }
	                            // Request processing failed.
	                            else {
	 
	                                // Handle message publish error. Check 'category' property to find out possible issue
	                                // because of which request did fail.
	                                //
	                                // Request can be resent using: [status retry];
	                            }
	                        }
	                    });
	                }
	            }
	            else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
	 
	                // Happens as part of our regular operation. This event happens when
	                // radio / connectivity is lost, then regained.
	            }
	            else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
	 
	                // Handle messsage decryption error. Probably client configured to
	                // encrypt messages and on live data feed it received plain text.
	            }
	        }
	 
	        @Override
	        public void message(PubNub pubnub, PNMessageResult message) {
	            // Handle new message stored in message.message
	            if (message.getChannel() != null) {
	                // Message has been received on channel group stored in
	                // message.getChannel()
	            	System.out.println("Received1" + message.getMessage());
	            }
	            else {
	                // Message has been received on channel stored in
	                // message.getSubscription()
	            	System.out.println("Received2");
	            }
	 
	            /*
	                log the following items with your favorite logger
	                    - message.getMessage()
	                    - message.getSubscription()
	                    - message.getTimetoken()
	            */
	        }
	 
	        @Override
	        public void presence(PubNub pubnub, PNPresenceEventResult presence) {
	 
	        }
	    });
    	
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
