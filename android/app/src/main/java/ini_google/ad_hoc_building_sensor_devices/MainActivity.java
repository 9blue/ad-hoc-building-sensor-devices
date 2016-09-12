package ini_google.ad_hoc_building_sensor_devices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings.Secure;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static String android_id;
    private Button fetchButton, submitButton;
    private TextView textView, connectedList;
    private static TextView connectionStatus;
    private DatabaseReference deviceList;
    private ValueEventListener deviceListListener;
    // Firebase instance variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();






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
        deviceList = database.getReference().child("connected_devices");

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
//                new SendPostRequest().execute();

                deviceList.push().setValue(android_id);

                textView.setText("Device ID Submitted");
                submitButton.setEnabled(false);
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
                    for (String id : data.values()){
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
}
