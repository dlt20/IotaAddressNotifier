<a href="http://iotawatch.it"><img src="http://iotawatch.it/header_new.png" title="IOTA Watch Bot" alt="IOTA Watch Bot"></a>

# Iota Address Notifier

The IOTA Address Notifier (IAN) is a Telegram bot that offers mobile notifications about transactions on IOTA addresses. Users can add any IOTA address to the bot, and will receive notifications if tokens are sent to or from this address. Additionally, the bot sends a notification if such a transaction gets confirmed by the IOTA network. In the last two years the bot was running in a closed alpha and recently a beta stage. 

You can either use the bot by joining the running version of IAN, or set up an instance yourself with the code available in this repo. The software is still in a beta phase and not production ready. Limitations and errors might occur.

The bot offers further features, like spam control, an token auto-follow feature, balance calculation in USD/EUR/BTC:


[![](http://iotawatch.it/main_new.png)]()


---

## Table of Contents


- [Features](#features)
- [Installation](#installation)
- [FAQ](#faq)
- [Support](#support)
- [Disclaimer](#disclaimer)


---

## Features

Before a user can start to interact with the bot, a user has to agree to not use a seed with the bot. To do so, the user needs to reply the command /IWillNotUseASeed to the bot.
The following describes all commands that can be used with the bot after agreeing to not use a seed:

| Command | Description |
|-|-|
| **/startWatch X** | Request the bot to start observing address X (substitute X with address). |
| **/stopWatch X** | Request the bot to stop observing address X (substitute X with address). |
| **/help** | Returns a list of all available commands to the user. |
| **/about** | Returns more general inforation about the bot to the user. |
| **/donate** | Returns the bot's IOTA donation address to the user. |
| **/showStats** | Returns the IOTA Tangle's performance data and IOTA price back to the user. |
| **/showWatch** | Returns an overview of all - for this user - observed addresses back to the user. |
| **/showBalances** | Returns an overview of all - for this user - observed addresses (including IOTA balance and price in USD/EURO/BTC) back to the user. |
| **/deleteUser** | Command to delete all user related data. User can ultimatively delete all data by replying /YESdeleteUser after the first deletion request. |
| **/setReattach** | IAN can send notifications if transactions/bundles are reattached. This command activates/deactives the feature. |
| **/setAutoFollow** | IAN can automatically follow funds on addresses, if the the AutoFollow feature is activated. If IOTAs are send from an address that the bot is observing, IAN will start watching the address where most of the IOTAs were sent to. The old address will no longer be observed, when the transaction has been confirmed. In the current beta status, IAN will automatically start watching the address were the most IOTAs were sent to. |

- AutoFollow Example: Your are observing address A that has 10 IOTAs. In a new transaction 8 IOTAs are sent to address B, the remaining 2 IOTAs will be send to address C. As soon as the transaction is confirmed, IAN will stop watching address A and start observing address B.

The following commands are only processed by the bot, if send from the bot's operator (identified by the user's Telegram ID)

| Command | Description |
|-|-|
| **/getUsers** | Returns a list of all active/inactive users of the bot. |
| **/messageAll XYZ** | Requests the bot to send message XYZ to all active users of the bot. |
| **/messageOne\*\*TELEGRAM_TARGET_ID++XYZ** | Request the bot to send message XYZ to a single user with the Telegram ID: TELEGRAM_TARGET_ID. |
---

## Installation

- Create a new telegram bot by interacting with the Telegram Bot Father. A guidline to create the bot, can be found <a href="https://core.telegram.org/bots#creating-a-new-bot" target="_blank">`here`</a>. After creation of the bot, you should posses the Telegram Bot token, which is 
referred to as *TELEGRAM_SECRET* in the following.

- Overwrite the config file (*/botStorage/BotConfiguration.txt*) , the following fields have to be set:

```
{
"externalAesKeyFragment":"AES_KEY_PART",
"iotaZMQLink":"IOTA_NODE_IP:5556",
"iotaApiLink":"IOTA_NODE_IP:14265",
"telegramSecret":"TELEGRAM_SECRET",
"masterTelegramID":"OPERATOR_TELEGRAM_ID",
"initialDonationAddress":"IOTA_DONATION_ADDRESS",
}
```

| Configuration | Description |
|-|-|
| AES_KEY_PART   | 12 characters that the user can choose 0-1, a-A, A-Z. AES_KEY_PART is party of the key, that bot uses to encrypt stored data. |
| IOTA_NODE_IP:PORT | Reference to the IOTA Node's IP and ZMQ port (usually 5556). |
| IOTA_NODE_IP:PORT | Reference to the IOTA Node's IP and API port (usually 14265). |
| TELEGRAM_SECRET   | Telegram bot token, in the form: ABCD:XYZ |
| OPERATOR_TELEGRAM_ID | Telegram ID (number format) of the bot's operator, has special privileges. If you don't know your userID, start the bot with a random OPERATOR_TELEGRAM_ID, and send the comman **\userID** to the bot. The bot will return your user ID that you can then add to the config, and restart the bot. |
| IOTA_DONATION_ADDRESS   | Standard donation address, that the bot returns to users. |

- You might operate the bot in your development environment, or move the bot to a server. If you're running the bot on the same server where the IOTA Node is located, the *IOTA_NODE_IP* might be *localhost*.

- Compile .jar and start jar on your device.
---

## FAQ

- **Can I use a seed with the bot?**
    - No, this bot isn't a wallet. Seeds must always stay private and must not be entered. To make sure no user enters a seed, the bot informs the users repeatedly about the differences between seeds and addresses. In addition the bot does only work if the users agree and confirm that they will not use a seed.

- **Will you provide a compiled version of the code?**
    - No, I'd recommend to understand the code first, and then compile it.

- **Is this bot maintained by the IOTA Foundation?**
    - IAN is a third-party software and not related to or associated with the IOTA Foundation.
    
- **Which external libraries are used?**
    - <a href="https://github.com/iotaledger/iota-java" target="_blank">`"IOTA Java API Library"`</a>, **Version: 0.9.6**, <a href="https://opensource.org/licenses/Apache-2.0" target="_blank">`Apache-2.0 License`</a>

    - <a href="https://github.com/pengrad/java-telegram-bot-api" target="_blank">`"Telegram Bot API for Java"`</a>, **Version: 3.5.2**, <a href="https://opensource.org/licenses/Apache-2.0" target="_blank">`Apache-2.0 License`</a>

    - <a href="https://github.com/zeromq/jeromq" target="_blank">`"Pure Java ZeroMQ"`</a>, **Version: 0.4.4**, <a href="https://opensource.org/licenses/MPL-2.0" target="_blank">`MPL-2.0 license`</a>

    - <a href="https://github.com/stleary/JSON-java" target="_blank">`"JSON in Java"`</a>, **Version: 20190722**, <a href="https://github.com/stleary/JSON-java/blob/master/LICENSE" target="_blank">`Copyright (c) 2002 JSON.org`</a>


---

## Support

Reach out at one of the following places!

- In the <a href="https://discord.iota.org" target="_blank">`IOTA Discord`</a>: Felix - iotawatch.it#8563

If you want to support the project, consider donating here:
 - Support with IOTAs: <a href="https://thetangle.org/address/9UMQUODAHMRHCHIEPB9LFANMJFYQPQKQSFLWNAQVTRRXTUNBUJBUTHAHQJAFBIAZIWLVIXDQDHYBUUBTXDVMQWIEWC" target="_blank">`Donation (TheTangle.com)`</a>

---

## Disclaimer

Use at your own risk. No guarantee for accurate, up-to-date or complete information.

Bot uses price data **powered by CoinGecko API**, API is free of charge as of 3rd of September 2020.

- **[MPL-2.0 license](https://opensource.org/licenses/MPL-2.0)**
