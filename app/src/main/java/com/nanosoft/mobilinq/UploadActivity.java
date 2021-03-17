package com.nanosoft.mobilinq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadActivity extends AppCompatActivity {
    private static final String TAG = UploadActivity.class.getSimpleName();

    private ArrayList<String> arrayList;
    Button btnUpload, btnChoose;
    private final int REQUEST_CODE_READ_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent myIntent = getIntent(); // gets the previously created intent

        btnChoose = findViewById(R.id.btnChoose);
        btnChoose.setOnClickListener(v -> {
            // Display the file chooser dialog
            showChooser();
        });

        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> uploadImagesToServer());

        arrayList = new ArrayList<>();
    }

    private void showChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_READ_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_READ_STORAGE) {
                if (resultData != null) {
                    if (resultData.getClipData() != null) {
                        int count = resultData.getClipData().getItemCount();
                        int currentItem = 0;
                        while (currentItem < count) {
                            Uri imageUri = resultData.getClipData().getItemAt(currentItem).getUri();
                            currentItem = currentItem + 1;

                            try {
                                arrayList.add(FileChooser.getPath(this, imageUri));
                            } catch (Exception e) {
                                Log.e(TAG, "File select error", e);
                            }
                        }
                    } else if (resultData.getData() != null) {
                        final Uri uri = resultData.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            arrayList.add(FileChooser.getPath(this, uri));
                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }
                }
            }
        }
    }

    private void uploadImagesToServer() {
        List<MultipartBody.Part> parts = new ArrayList<>();
        //ArrayList<String> filePaths = new ArrayList<>();
        List<UploadedProperty> images_meta = new ArrayList<UploadedProperty>();
        for (int i = 0; i < arrayList.size(); i++) {
            parts.add(prepareFilePart("images", arrayList.get(i)));
            UploadedProperty up = new UploadedProperty("123","a description");
            images_meta.add(up);
        }

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(600, TimeUnit.SECONDS)
                .connectTimeout(600, TimeUnit.SECONDS)
                .build();

        // add the description part within the multipart request
        RequestBody description = createPartFromString("hello, this is description speaking");
        RequestBody name = createPartFromString("hello, this is name");
        RequestBody address = createPartFromString("hello, this is address");
        RequestBody lat = createPartFromString("31.2254");
        RequestBody lng = createPartFromString("34.88234");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://mobilinq.co/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        FileUploadService service = retrofit.create(FileUploadService.class);
        Call<ResponseBody> call = service.uploadMultipleFilesDynamic(description, name,address,lat,lng, images_meta, parts);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(UploadActivity.this, "Success " + response.message(), Toast.LENGTH_LONG).show();
                // String s= response.errorBody().string();
                // Log.e("test", s);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error " + t.getMessage());
            }
        });

    }

    private void showProgress() {
        btnChoose.setEnabled(false);
        btnUpload.setVisibility(View.GONE);
    }

    private void hideProgress() {
        btnChoose.setEnabled(true);
        btnUpload.setVisibility(View.VISIBLE);
    }


    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, String filePath) {
        // use the FileUtils to get the actual file by uri
        File file = new File(filePath);//FileUtils.getFile(this, fileUri);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
}