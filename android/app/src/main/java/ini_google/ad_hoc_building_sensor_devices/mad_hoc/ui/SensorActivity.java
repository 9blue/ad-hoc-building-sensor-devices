package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ini_google.ad_hoc_building_sensor_devices.R;

public class SensorActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "SensorActivity";
    private Activity activity = this;
    private String androidID;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference devices;
    private DatabaseReference deviceConfigRef;
    private DatabaseReference sensors,actuators;
    private ValueEventListener deviceListListener;

    private JSONObject deviceConfig;
    private CameraManager mCameraManager;
    private static String device_id;
    private TextView configView,sensorType ,sensorValue,taskView;
    private Button sensorListButton, actuatorButton, cancelButton;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int lowerThreshold,upperThreshold;
    private String instanceID ;

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
                if (parameterType.equals("LIGHT")) {
                    configureSensors(parameter,parameterType);
                } else if (parameterType.equals("FLASH")) {
                    configureActuators(parameter,parameterType);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//        sensorListButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(activity, ListActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        actuatorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(activity, ListActivity.class);
//                startActivity(intent);
//            }
//        });


        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(activity, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void configureActuators(String parameter, String parameterType){
        actuators = database.getReference("install_actuators").child(instanceID).child(parameter).child(device_id);
        configView.setText(deviceConfig.toString());
        setAppNamefromInstanceID(instanceID);
        actuators.setValue(false);
        actuators.addValueEventListener(new ValueEventListener() {

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

            if(parameterType.equals("LIGHT")) {
                mSensor = mSensorManager.getDefaultSensor(5);
            }
            else{
                sensorValue.setText("Sensor not supported");
            }
            JSONObject config = (JSONObject)(this.deviceConfig.get(parameter));
            lowerThreshold = Integer.parseInt(config.get("threshold_lower").toString());
            upperThreshold = Integer.parseInt(config.get("threshold_upper").toString());

            if(mSensor==null){
                taskView.setText("Sensor Not Available");
            } else{
                //taskView.setText(sensorConfig.get("application_name").toString());
                System.out.println("Job Is Running");
                int interval =  Integer.parseInt(config.get("sampling_rate").toString())*1000000;
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
                   String app_id = dataSnapshot.getValue().toString();
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

//    public void onSensorChanged(SensorEvent event) {
//        float lux = event.values[0];
//        TextView textView = (TextView)findViewById(R.id.textView);
//        textView.setTextSize(40);
//        textView.setText(Float.toString(lux));
//        if(lux>triggerPoint)
//        {
//            appDataStore.child(androidID).child(Long.toString(new Date().getTime())).setValue(Float.toString(lux));
//        }
//    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float sensor_value = event.values[0];
        sensorValue.setText(Float.toString(sensor_value));

        if(sensor_value<upperThreshold && sensor_value>lowerThreshold)
        {
            sensors.child("value").setValue(Float.toString(sensor_value));
            sensors.child("last_modified").setValue(Long.toString(new Date().getTime()));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


}
