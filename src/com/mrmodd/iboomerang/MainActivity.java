package com.mrmodd.iboomerang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * iBoomerang for Android
 * Copyright (C) 2014  Federico "MrModd" Cosentino (http://mrmodd.it/)
 * 
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
public class MainActivity extends Activity implements SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private float gravity[];
	private float linear_acceleration[];
	private TextView start_message;
	private TextView end_message;
	private TextView tilt_box;
	private ImageView boomerang;
	private float mean[];
	private float variance[];
	private boolean tilt;

	final float alpha = 0.8f;
	final float beta = 0.5f;
	final float gamma = 0.125f;
	final float critical = 0.5f;
	final float tilt_max = 20f;
	final float tilt_min = 0.1f;

	/**
	 * First
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Called just when the application starts for the first time or
		// when was destroyed before (deallocated from ram)
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.i(getClass().getName(), "Activity created");

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		start_message = (TextView) findViewById(R.id.start_textbox);
		end_message = (TextView) findViewById(R.id.end_textbox);
		tilt_box = (TextView) findViewById(R.id.tilt_box);
		boomerang = (ImageView) findViewById(R.id.boomerang_id);
		
		gravity = new float[3];
		linear_acceleration = new float[3];
		mean = new float[3];
		variance = new float[3];
		tilt = false;
	}

	/**
	 * Second
	 */
	@Override
	public void onStart() {
		super.onStart();

		Log.i(getClass().getName(), "Activity started");
		
		Toast.makeText(this, R.string.toast_loading_msg, Toast.LENGTH_LONG).show();
		SystemClock.sleep(1000);
		start_message.setVisibility(View.VISIBLE);
		boomerang.setVisibility(View.VISIBLE);
		
		// If there's no such sensor, close the application
		if (mSensor == null) {
			Log.e(getClass().getName(), "Cannot find accelerometer");

			// Create an alert dialog box
			AlertDialog.Builder message = new AlertDialog.Builder(this);
			message.setMessage(R.string.sensor_error_msg);
			message.setTitle(R.string.sensor_error_title);
			// Create a button and add an action listener for it
			message.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					System.exit(RESULT_CANCELED);
				}
			});
			message.setCancelable(false);
			message.create().show();
		}
	}

	/**
	 * Third
	 */
	@Override
	public void onResume() {
		super.onResume();

		Log.i(getClass().getName(), "Activity resumed");

		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		Log.i(getClass().getName(), "Listener registered");
	}

	/**
	 * Fourth
	 */
	@Override
	public void onPause() {
		super.onPause();

		Log.i(getClass().getName(), "Activity paused");

		mSensorManager.unregisterListener(this);
		Log.i(getClass().getName(), "Listener unregistered");
	}

	/**
	 * Fifth
	 */
	@Override
	public void onStop() {
		super.onStop();

		Log.i(getClass().getName(), "Activity stopped");
	}

	/**
	 * Sixth (a) and return to onStart() after this call
	 */
	@Override
	public void onRestart() {
		super.onStop();

		Log.i(getClass().getName(), "Activity restarted");
	}

	/**
	 * Sixth (b)
	 */
	@Override
	public void onDestroy() {
		super.onStop();

		Log.i(getClass().getName(), "Activity destroyed");
	}

	/**
	 ***** Methods inherited from SensorEventListener
	 */

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// In this example, alpha is calculated as t / (t + dT),
		// where t is the low-pass filter's time-constant and
		// dT is the event delivery rate.

		// Isolate the force of gravity with the low-pass filter.
		for (int i = 0; i < 3; i++) {
			gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i];
		}

		// Remove the gravity contribution with the high-pass filter.
		for (int i = 0; i < 3; i++) {
			linear_acceleration[i] = event.values[i] - gravity[i];
		}

		for (int i = 0; i < 3; i++) {
			mean[i] = (1 - beta) * mean[i] + beta * event.values[i];
		}

		for (int i = 0; i < 3; i++) {
			variance[i] = (1 - gamma) * variance[i] + gamma * (event.values[i] - mean[i]) * (event.values[i] - mean[i]);
		}
		
		float sample_modulo = (float)Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
		float mean_modulo = (float)Math.sqrt(mean[0] * mean[0] + mean[1] * mean[1] + mean[2] * mean[2]);
		float variance_modulo = (float)Math.sqrt(variance[0] * variance[0] + variance[1] * variance[1] + variance[2] * variance[2]);
		
		/*Log.d(getClass().getName(),
				"Acc. x-axis: " + Float.toString(event.values[0])
						+ "; y-axis: " + Float.toString(event.values[1])
						+ "; z-axis: " + Float.toString(event.values[2]));
		Log.d(getClass().getName(),
				"Linear x-axis: " + Float.toString(linear_acceleration[0])
						+ "; y-axis: " + Float.toString(linear_acceleration[1])
						+ "; z-axis: " + Float.toString(linear_acceleration[2]));
		Log.d(getClass().getName(),
				"Avg x-axis: " + Float.toString(mean[0])
						+ "; y-axis: " + Float.toString(mean[1])
						+ "; z-axis: " + Float.toString(mean[2]));*/
		Log.d(getClass().getName(), "Sample mod: " + Float.toString(sample_modulo) + " Mean mod: " + Float.toString(mean_modulo) + " Var mod: " + Float.toString(variance_modulo));

		if (!tilt && mean_modulo < critical) {
			Log.i(getClass().getName(), "Free fall detected");
			
			start_message.setVisibility(View.INVISIBLE);
			end_message.setVisibility(View.VISIBLE);
			
			mSensorManager.unregisterListener(this);
		}
		
		//if (sample_modulo > tilt) {
		if (!tilt && variance_modulo > tilt_max) {
			Log.i(getClass().getName(), "TILT");
			
			/*mSensorManager.unregisterListener(this);
			// Create an alert dialog box
			AlertDialog.Builder message = new AlertDialog.Builder(this);
			message.setMessage(R.string.tilt_string);
			message.setTitle(R.string.tilt_title);
			// Create a button and add an action listener for it
			message.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mSensorManager.registerListener(MainActivity.this, mSensor,
							SensorManager.SENSOR_DELAY_FASTEST);
				}
			});
			message.setCancelable(false);
			message.create().show();*/
			
			start_message.setVisibility(View.INVISIBLE);
			boomerang.setVisibility(View.INVISIBLE);
			tilt_box.setVisibility(View.VISIBLE);
			
			tilt = true;
			
			//Toast.makeText(this, R.string.tilt_string, Toast.LENGTH_LONG).show();
		}
		
		if (tilt && variance_modulo < tilt_min) {
			Log.i(getClass().getName(), "TILT revoked");
			
			start_message.setVisibility(View.VISIBLE);
			boomerang.setVisibility(View.VISIBLE);
			tilt_box.setVisibility(View.INVISIBLE);
			
			tilt = false;
		}
		
	}

}
