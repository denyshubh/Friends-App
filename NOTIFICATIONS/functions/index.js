'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
let firebaseConfig = JSON.parse(process.env.FIREBASE_CONFIG);

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((change, context) => {

const user_id = context.params.user_id;
const notification_id = context.params.notification_id;

console.log("The User Id is : ",user_id);


if(!change.after.val()) {

return console.log('A Notification has been deleted from database : ', notification_id);
}

          const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');
          return fromUser.then(
                fromUserResult => {

                  const from_user_id =fromUserResult.val().from;
                  console.log('You have a new notification from : ', from_user_id);

                  const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
                  const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

                  return Promise.all([userQuery,deviceToken]).then( result => {

                      const userName = result[0].val();
                      const token_id = result[1].val();

                      const payload = {
                        notification : {
            
                          title : "New Friend Request",
                          body : `${userName} has sent you request`,
                          icon : "default",
                          click_action : "com.knstech.friendsapp2_TARGET_NOTIFICATION"
                        },
                        data: {
                          from_user_id: from_user_id
                        }
                      };
                      return admin.messaging().sendToDevice(token_id , payload).then(response => {

                        console.log('This was the Notifiaction Feature');
                         
                          return null;
                      });

                    }
                  );
                }
          );


});