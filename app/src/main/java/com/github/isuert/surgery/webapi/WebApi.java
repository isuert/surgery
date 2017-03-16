package com.github.isuert.surgery.webapi;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import com.github.isuert.surgery.models.GlassConfig;
import com.github.isuert.surgery.models.Test;
import com.github.isuert.surgery.models.Xray;

public interface WebApi {
    @GET("get-glass-config.php")
    Call<GlassConfig> getGlassConfig(@Query("id") String id);

    @GET("get-xrays.php")
    Call<List<Xray>> getXrays(@Query("operationId") int operationId);

    @GET("get-tests.php")
    Call<List<Test>> getTests(@Query("operationId") int operationId);

    @GET("show-xray-on-display.php")
    Call<Void> showXrayOnDisplay(@Query("displayId") String displayId,
                                 @Query("xrayId") int xrayId);

    @GET("show-test-on-display.php")
    Call<Void> showTestOnDisplay(@Query("displayId") String displayId,
                                 @Query("testId") int testId);

    @FormUrlEncoded
    @POST("save-note.php")
    Call<Void> saveNote(@Field("text") String text,
                        @Field("operationId") int operationId);

    @Multipart
    @POST("save-picture.php")
    Call<ResponseBody> savePicture(@Part MultipartBody.Part picture,
                                   @Part("operationId") RequestBody operationId);
}
