package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private Button backButton, confirmButton;
    private HashMap<String, String> lookupTable;
    private String configData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        typeText = (TextView) findViewById(R.id.typeView);
        backButton = (Button) findViewById(R.id.backButton);
        confirmButton = (Button) findViewById(R.id.confirmButton);
        lookupTable = new HashMap<String, String>();
        lookupTableInit(lookupTable);

        Bundle bundle = getIntent().getExtras();
        configData =  (String) bundle.get("sensorConfig");

        // get the listview, (ViewHolder)
        expListView = (ExpandableListView) findViewById(R.id.sensorList);

        // preparing list data
        // should prepare data first and then initialize an adapter
        prepareListData(configData);
        listAdapter = new SensorListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter, (connect ViewHolder and View)
        expListView.setAdapter(listAdapter);

        // update the list
        //listAdapter.updateData(listDataHeader, listDataChild);
        //listAdapter.notifyDataSetChanged();


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //use firebase code to update json
                listAdapter.updateVal();
                UpdateJson(configData);
                Toast.makeText(activity, "Value Updated Successfully", Toast.LENGTH_LONG).show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listAdapter.updateList(listDataHeader, listDataChild);
                listAdapter.notifyDataSetChanged();

                Intent intent = new Intent(activity, SensorActivity.class);
                intent.putExtra("sensorConfig", configData);
                // set data back to firebase
                startActivity(intent);
            }
        });

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

            while(keys.hasNext()) {
                parameters = new ArrayList<Parameter>();
                String key = (String) keys.next();
                JSONObject configSensor = (JSONObject) config.get(key);
                parameter = null;
                String type = configSensor.get("type").toString();
                listDataHeader.add(key);

                if (configSensor.has("threshold_upper")) {
                    parameter = null;
                    parameter = new Parameter("Upper Threshold", configSensor.get("threshold_upper").toString(),"Int",configSensor.get("threshold_fixed").toString());
                    parameters.add(parameter);
                }
                if(configSensor.has("threshold_lower")) {
                    parameter = null;
                    parameter = new Parameter("Lower Threshold", configSensor.get("threshold_lower").toString(),"Int",configSensor.get("threshold_fixed").toString());
                    parameters.add(parameter);
                }
                if(configSensor.has("sampling_rate")) {
                    parameter = null;
                    parameter = new Parameter("Sampling Rate", configSensor.get("sampling_rate").toString(),"Int",configSensor.get("threshold_fixed").toString());
                    parameters.add(parameter);
                }
                listDataChild.put(listDataHeader.get(listDataHeader.indexOf(key)), parameters);
            }




        } catch (Exception e) {
            Toast.makeText(this, "Configuration Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    // build the hashmap for sensors or actuators
    private void lookupTableInit(Map<String, String> lookupTable) {
        // smart light
        lookupTable.put("flash", "A");
        lookupTable.put("light", "S");

    }

    // update Json after threshold values are changed by users
    private void UpdateJson(String configData) {
        try {
            JSONObject config = new JSONObject(configData);
            for(HashMap.Entry<String, List<Parameter>> entry: listDataChild.entrySet()) {
               String key = entry.getKey();
               List<Parameter> list = listDataChild.get(key);
               JSONObject field = (JSONObject) config.get(key);
               for(Parameter parameter:list) {
                   String item = parameter.item;
                   String val = parameter.val;
                   if(!field.get(item).equals(val)) {
                       field.put(item, val);
                   }
               }

           }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    // define new class for parameters
    public class Parameter{
        String item;
        String val;
        String type;
        String fixed;
        Parameter(String item, String val,String type,String fixed) {
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
            if (this.fixed.equals("true")){
                return true;
            }
            else {
                return false;
            }
        }

        public void setFixed(String Type) {
            this.type = type;
        }
    }
}
