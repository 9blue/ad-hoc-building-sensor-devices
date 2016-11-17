package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.HashMap;
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
    private static String device_id;
    private TextView statusView, sensorValue,taskView;
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
        statusView = (TextView) findViewById(R.id.statusView);
        //sensorValue = (TextView) findViewById(R.id.sensorValue);
        sensorListButton = (Button) findViewById(R.id.sensorList);
        actuatorButton = (Button) findViewById(R.id.actuatorList);
        cancelButton = (Button) findViewById(R.id.cancelbutton);


        //device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Bundle bundle = getIntent().getExtras();

        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = null;

        try {
            this.deviceConfig = new JSONObject(bundle.get("sensorConfig").toString());

            System.out.println("json:" + deviceConfig);
            this.instanceID = bundle.get("instanceID").toString();

            devices = database.getReference("/devices");
            sensors = database.getReference("install_sensors");
            actuators = database.getReference("install_actuators");

            deployDevicewithConfig(this.instanceID,this.deviceConfig);
            configureSensors();
            //System.out.println("json:" + bundle.get("sensorConfig"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        sensorListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ListActivity.class);
                startActivity(intent);
            }
        });

        actuatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ListActivity.class);
                startActivity(intent);
            }
        });


        /*cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(activity, MainActivity.class);
                startActivity(intent);
            }
        });*/
    }

    private void deployDevicewithConfig(String instanceID, JSONObject deviceConfig) {
        //Map<String,JSONObject> configuration = new HashMap<String, JSONObject>();
        //Map configuration = Gson.fromJson(deviceConfig, Map.class);
       // new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
        //java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Gson gson = new Gson();
        Map<String, Object> configuration = gson.fromJson(deviceConfig.toString(), Map.class );
        //configuration.put(instanceID,deviceConfig);
        devices.child(instanceID).child(androidID).child("config").updateChildren(configuration);
        Toast.makeText(activity, "Configuration Updated Successfully", Toast.LENGTH_LONG).show();
    }

    private void configureSensors(){
//        try {
//
//            appDataStore = database.getReferenceFromUrl(deviceConfig.get("data_store").toString());
//
//            statusView.setText("Status: Connected Now!!");
//
//            if(deviceConfig.get("type").toString().equals("LIGHT")) {
//                mSensor = mSensorManager.getDefaultSensor(5);
//            }
//            else{
//                sensorValue.setText("Sensor not supported");
//            }
//            JSONObject thresholds = new JSONObject(deviceConfig.get("threshold").toString());
//            lowerThreshold = Integer.parseInt(thresholds.get("lower").toString());
//            upperThreshold = Integer.parseInt(thresholds.get("upper").toString());
//
//            if(mSensor==null){
//                taskView.setText("Sensor Not Available");
//            } else{
//                //taskView.setText(sensorConfig.get("application_name").toString());
//                System.out.println("Job Is Running");
//                int interval = Integer.parseInt(deviceConfig.get("sampling_rate").toString())*1000000;
//                mSensorManager.registerListener(this,mSensor,interval);
//            }
//
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
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
        float lux = event.values[0];
        sensorValue.setText(Float.toString(lux));

        if(lux<upperThreshold && lux>lowerThreshold)
        {
//            appDataStore.child(device_id).setValue(Float.toString(lux));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


}
