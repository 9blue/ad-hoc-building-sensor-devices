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
<<<<<<< HEAD
            Parameter parameter = null;
            List<Parameter> parameters = null;

            while(keys.hasNext()) {
                parameters = new ArrayList<Parameter>();
=======
//            Parameter parameter1 = null;
//            Parameter parameter2 = null;
//            Parameter parameter3 = null;
            while(keys.hasNext()) {
                Parameter parameter1 = null;
                Parameter parameter2 = null;
                Parameter parameter3 = null;

>>>>>>> 3ac41219bc868b77c6d382b472e446e7c03df7bb
                String key = (String) keys.next();
                JSONObject configSensor = (JSONObject) config.get(key);
                parameter = null;
                String type = configSensor.get("type").toString();
                listDataHeader.add(key);
<<<<<<< HEAD
=======
                List<Parameter> child = new ArrayList<Parameter>();

//                if(lookupTable.get(type).equals("S")) {
                if (configSensor.has("threshold")) {
                    JSONObject params = (JSONObject) configSensor.get(key);
                    // do we need lookuptable?
                    if (((JSONObject)params.get("threshold")).get("fixed").toString().equals("false")) {
                        if (((JSONObject)params.get("threshold")).has("upper")) {
                            parameter1 = new Parameter("Threshold_upper", ((JSONObject)params.get("threshold")).get("upper").toString(), true);
                        }
                        if (((JSONObject)params.get("threshold")).has("lower")) {
                            parameter2 = new Parameter("Threshold_lower", ((JSONObject)params.get("threshold")).get("lower").toString(), true);
                        }
                    } else {
                        if (((JSONObject)params.get("threshold")).has("upper")) {
                            parameter1 = new Parameter("Threshold_upper", params.get("upper").toString(), false);
                        }
                        if (((JSONObject)params.get("threshold")).has("lower")) {
                            parameter2 = new Parameter("Threshold_lower", params.get("lower").toString(), false);
                        }
                        if (params.has("sampling_rate")) {
                            parameter3 = new Parameter("Threshold_lower", params.get("lower").toString(), false);
                        }

                    }
>>>>>>> 3ac41219bc868b77c6d382b472e446e7c03df7bb

                if (configSensor.has("threshold_upper")) {
                    parameter = null;
                    parameter = new Parameter("Upper Threshold", configSensor.get("threshold_upper").toString(),"Int",configSensor.get("threshold_fixed").toString());
                    parameters.add(parameter);
                }
<<<<<<< HEAD
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



=======

                //List<Parameter> child = new ArrayList<Parameter>();
//                listDataChild.put(key, child);
                if(parameter1 != null)child.add(parameter1);
                if(parameter2 != null)child.add(parameter2);
                if(parameter3 != null)child.add(parameter3);

                if(child.size() > 0) {
                    listDataChild.put(key, child);
                }
            }
            //List<Parameter> child = new ArrayList<Parameter>();

            //if(parameter1 != null)child.add(parameter1);
            //if(parameter2 != null)child.add(parameter2);

            //listDataChild.put(listDataHeader.get(0), child);
>>>>>>> 3ac41219bc868b77c6d382b472e446e7c03df7bb

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
            JSONObject config = new JSONObject(configData);
            for(HashMap.Entry<String, List<Parameter>> entry: listDataChild.entrySet()) {
               String key = entry.getKey();
               List<Parameter> list = listDataChild.get(key);
               JSONObject field = (JSONObject) config.get(key);
               for(Parameter parameter:list) {
                   String item = parameter.getItem();
                   String val = parameter.getVal();
                   boolean editable = parameter.isEditable();

                   if (item.equals("Threshold_upper")) {
                       if (editable &&
                               (!((JSONObject)field.get("threshold")).get("upper").equals(val))) {

                           field.put("upper", val);
                       }
                   } else if(item.equals("Threshold_lower")) {
                       if (editable &&
                               (!((JSONObject)field.get("threshold")).get("lower").equals(val))) {
                           field.put("lower", val);
                       }
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
<<<<<<< HEAD
        String type;
        String fixed;
        Parameter(String item, String val,String type,String fixed) {
            this.item = item;
            this.val = val;
            this.type = type;
            this.fixed = fixed;
=======
        boolean editable;
        Parameter(String item, String val, boolean editable) {
            this.item = item;
            this.val = val;
            this.editable = editable;
>>>>>>> 3ac41219bc868b77c6d382b472e446e7c03df7bb
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
<<<<<<< HEAD
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
=======

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
>>>>>>> 3ac41219bc868b77c6d382b472e446e7c03df7bb
        }
    }
}
