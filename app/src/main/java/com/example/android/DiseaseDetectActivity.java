package com.example.android;

import static android.util.Base64.*;
import static java.lang.Integer.parseInt;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;


public class DiseaseDetectActivity extends AppCompatActivity {

    private TextView replyMsg;
    ImageView img_photo;
    private ImageButton capture;
    private ImageButton openAlbum;
    final int TAKE_PHOTO = 1;
    Uri imageUri;
    File outputImage;
    public static String ipAddress = "192.168.0.102";
    static int portNumber = 6666;

    public static final int REQUEST_CODE_ALBUM = 102; //album
    private static final int UPDATE_ok = 0;
    private static final int UPDATE_UI = 1;
    private static final int ERROR = 2;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detect);

        this.setTitle("稻田健康管家");
        Intent intent =getIntent();

        initViews();
        initEvent();
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.configNet:
                Intent it=new Intent(DiseaseDetectActivity.this,GetValue.class);//MainActivity
                startActivity(it);
                finish();

                break;
            case R.id.lookDst:
                Toast.makeText(getApplicationContext(), ipAddress+":"+portNumber, Toast.LENGTH_LONG).show();

            default:
        }
        return true;
    }


//    @SuppressLint("WrongViewCast")
    private void initViews() {
        img_photo = findViewById(R.id.img_photo);
        replyMsg = findViewById(R.id.replyMsg);
        capture = findViewById(R.id.capture);
        openAlbum = findViewById(R.id.openAlbum);

        replyMsg.setText("水稻疾病检测系统"); // Set default display text
        img_photo.setImageResource(R.mipmap.sample_leaf); // Set default display image
    }


    private void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                });
    }

    private void openAlbum() {

    }


    private void initEvent() {
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filename = "test.png";
                outputImage = new File(getExternalCacheDir(),filename);
                try {if (outputImage.exists()){
                    outputImage.delete();
                }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(DiseaseDetectActivity.this, "com.example.android.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

        openAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent=new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");

                startActivityForResult(intent,REQUEST_CODE_ALBUM);

            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {

                    try {

                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        img_photo.setImageBitmap(bitmap);
                        getLocationAndStartNetThread();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_CODE_ALBUM:

                if (resultCode == RESULT_OK) {
                    try {

                        imageUri= data.getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        img_photo.setImageBitmap(bitmap);

                        getLocationAndStartNetThread();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }


                break;
            default:
                break;
        }
    }


    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = encodeToString(bitmapBytes, DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static String imageToBase64(File path){
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(path);

            data = new byte[is.available()];

            is.read(data);

            result = encodeToString(data, NO_CLOSE);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    static public Bitmap compressBitmap(Bitmap beforBitmap, double maxWidth, double maxHeight) {
        float beforeWidth = beforBitmap.getWidth();
        float beforeHeight = beforBitmap.getHeight();
        if (beforeWidth <= maxWidth && beforeHeight <= maxHeight) {
            return beforBitmap;
        }

        float scaleWidth =  ((float) maxWidth) / beforeWidth;
        float scaleHeight = ((float)maxHeight) / beforeHeight;
        float scale = scaleWidth;
        if (scaleWidth > scaleHeight) {
            scale = scaleHeight;
        }
        Log.d("BitmapUtils", "before[" + beforeWidth + ", " + beforeHeight + "] max[" + maxWidth
                + ", " + maxHeight + "] scale:" + scale);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap afterBitmap = Bitmap.createBitmap(beforBitmap, 0, 0,
                (int) beforeWidth, (int) beforeHeight, matrix, true);
        return afterBitmap;
    }

    private void getLocationAndStartNetThread() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        // Print latitude and longitude to the console
                        Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
                    } else {
                        // If location is null, request location updates
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(1000); // 1 second interval for a quick location fix

                        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                fusedLocationClient.removeLocationUpdates(this); // Stop receiving updates after first fix
                                if (locationResult != null && locationResult.getLocations().size() > 0) {
                                    Location latestLocation = locationResult.getLastLocation();
                                    latitude = latestLocation.getLatitude();
                                    longitude = latestLocation.getLongitude();

                                    // Print the location obtained from requestLocationUpdates
                                    Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);

                                    // Start the network thread after location is obtained
                                    startNetThreadWithLocation();
                                }
                            }
                        }, Looper.getMainLooper());
                    }

                    // If last location was available, start the network thread immediately
                    if (latitude != 0.0 && longitude != 0.0) {
                        startNetThreadWithLocation();
                    }
                });
    }


    private void startNetThreadWithLocation() {
        replyMsg.setText("检测中...");

        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                try {
                    Socket socket = new Socket();
                    InetSocketAddress isa = new InetSocketAddress(ipAddress, portNumber);
                    socket.connect(isa, 5000);

                    Message msg1 = new Message();
                    msg1.what = UPDATE_ok;
                    msg1.obj = socket;
                    handler.sendMessage(msg1);

                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);

                    // Convert image to Base64
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    String imageBase64 = bitmapToBase64(bitmap);

                    // Combine image data and location into a JSON-like format
                    String dataToSend = imageBase64 + "|" + latitude + "," + longitude;

                    // Send data to the server
                    pw.write(dataToSend);
                    pw.flush();

                    socket.shutdownOutput();

                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String content = br.readLine();

                    Message msg = new Message();
                    msg.what = UPDATE_UI;
                    msg.obj = content;
                    handler.sendMessage(msg);

                    socket.close();
                    os.close();
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }



    // 主线程创建消息处理器
    Handler handler = new Handler() {
        // 但有新消息时调用
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_UI) {
                // 获取消息对象
                if(msg.obj.equals("0")){
                    replyMsg.setText("*****");
                }else{
                    String content = (String) msg.obj;
                    replyMsg.setText(content);
                }
            } else if (msg.what == ERROR) {
                // Toast也是属于UI的更新
                Toast.makeText(getApplicationContext(), "*****", Toast.LENGTH_LONG).show();
            }else if (msg.what == UPDATE_ok){
                Toast.makeText(getApplicationContext(), "开始检测", Toast.LENGTH_LONG).show();
            }
        }
    };
}

