# Introduction
Sample application utilizing Firebase services.

# Table of contents

- [Introduction](#)
- [Table of contents](#)
- [Installation](#)
	- [External libraries](#)
	- [Screenshots](#)
- [Architechture](#)
	- [Database](#)
	- [Entities](#)
		- [Sauna](#)
			- [Properties](#)
		- [Rating](#)
		- [Properties](#)
	- [Conversation](#)
		- [Properties](#)
	- [Message](#)
		- [Properties](#)

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

NOTE! As you might have realized, the Conversation - Message structure is not very efficient. Each Conversation and Message is saved twice - for both participants. The data is structured like this mainly for ease of implementation. However, if this project is ever furthered to other than demonstrative purposes, attention should be brought to the issue of storing the data a bit more efficiently.

## Entities

### Sauna
Sauna is arguably the most important, basic entity in the system. Sauna represents a location, owned by a user, displayed on the list and map views.

#### Properties
- id: Sauna id
- name: Name for Sauna
- description: Description text for the sauna
- latitude: Float number representing the latitude (location)
- longitude: Float number representing the longitude (location)
- owner: User id of the owner
- ownerName: Display name for the owner
- photoPath: Possibly the relative path to the image for this sauna in Firebase storage
- rating: Current average of ratings
- ratingCount: Amount of ratings given, maxed to 500 to emphasize newer ratings when calculating the average.

### Rating
Represents a rating given to a Sauna by an user.

#### Properties
- id: Rating id
- user: Id of the user giving this Rating
- saunaId: Sauna id to rate
- message: Short message to accompany rating
- time: Time of the rating (Date)
- rating: Double value representing rating ( 0 <= rating <= 5)

### Conversation
Represents a conversation from a user to another.

#### Properties
- id: Conversation id
- target: Id of the targeted user
- targetName: Display name of the targeted user
- touched: Date of the last time this conversation was touched. Can be used for ordering for example.
- _hasNew: Whether conversation holds changes the user has not seen. 0 if nothing, else count of new messages.

### Message
A single message from user to another. Saved separately for each user.

#### Properties
- id: Message id
- text: Text content of the message
- sender: Id of the user that sent the message
- senderName: Display name for the sender
- target: Id of the targeted user
- saunaId: Id of the sauna that this message concerns (optional)
- date: Time of sending the message