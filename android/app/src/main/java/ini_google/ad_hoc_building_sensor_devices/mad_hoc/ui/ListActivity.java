package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ini_google.ad_hoc_building_sensor_devices.R;
import ini_google.ad_hoc_building_sensor_devices.mad_hoc.adapters.SensorListAdapter;

public class ListActivity extends AppCompatActivity implements SensorEventListener{
    private Activity activity = this;
    SensorListAdapter listAdapter;
    ExpandableListView expListView;
    String DataHeader;
    HashMap<String, List<Parameter>> listDataChild;
    HashMap<String, HashMap<String, List<Parameter>>> record;
    private TextView AppName;
    private String targetSensor = null;
    private Button deployButton, calibrateButton,backButton;
    private Spinner spinner;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private HashMap<String, String> lookupTable;
    private String configData,deviceConfig;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private int calibrationCount;
    private float minValue,maxValue,avgValue;
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        AppName = (TextView) findViewById(R.id.appView);
        deployButton = (Button) findViewById(R.id.deployButton);
        calibrateButton = (Button) findViewById(R.id.calibrateButton);
        spinner = (Spinner)findViewById(R.id.spinner);
        lookupTable = new HashMap<String, String>();
        record = new HashMap<String, HashMap<String, List<Parameter>>>();
        deviceConfig = "";
        lookupTableInit(lookupTable);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = null;calibrationCount = 0;minValue =0 ;maxValue=0;avgValue=0;

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        final Bundle bundle = getIntent().getExtras();
        configData = bundle.get("sensorConfig").toString();

        // get the listview, (ViewHolder)
        expListView = (ExpandableListView) findViewById(R.id.sensorList);

        setAppNamefromInstanceID(bundle.get("instanceID").toString());
        // set up spinner
        ArrayList<String> listItem = new ArrayList<>();
        getHeader(listItem, configData);
        String[] items = listItem.toArray(new String[listItem.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView adapterView, View view, int position, long id){
                targetSensor = adapterView.getSelectedItem().toString();
                Toast.makeText(activity, adapterView.getSelectedItem().toString() + " is chosen", Toast.LENGTH_LONG).show();
                // update listAdapter

                if (!record.containsKey(targetSensor)) {
                    prepareListData(configData);
                } else {
                    listDataChild = record.get(targetSensor);
                }
                listAdapter = new SensorListAdapter(activity, targetSensor, listDataChild);
                expListView.setAdapter(listAdapter);
            }
            public void onNothingSelected(AdapterView arg0) {
                Toast.makeText(activity, "No sensor is selected", Toast.LENGTH_LONG).show();
            }
        });
        progressBar.setVisibility(View.INVISIBLE);

        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //use firebase code to update json
                progressBar.setVisibility(View.VISIBLE);

                startCalibration();
            }
        });

        deployButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listAdapter.updateList(listDataChild);
                listAdapter.notifyDataSetChanged();

                //Intent intent;
                //Intent intent_tracker = new Intent(activity, MultiTrackerActivity.class);

                if(!deviceConfig.isEmpty()){
                   Intent intent = null;
                   if(!targetSensor.isEmpty()) {
                       intent = new Intent(activity, SensorActivity.class);
                       intent.putExtra("sensorConfig", deviceConfig);
                       intent.putExtra("instanceID", bundle.get("instanceID").toString());
                   }
                    startActivity(intent);
                } else {
                    Toast.makeText(activity, "device configuration is empty", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void startCalibration(){
        if(targetSensor.equals("light")){
            mSensor = mSensorManager.getDefaultSensor(5);
            mSensorManager.registerListener((SensorEventListener) this,mSensor,100000);

        }
        else
        {
            setCalibraation();
        }
        mSensor = null;
    }

    public void onSensorChanged(SensorEvent event) {
        if (targetSensor.equals("light")) {
            float sensor_value = event.values[0];
            if(calibrationCount == 0)
            {
                minValue = sensor_value;
                maxValue = sensor_value;
                avgValue = sensor_value;
            }
                calibrationCount++;
            if(sensor_value<minValue) minValue = sensor_value;
            if(sensor_value>maxValue) maxValue = sensor_value;
            avgValue+=sensor_value;

        }
        if(calibrationCount == 49)
        {
            calibrationCount =0;
            avgValue = avgValue/50;
            mSensorManager.unregisterListener(this);
            setCalibraation();
        }
    }
    public void setCalibraation(){

        if(targetSensor.equals("light")){
            try {
                JSONObject lightconfig = (new JSONObject(configData)).getJSONObject("light");
                lightconfig.put("threshold_upper",Math.round(maxValue));
                lightconfig.put("threshold_lower",Math.round(minValue));
                configData = ((new JSONObject(configData)).put("light",lightconfig)).toString();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prepareListData(configData);
        listAdapter.updateVal();
        UpdateJson(configData);
        progressBar.setVisibility(View.INVISIBLE);
        //progressBar.setVisibility(View.GONE);
        Toast.makeText(activity, "Sensos Calibrated Successfully", Toast.LENGTH_LONG).show();
        record.put(targetSensor, listDataChild);
        listAdapter = new SensorListAdapter(activity, targetSensor, listDataChild);
        expListView.setAdapter(listAdapter);

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void getHeader(ArrayList<String> listItem, String configData) {
        try {
            JSONObject config = new JSONObject(configData);
            Iterator<?> keys = config.keys();
            while(keys.hasNext()) {
                listItem.add((String) keys.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareListData(String configData) {
        try {
            System.out.println("json:" + configData);
            JSONObject config = new JSONObject(configData);


            //listDataHeader = new ArrayList<String>();
            //if (listDataChild == null) {
                listDataChild = new HashMap<String, List<Parameter>>();

                Iterator<?> keys = config.keys();
                Parameter parameter = null;
                List<Parameter> parameters = null;

//            while(keys.hasNext()) {
                parameters = new ArrayList<Parameter>();
//                String key = (String) keys.next();
                String key = targetSensor;
                JSONObject configSensor = (JSONObject) config.get(key);
                parameter = null;
                String type = configSensor.get("type").toString();

                //listDataHeader.add(key);

                if (key.equals("light") && configSensor.has("threshold_upper")) {
                    //parameter = null;
                    parameter = new Parameter("threshold_upper", configSensor.get("threshold_upper").toString(), "Int");
                    parameters.add(parameter);
                }
                if ((key.equals("light") || key.equals("accelerometer")) && configSensor.has("threshold_lower")) {
                    //parameter = null;
                    parameter = new Parameter("threshold_lower", configSensor.get("threshold_lower").toString(), "Int");
                    parameters.add(parameter);
                }
                if (key.equals("light") && configSensor.has("sampling_rate")) {
                    //parameter = null;
                    parameter = new Parameter("sampling_rate", configSensor.get("sampling_rate").toString(), "Int");
                    parameters.add(parameter);
                }
                if (key.equals("camera")) {
                    parameter = new Parameter("queue_number", "0", "Int");
                    parameters.add(parameter);
                }
                listDataChild.put(targetSensor, parameters);
//            }
//            }

            //System.out.println("Header: " + listDataHeader);

        } catch (Exception e) {
            Toast.makeText(this, "Configuration Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    // build the hashmap for sensors or actuators
    private void lookupTableInit(Map<String, String> lookupTable) {
        // smart light
        lookupTable.put("FLASH", "A");
        lookupTable.put("LIGHT", "S");

    }

    // update Json after threshold values are changed by users
    private void UpdateJson(String configData) {
        try {
            JSONObject default_config = new JSONObject(configData);
               List<Parameter> list = listDataChild.get(targetSensor);
                JSONObject chosenConfig  = (JSONObject) default_config.get(targetSensor);
               for(Parameter parameter:list) {
                   String item = parameter.getItem();
                   String val = parameter.getVal();
                   //boolean fixed = parameter.getFixed();
                   chosenConfig.put(item, val);
               }

            default_config = new JSONObject();
            default_config.put(targetSensor,chosenConfig);
            deviceConfig = default_config.toString();

           } catch (JSONException e) {
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
                                if(!app_name.isEmpty())AppName.setText(app_name);
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
            // define new class for parameters
    public class Parameter {
        String item;
        String val;
        String type;

        Parameter(String item, String val, String type) {
            this.item = item;
            this.val = val;
            this.type = type;
        }

        public String getItem() {
            return this.item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getVal() {
            return this.val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String Type) {
            this.type = type;
        }

//        public Boolean getFixed() {
//            if (this.fixed.equals("true")) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        public void setFixed(String Type) {
//            this.fixed = fixed;
//        }
    }



}
