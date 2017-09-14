

### Tech in the input activities options
09/05/2017

I need to provide: common option, self-edit option, temp-option.

The common option and self-edit option should be the same data structure.

{"activity":"color",.. }

But the common options should be saved in diff variables, which can be modified easily.

I want to build a "more" option for user. 
	


### About the input activities options
09/02/2017
I'm thinking that the acitivities option for users is not good.
The reason is :
 I cannot attract users' attention to my app without providing the self-edit activity.

So it is about user feeling. 

They don't need some function like self-edit acitivity. Actually, they don't even take a look at these activities details. But they don't know that, they think they need to modify their each activity, which may give them a graceful feeling.

So, I need to provide this function, even it is not an useful way to deal with their daily acivities. 

I'm not sure whether it is a right way to desig the TimeGo. So, I'm still thinking of it.

### About the button function in menu bar
08/28/2017
The functions in menu should be changed.
The current choices are:
1. sign in/up or sign out and feedback

I think We should provide the most common function for user, for most user, they don't even use the sign in button again.

so, the button for each I think should be:
- the add:

- the display:

- the setting:



### About the choices of event 
08/15/2017
Just now, I find it may be bad to provide only 5 choices to users.("study", "entertain", "sleep", "trash", "exercise")
There are different people want to use it having different attitude. Some want to track things in detail: like eating, shopping, transportation. And others want to track things in the abstract: useful, value, necessary.

So, may be I should provide different mode for people to track their time.

And how to implement this function in a little package is a challenge.  




### About the basic choices for human activities.
08/12/2017
Today, One thing annoying me a lot. That is: Do I need to implement "More" option in add_records, such as: transportation, shop, body wash.. 
The answer I think is : NO!

The reason is simple: This app is for recording and analysing how long a person spent on sleep, study, exercise. So we don't need to care about the details about shopping etc.

So, the basic choices are: 
Study, Entertain, Sleep, Exercise, Trash.

PS: and I need to think about  "study" or "work", "exercise" or "sport".
 
