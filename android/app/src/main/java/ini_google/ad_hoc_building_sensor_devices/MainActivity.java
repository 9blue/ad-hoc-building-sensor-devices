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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static String android_id;
    private Button fetchButton, submitButton;
    private TextView textView, listView;
    private static TextView connectionStatus;

    // Firebase instance variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRaf = database.getReference();












    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fetch Android ID after all the components have been created
        android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        textView = (TextView) findViewById(R.id.textView);
        connectionStatus = (TextView) findViewById(R.id.connectStatus);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        submitButton = (Button) findViewById(R.id.submitButton);

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

                myRaf.push().setValue(android_id);

                textView.setText("Device ID Submitted");
                submitButton.setEnabled(false);
            }

        });
    }

//    private void retrieveDeviceList() {
////        myRaf.
//    }
}
