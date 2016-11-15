package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ini_google.ad_hoc_building_sensor_devices.R;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
//    private static final String firebabeURL = "https://ad-hoc-building-sensor-devices.firbaseio.com/";
//    private SensorManager mSensorManager;
//    private Sensor mSensor;
    private final Activity activity = this;
    private static String androidID;
    private Button scanButton, startButton;
    private TextView textView, connectedList;
    private EditText urlTextView;
    private String instanceID;
    private static String device_hash;
    private int triggerPoint = 0;

    // Firebase instance variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    //private DatabaseReference devices = database.getReference().child("devices");
    private DatabaseReference deviceConfig;
    private DatabaseReference applicationInfo;
    private DatabaseReference actuationLocation;
    private JSONObject configFromFireBase;
    private CameraManager mCameraManager;
    private String configData;


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
        instanceID = "";
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        configData = "";

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
                instanceID = s.toString();
                textView.setText(instanceID);
                if(!instanceID.isEmpty()){
                    downloadDefaultConfig();
                }
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
                Intent intent = new Intent(activity, ListActivity.class);
                intent.putExtra("configData", configData);
                startActivity(intent);

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

                instanceID = result.getContents();
                System.out.println("URL: " + instanceID);
                urlTextView.setText(instanceID);
                if(!instanceID.isEmpty()) {
                    downloadDefaultConfig();
                }
                //searchOnInternet(result.getContents());
            }
        } else {
            //empty
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void downloadDefaultConfig() {
        //reference for the application
        applicationInfo = database.getReference("/app_ids");
        //fetch reference of Instance
        DatabaseReference instanceInfo = applicationInfo.child(instanceID);
        instanceInfo.addListenerForSingleValueEvent( new ValueEventListener() {
            public void onDataChange (DataSnapshot dataSnapshot){
                try {
                    //fetch application Id from firebase
                    String appID =  dataSnapshot.getValue().toString();
                    applicationInfo = database.getReference("/apps/".concat(appID));
                    deviceConfig = applicationInfo.child("default_config");
                    deviceConfig.addListenerForSingleValueEvent( new ValueEventListener() {

                        public void onDataChange (DataSnapshot dataSnapshot){
                            try {
                                //default config for the app from firebase
                                configFromFireBase = new JSONObject((HashMap) dataSnapshot.getValue());
                                Intent intent = new Intent(activity, ListActivity.class);
                                intent.putExtra("sensorConfig", configFromFireBase.toString());
                                startActivity(intent);

//                                Iterator<String> configParameters = configFromFireBase.keys();
//                                while (configParameters.hasNext()) {
//                                    String configParameter = configParameters.next();
//                                    JSONObject default_config = new JSONObject(configFromFireBase.get(configParameter).toString());
//                                    configData = default_config.toString();
//                                    System.out.println("configData: " + configData);
////                        if (default_config.get("type").toString().equals("LIGHT")) {
//                            textView.setText(default_config.toString());
//                            Intent intent = new Intent(activity, SensorActivity.class);
//                            intent.putExtra("sensorConfig", default_config.toString());
//                            startActivity(intent);
//                        } else if (default_config.get("type").toString().equals("FLASH")) {
//                            setupActuation(default_config);
//                        }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });
                    }
                catch (Exception e) {
                    e.printStackTrace();
                }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    /**
     * Fetch URL
     */

    private void setupActuation(JSONObject config){
        try {
            actuationLocation = database.getReferenceFromUrl(config.get("data_store").toString()).child(androidID);
            actuationLocation.addValueEventListener(new ValueEventListener() {

                public void onDataChange(DataSnapshot dataSnapshot) {
                    String flashON = dataSnapshot.getValue().toString();
                    if(flashON.equals("true")){
                        turnOnFlashLight();
                    }
                    else {
                        turnOffFlashLight();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                        System.out.println(databaseError.toString());
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mCameraManager.setTorchMode("0",true);
                }

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mCameraManager.setTorchMode("0",false);
                    textView.setText("Flash Off");
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}