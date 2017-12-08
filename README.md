# Introduction
Sample application utilizing Firebase services.

# Table of contents

- [Introduction](#introduction)
- [Table of contents](#table-of-contents)
- [Installation](#installation)
	- [External libraries](#external-libraries)
	- [Screenshots](#screenshots)
- [Architechture](#architechture)
	- [Database](#database)
	- [Entities](#entities)
		- [Sauna](#sauna)
		- [Rating](#rating)
		- [Conversation](#conversation)
		- [Message](#message)

# Installation
You need Google Play Service SDK tools installed to Android Studio.

1. Add your Android Studio sha1 fingerprint to Firebase project
2. Go to your Firebase console and download google-services.json. Add it to app folder.
3. Save config-example.xml to resources with your data as config.xml

## External libraries
- [ImagePickerWithCrop](https://github.com/Tofira/ImagePickerWithCrop)
- which uses [UCrop](https://github.com/Yalantis/uCrop)
- [Mike Gavaghan's Geodesy library](http://www.gavaghan.org/blog/free-source-code/geodesy-library-vincentys-formula-java/)

## Screenshots

<img src="/screenshots/1.png?raw=true" width="440"/> <img src="/screenshots/4.png?raw=true" width="440"/>
<img src="/screenshots/2.png?raw=true" width="440"/> <img src="/screenshots/3.png?raw=true" width="440"/>

# Architechture

## Database
SaunaApp uses Firebase real time database for storing data. At the moment all data access is restricted to logged in users.

Here is a basic representation of the database structure:
<pre>
root
  |_ _hasRated
  |    |__ userId
  |        |__ ratingId: saunaId
  |        
  |_ conversations
  |    |__ userId
  |        |__ conversationId: Conversation
  |        
  |_ messages
  |    |__ userId
  |        |__ conversationId
  |            |__ messageId: Message
  |            
  |_ ratings
  |    |__ ratingId: Rating
  |    
  |_ saunas
  |    |__ saunaId: Sauna
  |    
  |_ users
       |__ userId
           |__ notificationTokens
               |__ token
</pre>

_**NOTE:** As you might have realized, the Conversation - Message structure is not very efficient. Each Conversation and Message is saved twice - for both participants. The data is structured like this mainly for ease of implementation. However, if this project is ever furthered to other than demonstrative purposes, attention should be brought to the issue of storing the data a bit more efficiently._

## Entities

### Sauna
Sauna is arguably the most important, basic entity in the system. Sauna represents a location, owned by a user, displayed on the list and map views.

**Properties**
- _id:_ Sauna id
- _name:_ Name for Sauna
- _description:_ Description text for the sauna
- _latitude:_ Float number representing the latitude (location)
- _longitude:_ Float number representing the longitude (location)
- _owner:_ User id of the owner
- _ownerName:_ Display name for the owner
- _photoPath:_ Possibly the relative path to the image for this sauna in Firebase storage
- _rating:_ Current average of ratings
- _ratingCount:_ Amount of ratings given, maxed to 500 to emphasize newer ratings when calculating the average.

### Rating
Represents a rating given to a Sauna by an user.

**Properties**
- _id:_ Rating id
- _user:_ Id of the user giving this Rating
- _saunaId:_ Sauna id to rate
- _message:_ Short message to accompany rating
- _time:_ Time of the rating (Date)
- _rating:_ Double value representing rating ( 0 <= rating <= 5)

### Conversation
Represents a conversation from a user to another.

**Properties**
- _id:_ Conversation id
- _target:_ Id of the targeted user
- _targetName:_ Display name of the targeted user
- _touched:_ Date of the last time this conversation was touched. Can be used for ordering for example.
- _hasNew:_ Whether conversation holds changes the user has not seen. 0 if nothing, else count of new messages.

### Message
A single message from user to another. Saved separately for each user.

**Properties**
- _id:_ Message id
- _text:_ Text content of the message
- _sender:_ Id of the user that sent the message
- _senderName:_ Display name for the sender
- _target:_ Id of the targeted user
- _saunaId:_ Id of the sauna that this message concerns (optional)
- _date:_ Time of sending the message