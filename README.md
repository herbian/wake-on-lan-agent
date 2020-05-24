## WOLAgent app

This app is used as a middleman inside the home network, which serve a HTTP server and wake the PC up if the message matches

You will need to define two variable in local.properties file:

1. WOL_PASSWORD: used for authenticating to the /wakeup path
2. WOL_MACADDRESS: the mac address of your PC's network card
