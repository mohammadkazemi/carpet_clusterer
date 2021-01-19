package com.example.carpetdetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public class MainActivity extends AppCompatActivity {


    Button btnTakePhoto, btnUpload, btnResult;
    TextView imgPath, imgName;
    EditText resultTxt;
    private Image uploadImage;
    private Bitmap myBitmap;
    private String baseUrl = "http://10.0.2.2:8000";
//    private String baseUrl = "http://185.17.123.5:8000";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePhoto = findViewById(R.id.button2);
        imgPath = findViewById(R.id.textView);
        imgName = findViewById(R.id.textView);
        btnUpload = findViewById(R.id.button);
        btnResult = findViewById(R.id.button3);
        resultTxt = findViewById(R.id.textView3);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.create(MainActivity.this).start();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploadImage != null) {
                    //  1/9/21 read image from storage
//                    File f = getImageFile();

                    File f = new File(uploadImage.getPath());
                    //  1/9/21 upload image with retrofit
                    RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), f);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("file", f.getName(), reqFile);
                    Service service = new Retrofit.Builder().baseUrl(baseUrl).build().create(Service.class);
                    Log.d("mdev", "sending reaquest");

                    // TODO: 1/9/21 requesting in new thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Call<okhttp3.ResponseBody> req = service.postImage(body);
                            req.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    // Do Something with response
                                    // TODO: 1/9/21 parse response
                                    Toast.makeText(getApplicationContext(), "successfull request", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    //failure message
                                    Toast.makeText(getApplicationContext(), "successfull request", Toast.LENGTH_LONG).show();
                                    t.printStackTrace();
                                }
                            });
                        }
                    }).start();


                    // TODO: 1/9/21 show response

                } else {
                    Toast.makeText(getApplicationContext(), "there is no image to upload", Toast.LENGTH_SHORT).show();
                }
            }

            private File getImageFile() {
                //create a file to write bitmap data
                File f = new File(getApplicationContext().getCacheDir(), uploadImage.getName());
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Convert bitmap to byte array
                Bitmap bitmap = getImageBitmap(uploadImage.getPath() + uploadImage.getName());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return f;
            }
        });

        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Service service = new Retrofit.Builder().baseUrl(baseUrl).build().create(Service.class);
                        Call<ResponseBody> req = service.getPredictionsClusters();
                        req.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                Toast.makeText(getApplicationContext(), "successfull request", Toast.LENGTH_LONG).show();
                                try {

                                    resultTxt.setText(response.body().string());
//                                    resultTxt.setMovementMethod(new ScrollingMovementMethod());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });
                    }
                }).start();
            }
        });
    }


    interface Service {
        @Multipart
        @POST("/uploadfile/")
        Call<ResponseBody> postImage(@Part MultipartBody.Part image);

        @GET("/get_predictions/")
        Call<ResponseBody> getPredictions();

        @GET("/get_predictions_clusters/")
        Call<ResponseBody> getPredictionsClusters();

    }

    private Bitmap getImageBitmap(String path) {
        File imgFile = new File(path);
        if (imgFile != null) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return myBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
//            /storage/emulated/0/DCIM/Camera/IMG_20200814_221702.jpg

            // Get a list of picked images
            List<com.esafirm.imagepicker.model.Image> images = ImagePicker.getImages(data);
            // or get a single image only
            uploadImage = ImagePicker.getFirstImageOrNull(data);
            imgName.setText(uploadImage.getName());
            imgPath.setText(uploadImage.getPath());
            Toast.makeText(getApplicationContext(), "we got your image", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}