package ini_google.ad_hoc_building_sensor_devices;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.Date;

public class SensorActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "SensorActivity";
    private Activity activity = this;
    private String androidID;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference devices = database.getReference().child("devices");
    private DatabaseReference deviceConfig;
    private DatabaseReference appDataStore;
    private ValueEventListener deviceListListener;

    private JSONObject sensorConfig;
    private static String device_id;
    private TextView statusView, sensorValue,taskView;
    private Button cancelButton;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int lowerThreshold,upperThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        taskView = (TextView) findViewById(R.id.taskView);
        statusView = (TextView) findViewById(R.id.statusView);
        sensorValue = (TextView) findViewById(R.id.sensorValue);
        cancelButton = (Button) findViewById(R.id.cancelbutton);

        device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Bundle bundle = getIntent().getExtras();
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = null;

        try {
            sensorConfig = new JSONObject((String) bundle.get("sensorConfig"));
            System.out.println("json:" + sensorConfig);
            configureSensors();
            //System.out.println("json:" + bundle.get("sensorConfig"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(activity, MainActivity.class);
                startActivity(intent);
            }
        });*/
    }


    private void configureSensors(){
        try {
            //textView.setText(getConfiguration().toString());
            //String sensor = (String) sensorConfig.get("stream");
            //System.out.println("sensor: " + sensor);
            appDataStore = database.getReferenceFromUrl(sensorConfig.get("data_store").toString());
            //appDataStore.child(androidID).child(Long.toString(new Date().getTime())).setValue("Connected");

            statusView.setText("Status: Connected Now!!");
            //System.out.println("sensortype: " + sensorConfig.get("sensor_type").toString());
            //int sensorType = Integer.parseInt(sensorConfig.get("sensor_type").toString());
            //System.out.println("sensortype: " + (String) sensorConfig.get("sensor_type"));

            if(sensorConfig.get("type").toString().equals("LIGHT")) {
                mSensor = mSensorManager.getDefaultSensor(5);
            }
            else{
                sensorValue.setText("Sensor not supported");
            }
            JSONObject thresholds = new JSONObject(sensorConfig.get("threshold").toString());
            lowerThreshold = Integer.parseInt(thresholds.get("lower").toString());
            upperThreshold = Integer.parseInt(thresholds.get("upper").toString());

            if(mSensor==null){
                taskView.setText("Sensor Not Available");
            } else{
                //taskView.setText(sensorConfig.get("application_name").toString());
                System.out.println("Job Is Running");
                int interval = Integer.parseInt(sensorConfig.get("sampling_rate").toString())*1000000;
                mSensorManager.registerListener(this,mSensor,interval);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
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
            appDataStore.child(device_id).setValue(Float.toString(lux));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }
}
