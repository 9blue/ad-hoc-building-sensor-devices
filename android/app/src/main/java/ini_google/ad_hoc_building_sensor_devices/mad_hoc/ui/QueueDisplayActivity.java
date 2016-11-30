package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ini_google.ad_hoc_building_sensor_devices.R;
import ini_google.ad_hoc_building_sensor_devices.mad_hoc.adapters.QueueListAdapter;

public class QueueDisplayActivity extends AppCompatActivity{
    QueueListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Queue>> listDataChild;
    private Button backButton;
    private Activity activity = this;
    private String configData = null;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        expListView = (ExpandableListView) findViewById(R.id.queueList);
        backButton = (Button) findViewById(R.id.backButton);

        final Bundle bundle = getIntent().getExtras();
        configData =  (String) bundle.get("sensorConfig");

        prepareListData(configData);
        listAdapter = new QueueListAdapter(activity, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void prepareListData(String configData) {
        try {
            System.out.println("json:" + configData);
            JSONObject config = new JSONObject(configData);

            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<Queue>>();

            listDataHeader.add("Queue List");
            listDataChild = new HashMap<String, List<Queue>>();

            Iterator<?> keys = config.keys();
            List<Queue> queues = new ArrayList<Queue>();
//            while(keys.hasNext()) {
//                String key = (String) keys.next();
//                String value = config.get(key).toString();
//                Queue queue = new Queue(key, value);
//
//            }
            // test
            queues.add(new Queue("queue1", "10", Color.BLUE));
            queues.add(new Queue("queue2", "12", Color.YELLOW));
            queues.add(new Queue("queue3", "15", Color.RED));
            listDataChild.put(listDataHeader.get(0), queues);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public class Queue {
        String Qname;
        String Qcount;
        int Qcolor;

        Queue(String name, String count, int color) {
            this.Qname = name;
            this.Qcount = count;
            this.Qcolor = color;
        }

        public String getQname() {
            return Qname;
        }

        public void setQname(String qname) {
            Qname = qname;
        }

        public String getQcount() {
            return Qcount;
        }

        public void setQcount(String qcount) {
            Qcount = qcount;
        }

        public int getQcolor() {
            return Qcolor;
        }

        public void setQcolor(int qcolor) {
            Qcolor = qcolor;
        }
    }
}
