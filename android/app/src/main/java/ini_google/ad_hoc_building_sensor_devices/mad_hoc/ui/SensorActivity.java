package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import ini_google.ad_hoc_building_sensor_devices.R;

public class SensorActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "SensorActivity";
    private Activity activity = this;
    private String androidID;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference devices;
    private DatabaseReference sensors,actuators;
    private JSONObject deviceConfig;
    private CameraManager mCameraManager;
    private static String device_id;
    private TextView configView,sensorType ,sensorValue,taskView;
    private Button cancelButton;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int lowerThreshold,upperThreshold;
    private String instanceID ;
    private String type;
    private Sensor accelerometer;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private float accThreshold;
    private TextView textView;
    TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        taskView = (TextView) findViewById(R.id.taskView);
        cancelButton = (Button) findViewById(R.id.cancelbutton);
        sensorValue = (TextView) findViewById(R.id.valueView);
        sensorType = (TextView) findViewById(R.id.sensorType);
        configView = (TextView) findViewById(R.id.configuration);
        Bundle bundle = getIntent().getExtras();

        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = null;

        // textView = (TextView) findViewById(R.id.textView);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        try {
            this.deviceConfig = new JSONObject(bundle.get("sensorConfig").toString());

            System.out.println("json:" + deviceConfig);
            this.instanceID = bundle.get("instanceID").toString();
            this.device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            devices = database.getReference("/devices");
            sensors = database.getReference("install_sensors");
            actuators = database.getReference("install_actuators");

            deployDevicewithConfig(this.instanceID,this.deviceConfig);
            Iterator<String> configParameters = this.deviceConfig.keys();
            while (configParameters.hasNext()) {
                String parameter = configParameters.next();
                String parameterType = ((JSONObject) this.deviceConfig.get(parameter)).get("type").toString();
                int role = getResources().getIdentifier(parameterType, "String", getPackageName());
                this.type = parameterType;
                if (parameterType.equals("LIGHT") || parameterType.equals("ACCELEROMETER")) {
                    configureSensors(parameter,parameterType);

                }
                if (parameterType.equals("FLASH") || parameterType.equals("SPEAKER")) {
                    configureActuators(parameter,parameterType);

                }
                if (parameterType.equals("SCREEN")) {
                    configureActuators(parameter,parameterType);
                    //Intent intent = new Intent(activity, QueueDisplayActivity.class);

                    //actuators.child(this.instanceID).child(parameter).child(device_id).setValue(true);
                    //intent.putExtra("actuator_url","install_actuator".concat("/").concat(this.instanceID).concat("/").concat(parameter).concat("/").concat(device_id));
                    //intent.putExtra("instanceID", instanceID);
                    //startActivity(intent);
                }
                if(parameterType.equals("CAMERA")){
                    Intent intent = new Intent(activity, MultiTrackerActivity.class);

                    sensors.child(this.instanceID).child(parameter).child(((JSONObject)this.deviceConfig.get(parameter)).get("queue_number").toString()).child(device_id).setValue(true);
                    intent.putExtra("sensor_url","install_sensors".concat("/").concat(this.instanceID).concat("/").concat(parameter).concat("/").concat(((JSONObject)this.deviceConfig.get(parameter)).get("queue_number").toString()).concat("/").concat(device_id));
                    intent.putExtra("instanceID", instanceID);
                    startActivity(intent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //mSensorManager.unregisterListener();
                Intent intent = new Intent();
                intent.setClass(activity, MainActivity.class);
                startActivity(intent);
            }
        });

    }


    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    private void configureActuators(String parameter, String parameterType){
        actuators = database.getReference("install_actuators").child(instanceID).child(parameter).child(device_id);
        configView.setText(deviceConfig.toString());
        setAppNamefromInstanceID(instanceID);
        if(this.type.equals("FLASH")) {
            actuators.setValue(false);
            actuators.addValueEventListener(new ValueEventListener() {

                public void onDataChange(DataSnapshot dataSnapshot) {
                    String flashON = dataSnapshot.getValue().toString();
                    if (flashON.equals("true")) {
                        turnOnFlashLight();
                    } else {
                        turnOffFlashLight();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println(databaseError.toString());
                }
            });
        }
        if(this.type.equals("SCREEN"))
        {
            Intent intent = new Intent(activity, QueueDisplayActivity.class);

            actuators.setValue(true);
            intent.putExtra("actuator_url","install_actuators".concat("/").concat(this.instanceID).concat("/").concat(parameter).concat("/").concat(device_id));
            intent.putExtra("instanceID", instanceID);
            startActivity(intent);
        }
        if(this.type.equals("SPEAKER")){
            t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(Locale.US);
                    }
                }
            });
            actuators.setValue(false);
            actuators.addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String speakerText = dataSnapshot.getValue().toString();
                    // String speakString = this.desc;
                    speakMessage(speakerText);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println(databaseError.toString());
                }
            });
        }
    }

    private void speakMessage(String speakerText){
        Toast.makeText(activity, "speaker invoked", Toast.LENGTH_SHORT).show();
        t1.speak(speakerText, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void turnOnFlashLight() {
        //here to judge if flash is available
        try{
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
            boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (flashAvailable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mCameraManager.setTorchMode("0",true);
                    sensorValue.setText("Flash On");
                }
                else{
                    sensorValue.setText("Flash not supported in this device");
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
                    sensorValue.setText("Flash Off");
                }
                else{
                    sensorValue.setText("Flash not supported in this device");
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deployDevicewithConfig(String instanceID, JSONObject deviceConfig) {
        Gson gson = new Gson();
        Map<String, Object> configuration = gson.fromJson(deviceConfig.toString(), Map.class );
        configView.setText(deviceConfig.toString());
        devices.child(instanceID).child(androidID).child("config").updateChildren(configuration);
        Toast.makeText(activity, "Configuration Deployed Successfully", Toast.LENGTH_LONG).show();
    }

    private void configureSensors(String parameter, String parameterType){
        try {

            sensors = database.getReference("install_sensors").child(this.instanceID).child(parameter).child(device_id);

            setAppNamefromInstanceID(this.instanceID);
            sensorType.setText(parameterType);

            sensors.setValue(true);
            JSONObject config = (JSONObject)(this.deviceConfig.get(parameter));
            if(parameterType.equals("LIGHT")) {
                mSensor = mSensorManager.getDefaultSensor(5);
                lowerThreshold = Integer.parseInt(config.get("threshold_lower").toString());
                upperThreshold = Integer.parseInt(config.get("threshold_upper").toString());

            }
            if(parameterType.equals("ACCELEROMETER")){
                mSensor = mSensorManager.getDefaultSensor(1);
                accThreshold = Float.parseFloat(config.get("threshold_lower").toString());
            }
            else{
                sensorValue.setText("Sensor not supported");
            }
            if(mSensor==null){
                taskView.setText("Sensor Not Available");
            } else {
                //taskView.setText(sensorConfig.get("application_name").toString());
                System.out.println("Job Is Running");
                int interval = 1000000;
                if (config.has("sampling_rate")) {
                    interval = Integer.parseInt(config.get("sampling_rate").toString()) * 1000000;
                }
                mSensorManager.registerListener(this,mSensor,interval);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setAppNamefromInstanceID(String instanceID) {
        DatabaseReference applicaton = database.getReference("app_ids").child(instanceID);
        applicaton.addListenerForSingleValueEvent( new ValueEventListener() {

            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    JSONObject app_info = new JSONObject((HashMap) dataSnapshot.getValue());
                    String app_id = app_info.get("app_id").toString();
                    DatabaseReference applicationName = database.getReference("apps").child(app_id).child("app_name");
                    applicationName.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try{
                                String app_name = dataSnapshot.getValue().toString();
                                if(!app_name.isEmpty())taskView.setText(app_name);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(this.type.equals("LIGHT")){
            float sensor_value = event.values[0];
            sensorValue.setText(Float.toString(sensor_value));

            if(sensor_value<upperThreshold && sensor_value>lowerThreshold)
            {
                sensors.child("value").setValue(Float.toString(sensor_value));
                sensors.child("last_modified").setValue(Long.toString(new Date().getTime()));
            }
        }
        if(this.type.equals("ACCELEROMETER")){
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if(mAccel > accThreshold) {
                Toast.makeText(activity, "Motion detected!", Toast.LENGTH_SHORT).show();
                sensors.child("value").setValue(true);
                sensors.child("last_modified").setValue(Long.toString(new Date().getTime()));
                sensorValue.setText(Float.toString(mAccel));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

}
