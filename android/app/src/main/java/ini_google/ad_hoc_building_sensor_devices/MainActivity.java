package ini_google.ad_hoc_building_sensor_devices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.*;

import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private static String android_id;
    private Button fetchButton, submitButton, scanButton;
    private TextView textView, connectedList;
    private static TextView connectionStatus;
    private static String config;
    private int triggerPoint=0;

    // Firebase instance variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference deviceList = database.getReference().child("connected_devices");
    private DatabaseReference appDataStore;
    private ValueEventListener deviceListListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fetch Android ID after all the components have been created
        android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        textView = (TextView) findViewById(R.id.textView);
        connectedList = (TextView) findViewById(R.id.connectedList);
        connectionStatus = (TextView) findViewById(R.id.connectStatus);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        submitButton = (Button) findViewById(R.id.submitButton);
        scanButton = (Button) findViewById(R.id.scanButton);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = null;

        deviceList.child(android_id).onDisconnect().removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference firebase) {
                if (error != null) {
                    System.out.println("could not establish onDisconnect event:" + error.getMessage());
                }
            }
        });

        config = "";

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Andorid_id " + android_id);
                textView.setText(android_id);
            }});

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Andorid_id " + android_id);
                textView.setText("Device ID Submitting");

                deviceList.child(android_id).setValue(android_id);
                textView.setText("Device ID Submitted");
                submitButton.setEnabled(false);
            }

        });

        scanButton.setTransformationMethod(null);
        final Activity activity = this;

        /**
         * Start scanning function with zxing library
         */
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
    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Object obj = dataSnapshot.getValue();
                if (obj != null) {
                    HashMap<String, String> data = (HashMap) obj;
                    StringBuilder sbStr = new StringBuilder();
                    for (String id : data.keySet()){
                        sbStr.append(id);
                        sbStr.append("\n");
                    }
                    connectedList.setText("connected device changed: \n" + sbStr.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        };
        deviceList.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        deviceListListener = postListener;
    }

    //QR code
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (result != null) {
            if (result.getContents() == null) {
                Log.d("Main Activity Scan", "Cancel Scan ====================================");
                Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("Main Activity Scan", "Scanned ====================================");
                Toast.makeText(this, "Scanned " + result.getContents(), Toast.LENGTH_LONG).show();
                // start google search activity immediately
                //System.out.println("result: " + result.getContents());

                config = result.getContents();
                textView = (TextView) findViewById(R.id.textView);
                textView.setText(config);
                JSONObject configReader = null;
                try {
                    configReader = new JSONObject(config);
                    appDataStore = database.getReferenceFromUrl(configReader.get("firebase_url").toString()).child((String) configReader.get("stream"));
                    appDataStore.child(android_id).child(Long.toString(new Date().getTime())).setValue("Connected");
                    mSensor = mSensorManager.getDefaultSensor(Integer.parseInt(configReader.get("sensor_type").toString()));
                    triggerPoint = Integer.parseInt(configReader.get("trigger").toString());
                    if(mSensor==null){
                        textView.setText("Sensor not available in phone");
                    }
                    else{
                        mSensorManager.registerListener(this,mSensor,Integer.parseInt(configReader.get("interval").toString())*100000);
                    }
                }
                catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());}
                catch(Exception e){
                    Log.e(TAG,e.getMessage());
                }



                //System.out.println("URL: " + targetURL);
                
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
    private static String extractUrls(String text)
    {
        String containedUrls = null;
        String urlRegex = "((https?|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls = text.substring(urlMatcher.start(0),
                    urlMatcher.end(0));
            return containedUrls;
        }

        return "";
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float lux = event.values[0];
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setTextSize(40);
        textView.setText(Float.toString(lux));
        if(lux>triggerPoint)
        {
            appDataStore.child(android_id).child(Long.toString(new Date().getTime())).setValue(Float.toString(lux));
        }
//
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
