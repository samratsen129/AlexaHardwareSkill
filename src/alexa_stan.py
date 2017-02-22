from pubnub.callbacks import SubscribeCallback
from pubnub.pnconfiguration import PNConfiguration
from pubnub.pubnub import PubNub
import grovepi
from grove_rgb_lcd import *

pnconf = PNConfiguration()
pnconf.publish_key = "demo"
pnconf.subscribe_key = "demo"
pnconf.enable_subscribe = True

pubnub = PubNub(pnconfig)
channel = "sbd_987654321"
channel2 = "okButton"

buzzer=8
#set led to output
grovepi.pinMode(led,"OUTPUT")
grovepi.pinMode(buzzer,"OUTPUT")
grovepi.digitalWrite(buzzer,0)
setText('Stan is ready')
setRGB(255,215,0)

class MyListener(SubscribeCallback):
    def status(self, pubnub, status):
        print("status changed: %s" % status)

    def message(self, pubnub, message):
        grovepi.digitalWrite(buzzer,1)
        setText(message.message)
        if message.message == "red":
                setRGB(255,0,0)
        elif message.message == "green":
                setRGB(0,255,0)
        elif message.message == "blue":
                setRGB(0,0,255)
        elif message.message == "gold":
                setRGB(255,215,0)

    def presence(self, pubnub, presence):
        pass

my_listener = MyListener()
pubnub.add_listener(my_listener)
pubnub.subscribe().channels(channel).execute()