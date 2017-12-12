# Introduction
This application allows users to share, view and review saunas, and send messages. Application uses user's position to show distance for each sauna. Application is built on [Firebase](https://firebase.google.com/) and used services include **Firebase Realtime Database, Cloud Storage, Authentication, Firebase Functions** and **Crash Reporting.** Application also displays ads for the users.

The project was implemented as part of the Mobile Project course at JAMK University of Applied Sciences.

### Project timeline
At the moment the project has taken around 150 hours of work. See [the work log](https://drive.google.com/open?id=1CPqRP2ELmDbUH723Z50xf39h4uKDOUJgJaM_ro6_PMA) for details about tasks and timeline of the project.

[Presentation](https://drive.google.com/open?id=14kYUpZFZhNq7oJilqREiSaBiIBPPn6zMNNPhQwlNnzo)

# Table of contents

- [Introduction](#introduction)
    - [Project timeline](#project-timeline)
- [Table of contents](#table-of-contents)
- [Installation](#installation)
	- [External libraries](#external-libraries)
	- [Screenshots](#screenshots)
- [Architecture](#architecture)
    - [Application structure](#application-structure)
        - [MainActivity](#mainactivity)
        - [SaunaDetailsActivity](#saunadetailsactivity)
        - [UserProfileActivity](#userprofileactivity)
        - [EditSaunaActivity](#editsaunaactivity)
        - [ConversationListActivity](#conversationlistactivity)
        - [MessageListActivity](#messagelistactivity)
	- [Database](#database)
	- [Entities](#entities)
		- [Sauna](#sauna)
		- [Rating](#rating)
		- [Conversation](#conversation)
		- [Message](#message)
	- [Backend](#backend)
- [Todo](#todo)
- [Project notes and evaluation](#project-notes-and-evaluation)

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

<img src="/screenshots/login.png?raw=true" width="215"/> <img src="/screenshots/list.png?raw=true" width="215"/>
<img src="/screenshots/map.png?raw=true" width="215"/> <img src="/screenshots/profile.png?raw=true" width="215"/>
<img src="/screenshots/details1.png?raw=true" width="215"/> <img src="/screenshots/details2.png?raw=true" width="215"/>
<img src="/screenshots/conversations.png?raw=true" width="215"/> <img src="/screenshots/messages.png?raw=true" width="215"/>
<img src="/screenshots/edit.png?raw=true" width="215"/> <img src="/screenshots/push.png?raw=true" width="215"/>

# Architecture

## Application structure

Here is a basic representation of the applications Activities and their hierarchy:

<pre>
MainActivity
  |_ SaunaDetailsActivity
  |_ UserProfileActivity
  |   |__ EditSaunaActivity
  |_ ConversationListActivity
      |__ MessageListActivity
     
_ LoginActivity
_ UCropActivity
</pre>

### LoginActivity
Entry view for new users. Allow users to log in with Google account.

### MainActivity
This is the "entry" view for the application (logged in users). User interface is divided to three fragments shown as tabs.

**SaunaListFragment**

Displays saunas as a list. Tapping item opens SaunaDetailsActivity.

**SaunaMapFragment**

Displays saunas as markers on map. Tapping marker opens SaunaDetailsActivity.

**UserProfileFragment**

Displays list of actions:
- _Messages_ -> Navigate to ConversationListActivity
- _Your saunas_ -> Navigate to UserProfileActivity (a list of user's saunas)
- _Sign out_ -> Sign out and navigate to LoginActivity

### SaunaDetailsActivity

Activity for displaying information about sauna. Displays weighted rating based on user reviews,
sauna description, picture (if set), location on map, component for giving rating,
and five latest user reviews.

FAB takes user to MessageListView, where conversation with sauna owner can be initiated.

### UserProfileActivity

Displays a list of user's saunas. Clicking item opens EditSaunaActivity for selected sauna.
FAB opens empty EditSaunaActivity for creating new sauna.

### EditSaunaActivity

Activity for editing sauna. A name and description can be set for sauna. Image can be picked
by clicking image thumbnail. This launches ImagePicker and when image is selected from gallery,
UCrop for cropping image. Crop is forced to 1:1 ratio to ensure proper display for images. Also sauna
location can be picked on the map. By default location is set to user's current location.

### ConversationListActivity

Shows list of user's Conversations, ordered by relevance (touched-timestamp).
If Conversation contains unread messages, icon is shown on list with the amount of unread messages.

Tapping item takes user to MessageListActivity for selected Conversation.
 
### MessageListActivity

View for a single Conversation. Shows sent and received messages, and their respective times
in time order. User can send a new message by writing it at the bottom and tapping "SEND".

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
Sauna is the most important, basic entity in the system. Sauna represents a location, owned by a user, displayed on the list and map views.

**Properties**
- _id:_ Sauna id
- _name:_ Name for Sauna
- _description:_ Description text for the sauna
- _latitude:_ Float number representing the latitude (location)
- _longitude:_ Float number representing the longitude (location)
- _owner:_ User id of the owner
- _ownerName:_ Display name for the owner
- _photoPath:_ Relative path to the image for this sauna in Firebase Storage (if set)
- _rating:_ Current average of ratings
- _ratingCount:_ Amount of ratings given, maxed to 500 to emphasize newer ratings when calculating the average.
This number is off the top of my head and can be adjusted to lower/increase the impact of new ratings.

### Rating
Represents a rating given to a Sauna by an user.

**Properties**
- _id:_ Rating id
- _userId:_ Id of the user giving this Rating
- _userName:_ Display name for the user giving this Rating
- _saunaId:_ Sauna id to rate
- _message:_ Short message to accompany rating
- _time:_ Client time of the rating (Date)
- _rating:_ Double value representing rating (0 <= rating <= 5)

### Conversation
Represents a conversation from a user to another.

**Properties**
- _id:_ Conversation id
- _target:_ Id of the targeted user
- _targetName:_ Display name of the targeted user
- _touched:_ Server timestamp of the last time this conversation was touched, stored as a long number.
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
- _date:_ Client time of sending the message (Date)

## Backend
SaunaApp uses a Firebase function as a light backend to monitor message events in the database to 
send push notifications to client devices. The backend in found in [this repository.](https://github.com/RoniPa/SaunaApp-backend)
Notification tokens for each users' device are stored within the Realtime Database. Function is triggered on the onCreate event at the location of user's messages within database (messages/{uid}/{conversationId}/).

# Todo

**Things to work on:**
- Improve Sauna edit view (especially map)
- Optimize conversation/message structure
- Possibly sorting for SaunaListFragment
- Update distances in SaunaListFragment when device location is turned on
- Update rating bar in SaunaDetailActivity when new rating is given
- Move away from deprecated FusedLocationApi
- Enable multiple images
- Change Firebase Realtime Database to Cloud Firestore (beta)
- Gracefully destroy listeners on sign out

# Project notes and evaluation

All in all most of the functionality described in the original project plan was implemented (list filtering missing). More resources was used than was initally resourced, but this was affected by the long span on which the project was implemented (after failing the initial schedule). This caused some work updating platform & development tools and moving away from deprecated APIs. Time was also used with the UCrop library, trying to modify the library to the new permission API with bad success. This was how ever intentional with the extended schedule.

The original schedule was somewhat utopistic as I was working more or less full time at the time, but after furthering development I feel that the goals were accomplished satisfactorily. Some deviation occured from the initial UI and architechture plans, but to my experience this is quite common in modern, agile software development. Overall, I consider the project succesfull, despite the failure to keep up the initial schedule.
