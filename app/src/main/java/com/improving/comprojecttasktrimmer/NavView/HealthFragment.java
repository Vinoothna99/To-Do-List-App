package com.improving.comprojecttasktrimmer.NavView;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.improving.comprojecttasktrimmer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HealthFragment extends Fragment implements SensorEventListener {

    TextView stepsCount;
    SensorManager sensorManager;

    boolean running=false;

    public HealthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myFragmentView = inflater.inflate(R.layout.fragment_health, container, false);

        stepsCount=(TextView)myFragmentView.findViewById(R.id.stepsCount);
        sensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);

        return myFragmentView;
    }


    @Override
    public void onResume(){
        super.onResume();
        running = true;

        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);

        } else{
            Toast.makeText(getContext(), "Sensor not Found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        running = false;
    }
    private long steps = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(running) {
            String steps = String.valueOf
                    (event.values
                            [0]);
            stepsCount.setText(steps);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
