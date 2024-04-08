package com.example.social.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAHOCiXKA:APA91bGShZv8D1cxTDGd8_LQyRke2ea-xJDEL6oA8s7vQDeJ5QPvwmB5Sa-JZsw_tqgMSqvRq_QGq6p0bBmYsHmKxh7hq4Gv8Bug3EpVVrUafwclEUJFSYlJbHmeLUrgHxpGl9JzZOTN"
//            "Authorization:key=BJyuzHWybVVZnpmKSiEX5SEoXRsayd6Nh4yOII1rv2F1bDD"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
