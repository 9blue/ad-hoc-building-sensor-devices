package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ini_google.ad_hoc_building_sensor_devices.R;
import ini_google.ad_hoc_building_sensor_devices.mad_hoc.adapters.SensorListAdapter;

public class ListActivity extends AppCompatActivity {
    private Activity activity = this;
    SensorListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Parameter>> listDataChild;
    private TextView typeText;
    private String targetSensor = null;
    private Button deployButton, confirmButton;
    private Spinner spinner;
    private HashMap<String, String> lookupTable;
    private String configData,deviceConfig;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        typeText = (TextView) findViewById(R.id.typeView);
        deployButton = (Button) findViewById(R.id.deployButton);
        confirmButton = (Button) findViewById(R.id.confirmButton);
        spinner = (Spinner)findViewById(R.id.spinner);
        lookupTable = new HashMap<String, String>();
        deviceConfig = "";
        lookupTableInit(lookupTable);

        final Bundle bundle = getIntent().getExtras();
        configData =  (String) bundle.get("sensorConfig");

        // get the listview, (ViewHolder)
        expListView = (ExpandableListView) findViewById(R.id.sensorList);

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
                prepareListData(configData);
                listAdapter = new SensorListAdapter(activity, listDataHeader, listDataChild);
                expListView.setAdapter(listAdapter);
            }
            public void onNothingSelected(AdapterView arg0) {
                Toast.makeText(activity, "No sensor is selected", Toast.LENGTH_LONG).show();
            }
        });

        //dropdown.setSelection(((ArrayAdapter<String>)dropdown.getAdapter()).getPosition(items));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //use firebase code to update json
                listAdapter.updateVal();
                UpdateJson(configData);
                Toast.makeText(activity, "Value Updated Successfully", Toast.LENGTH_LONG).show();
            }
        });

        deployButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listAdapter.updateList(listDataHeader, listDataChild);
                listAdapter.notifyDataSetChanged();

                Intent intent = new Intent(activity, SensorActivity.class);
                if(!deviceConfig.isEmpty())intent.putExtra("sensorConfig",deviceConfig );

                else Toast.makeText(activity, "device configuration is empty", Toast.LENGTH_LONG).show();
                intent.putExtra("instanceID",bundle.get("instanceID").toString());
                // set data back to firebase
                startActivity(intent);
            }
        });

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
            //System.out.println("json:" + configData);
            JSONObject config = new JSONObject(configData);

            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<Parameter>>();

            Iterator<?> keys = config.keys();
            Parameter parameter = null;
            List<Parameter> parameters = null;

//            while(keys.hasNext()) {
                parameters = new ArrayList<Parameter>();
                //String key = (String) keys.next();
                String key = targetSensor;
                JSONObject configSensor = (JSONObject) config.get(key);
                parameter = null;
                String type = configSensor.get("type").toString();

                listDataHeader.add(key);

                if (configSensor.has("threshold_upper")) {
                    parameter = null;
                    parameter = new Parameter("threshold_upper", configSensor.get("threshold_upper").toString(),"Int",configSensor.get("threshold_fixed").toString());
                    parameters.add(parameter);
                }
                if(configSensor.has("threshold_lower")) {
                    parameter = null;
                    parameter = new Parameter("threshold_lower", configSensor.get("threshold_lower").toString(),"Int",configSensor.get("threshold_fixed").toString());
                    parameters.add(parameter);
                }
                if(configSensor.has("sampling_rate")) {
                    parameter = null;
                    parameter = new Parameter("sampling_rate", configSensor.get("sampling_rate").toString(),"Int",configSensor.get("threshold_fixed").toString());
                    parameters.add(parameter);
                }
                listDataChild.put(listDataHeader.get(listDataHeader.indexOf(key)), parameters);
//            }



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
                   boolean fixed = parameter.getFixed();
                   chosenConfig.put(item, val);
               }
            default_config = new JSONObject();
            default_config.put(targetSensor,chosenConfig);
            deviceConfig = default_config.toString();

           } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // define new class for parameters
    public class Parameter {
        String item;
        String val;
        String type;
        String fixed;

        Parameter(String item, String val, String type, String fixed) {
            this.item = item;
            this.val = val;
            this.type = type;
            this.fixed = fixed;
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

        public Boolean getFixed() {
            if (this.fixed.equals("true")) {
                return true;
            } else {
                return false;
            }
        }

        public void setFixed(String Type) {
            this.fixed = fixed;
        }
    }
}
