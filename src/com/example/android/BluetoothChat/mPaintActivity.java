/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import com.example.android.BluetoothChat.BluetoothChat.myFingerEven;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class mPaintActivity extends Activity {

	private FrameLayout mFrameLayout;
	private myPaintView mmyPaintView;
	private Button btn_up;
	private Button btn_down;
	private Button btn_left;
	private Button btn_right;
	private Button btn_ok;
	private Button btn_clear;

	private int time = 0;
	private Car car = myPaintView.car;

	private int current_x;
	private int current_y;

	// Debugging
	private static final String TAG = "mMessage";
	private static final boolean D = true;

	// Make current mChatService same as history

	public static BluetoothChatService mChatService;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Layout Views
	private TextView mTitle;

	// Name of the connected device

	public static String mConnectedDeviceName;
	// private String mConnectedDeviceName =
	// getIntent().getStringExtra("device_name");
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter

	public static BluetoothAdapter mBluetoothAdapter;

	// private BluetoothAdapter mBluetoothAdapter =
	// mChatService.GetBluetoothAdapter();
	// Member object for the chat services

	class TouchEven implements mOnFingerListener {

		public void onTouch(myPaintView myPaintView) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		// draw a smart car under control
		setContentView(R.layout.paint);
		mFrameLayout = (FrameLayout) findViewById(R.id.tablet_view);
		mmyPaintView = new myPaintView(this);
		mmyPaintView.init_line();
		mFrameLayout.addView(mmyPaintView);

		btn_up = (Button) findViewById(R.id.btn_up);
		btn_down = (Button)findViewById(R.id.btn_down);
		btn_clear = (Button) findViewById(R.id.btn_clear);

		btn_clear.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				myPaintView.car = new Car(mmyPaintView.getMeasuredWidth() / 2,
						mmyPaintView.getMeasuredHeight() / 2);
				mmyPaintView.draw();
				mmyPaintView.init_line();
				
			}
		});

		btn_up.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
						time++;
						current_x = myPaintView.car.get_x();
						current_y = myPaintView.car.get_y();
						myPaintView.car.foward_x(time * 2);
						myPaintView.car.foward_y(time * 2);
						time = 0;
						mmyPaintView.draw();
				return false;
			}
		});

		
		btn_down.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
					time++;
					current_x = myPaintView.car.get_x();
					current_y = myPaintView.car.get_y();
					myPaintView.car.back_x(time * 2);
					myPaintView.car.back_y(time * 2);
					time = 0;
					mmyPaintView.draw();
				return false;
			}
		});
		
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			mTitle.setText("无连接");
		} else
			mTitle.setText(mConnectedDeviceName);

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();

		}
		
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
						+ readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

}