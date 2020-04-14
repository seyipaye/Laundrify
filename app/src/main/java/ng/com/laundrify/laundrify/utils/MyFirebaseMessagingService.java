package ng.com.laundrify.laundrify.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ng.com.laundrify.laundrify.MainActivity;
import ng.com.laundrify.laundrify.R;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String NOTIF_CHANNEL_ID = "Sdcewrfer";
    String TAG = "Test";

    private DatabaseReference customerDB;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notification = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setSmallIcon(R.drawable.ic_local_laundry_service_black_24dp)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(remoteMessage.getNotification().getBody()))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();
            } else {
                notification = new NotificationCompat.Builder(this)
                        .setContentTitle("Laundrify stopped running")
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setSmallIcon(R.drawable.ic_local_laundry_service_black_24dp)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(remoteMessage.getNotification().getBody()))
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build();
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(55787, notification);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.i(TAG, "onNewToken: Token is... " + s);

        // Upload token
        if (customerDB != null) {
            customerDB.child("Info/NotificationToken").setValue(s);
        } else {
            if (intializeUserDB()) customerDB.child("Info/NotificationToken").setValue(s);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            channel = new NotificationChannel(NOTIF_CHANNEL_ID, getString(R.string.channel_name), importance);
            channel.setShowBadge(false);
            channel.setDescription(getString(R.string.channel_description));

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public boolean intializeUserDB () {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            customerDB = database.getReference().child("Users").child("Customers").child(user.getUid());
            return true;
        } else {
           return false;
        }
    }
}
