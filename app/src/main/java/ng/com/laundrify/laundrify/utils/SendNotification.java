package ng.com.laundrify.laundrify.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.android.volley.VolleyLog.TAG;

public class SendNotification {

    public SendNotification (final Context context, String token, String title, String body) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://laundrify-ba98a.firebaseapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NotificationApi notificationApi = retrofit.create(NotificationApi.class);

        Call<ResponseBody> call = notificationApi.sendNotification(token, title, body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String res = response.body().toString();
                    // Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Sent Message onResponse: " + res);
                } else {
                    Toast.makeText(context, "Couldn't send Notification. Please make contact ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
