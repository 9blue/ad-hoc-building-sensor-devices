package ini_google.ad_hoc_building_sensor_devices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
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
import org.json.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private static String android_id;
    private Button scanButton;
    private TextView textView, connectedList;
    private EditText urlText;
    private String configURL;
    private static String device_hash;
    private int triggerPoint=0;

    // Firebase instance variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference devices = database.getReference().child("devices");
    private DatabaseReference deviceConfig;
    private DatabaseReference appDataStore;
    private ValueEventListener deviceListListener;
    private JSONObject configuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fetch Android ID after all the components have been created
        android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        textView = (TextView) findViewById(R.id.textView);
        connectedList = (TextView) findViewById(R.id.connectedList);
        scanButton = (Button) findViewById(R.id.scanButton);
        urlText = (EditText) findViewById(R.id.config_url);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = null;
        device_hash = "";


        scanButton.setTransformationMethod(null);
        final Activity activity = this;
        urlText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                configURL = s.toString();
                downloadConfig(configURL);

            }
        });

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

    private void downloadConfig(String firebaseURL){
                            //appDataStore = database.getReferenceFromUrl(firebaseURL);
                //textView.setText(extractUrls(firebaseURL));
                deviceConfig = database.getReferenceFromUrl(extractUrls(firebaseURL));
                deviceConfig.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                        /*
                            TODO: Gobi, dataSnapshot.getValue() returns the config file
                            see what you want to do with it. I comment out your json part.
                            since now the information in json, is in dataSnapshot.
                            */
                            JSONObject config;
                            try{
                                config = new JSONObject((HashMap) dataSnapshot.getValue());
                                setConfiguration(config);
                                configureSensors();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            // ...
                        }
                    });
           }

    private void configureSensors(){
        try {
            textView.setText(getConfiguration().toString());
            appDataStore = database.getReferenceFromUrl(getConfiguration().get("data_store").toString()).child(getConfiguration().get("stream").toString());
            appDataStore.child(android_id).child(Long.toString(new Date().getTime())).setValue("Connected");
            textView.setText("connected");
            mSensor = mSensorManager.getDefaultSensor(Integer.parseInt(getConfiguration().get("sensor_type").toString()));
            triggerPoint = Integer.parseInt(getConfiguration().get("trigger").toString());
            if(mSensor==null){
                        textView.setText("Sensor not available in phone");
                    }
                    else{
                        mSensorManager.registerListener(this,mSensor,Integer.parseInt(getConfiguration().get("interval").toString())*100000);
                    }

        }
        catch (Exception e){
            e.printStackTrace();
        }
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

                configURL = result.getContents();
                textView.setText(configURL);
                //JSONObject configReader = null;

                //deviceConfig = devices.child(device_hash);
                //HashMap<String, Object> id = new HashMap<String, Object>();
                //id.put("device_id", android_id);
                //deviceConfig.updateChildren(id);
//

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
    private void setConfiguration(JSONObject config)
    {
        this.configuration = config;
    }
    private JSONObject getConfiguration()
    {
        return this.configuration;
    }
    private void turnOnFlashLight()
    {

    }
    private void turnOffFlashLight()
    {
        
    }
}
