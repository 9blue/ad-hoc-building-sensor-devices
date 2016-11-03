package ini_google.ad_hoc_building_sensor_devices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.common.StringUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
//    private SensorManager mSensorManager;
//    private Sensor mSensor;
    private final Activity activity = this;
    private static String androidID;
    private Button scanButton, startButton;
    private TextView textView, connectedList;
    private EditText urlTextView;
    private String targetURL;
    private static String device_hash;
    private int triggerPoint = 0;

    // Firebase instance variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference devices = database.getReference().child("devices");
    private DatabaseReference deviceConfig;
    private DatabaseReference appDataStore;
    private ValueEventListener deviceListListener;
    private JSONObject configFromFireBase;


    private CameraManager mCameraManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fetch Android ID after all the components have been created
        androidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        textView = (TextView) findViewById(R.id.orTextView);
        //connectedList = (TextView) findViewById(R.id.connectedList);
        urlTextView = (EditText) findViewById(R.id.urlTextView);
        scanButton = (Button) findViewById(R.id.scanButton);
        startButton = (Button) findViewById(R.id.startButton);
        targetURL = "";
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        //scanButton.setTransformationMethod(null);

        /**
         * Start scanning function with zxing library
         */
        urlTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                targetURL = s.toString();
                textView.setText(targetURL);
            }
        });
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Propagate Main Activity to IntentIntegrator
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                intentIntegrator.setPrompt("Scan");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(false);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.initiateScan();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deviceConfig = database.getReferenceFromUrl(extractUrls(targetURL));
                deviceConfig.addListenerForSingleValueEvent( new ValueEventListener() {

                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            configFromFireBase = new JSONObject((HashMap) dataSnapshot.getValue());
                            Iterator<String> configParameters = configFromFireBase.keys();
                            while(configParameters.hasNext()) {
                                String configParameter = configParameters.next();
                                JSONObject config = new JSONObject(configFromFireBase.get(configParameter).toString());

                                if (config.get("type").toString().equals("LIGHT")) {
                                    textView.setText(config.toString());
                                    Intent intent = new Intent(activity, SensorActivity.class);
                                    intent.putExtra("sensorConfig", config.toString());
                                    startActivity(intent);
                                } else if (config.get("type").toString().equals("FLASH")) {
                                    setupActuation(config);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
            }
        });
    }

    // QR code
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (result != null) {
            if (result.getContents() == null) {
                Log.d("Main Activity Scan", "Cancel Scan ====================================");
                Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("Main Activity Scan", "Scanned ====================================");
                //Toast.makeText(this, "Scanned " + result.getContents(), Toast.LENGTH_LONG).show();
                // start google search activity immediately
                System.out.println("result: " + result.getContents());

                targetURL = extractUrls(result.getContents());
                System.out.println("URL: " + targetURL);
                urlTextView.setText(targetURL);

                //searchOnInternet(result.getContents());
            }
        } else {
            //empty
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    /**
     * Fetch URL
     */
    private static String extractUrls(String text) {
        String containedUrls = null;
        String urlRegex = "((https?|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls = text.substring(urlMatcher.start(0),
                    urlMatcher.end(0));
            return containedUrls;
        }

        return "";
    }
    private void setupActuation(JSONObject config){
        try {
            appDataStore = database.getReferenceFromUrl(config.get("data_store").toString());
            appDataStore.addValueEventListener(new ValueEventListener() {

                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean flashON = (Boolean) dataSnapshot.getValue();
                    if(flashON){
                        turnOnFlashLight();
                    }
                    else {
                        turnOffFlashLight();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void turnOnFlashLight() {
        //here to judge if flash is available
        try{
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
            boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (flashAvailable) {
                mCameraManager.setTorchMode("0",true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void turnOffFlashLight() {
        try{
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
            boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (flashAvailable) {
                mCameraManager.setTorchMode("0",false);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}