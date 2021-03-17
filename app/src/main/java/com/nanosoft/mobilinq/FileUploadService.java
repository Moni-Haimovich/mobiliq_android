package com.nanosoft.mobilinq;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {
    @Multipart
    @Headers({"Accept: */*"})
    @POST("properties")
    Call<ResponseBody> uploadMultipleFilesDynamic(
            @Part("description") RequestBody description,
            @Part("name") RequestBody name,
            @Part("address") RequestBody address,
            @Part("lat") RequestBody lat,
            @Part("lng") RequestBody lng,
            @Part("images_meta[]") List<UploadedProperty> images_meta,
            @Part List<MultipartBody.Part> images);
}