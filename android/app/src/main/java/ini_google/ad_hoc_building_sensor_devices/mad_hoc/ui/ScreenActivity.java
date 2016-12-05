package ini_google.ad_hoc_building_sensor_devices.mad_hoc.ui;

/**
 * Created by chilli on 11/23/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ini_google.ad_hoc_building_sensor_devices.R;

public class ScreenActivity extends AppCompatActivity {
    private Button cancelButton;
    private TextView screen;
    private Activity activity = this;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference actuator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        screen = (TextView) findViewById(R.id.textView);

        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //mSensorManager.unregisterListener();
                Intent intent = new Intent();
                intent.setClass(activity, MainActivity.class);
                startActivity(intent);
            }
        });
        final Bundle bundle = getIntent().getExtras();

        String actuator_url = bundle.get("actuator_url").toString();
        actuator = database.getReference(actuator_url);
        actuator.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    JSONObject actuator_display = new JSONObject((HashMap) dataSnapshot.getValue());
                    Iterator display_keys = actuator_display.keys();
                    while(display_keys.hasNext()){
                        String display = display_keys.next().toString();
                        if(!display.startsWith("display")){
                            actuator_display.remove(display);
                        }
                    }
                    changeScreenColor(actuator_display);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void changeScreenColor(JSONObject displayData) {
        try {
            Iterator<?> keys = displayData.keys();
            while (keys.hasNext()) {
                String display = keys.next().toString();
                JSONObject display_data = (JSONObject) displayData.get(display);
                String text, value, color;

                if (display_data.has("display_value")) {
                    value = display_data.get("display_value").toString();
                } else {
                    value = "";
                }
                if (display_data.has("display_color")) {
                    color = display_data.get("display_color").toString();
                } else {
                    color = "teal";
                }
                screen.setBackgroundColor(Color.parseColor(color));
                screen.setText(value);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this,"Error occured while fetching data",Toast.LENGTH_LONG).show();;
        }
    }
}
