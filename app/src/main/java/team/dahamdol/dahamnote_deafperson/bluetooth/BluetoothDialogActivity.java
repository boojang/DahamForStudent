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

package team.dahamdol.dahamnote_deafperson.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Set;

import team.dahamdol.dahamnote_deafperson.R;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class BluetoothDialogActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    private static final int NOT_SELECTED = 0;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "deviceAddress";
    public static String EXTRA_STUDENT_NAME = "studentName";


    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private ArrayList<DeviceData> pairedDeviceArrayList;
    private ArrayList<DeviceData> newDeviceArrayList;
/*    private DeviceAdapter pairedDeviceAdapter;
    private DeviceAdapter newDeviceAdapter;*/
    private RecyclerView rvPairedDevices;
    private RecyclerView rvNewDevices;
    private LinearLayoutManager linearLayoutManagerForPaired;
    private LinearLayoutManager linearLayoutManagerForNew;

    private SpinnerAdapter spinnerAdapter;
    private Spinner spBluetoothDevice;
    private int selectedDevicePosition = NOT_SELECTED;
    private Button btnConfirm;
    private Button btnCancel;

    // 이름 기억 관련
    EditText edtStudentName;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_bluetooth);
        btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        edtStudentName = (EditText) findViewById(R.id.edt_dialog_bluetooth_name);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        btnSetting();
        initPairedDevices();
        setPairedDeviceNames();

        // for remember lastly registered name
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        setEdtStudentName();
    }

    private void btnSetting() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDevicePosition != NOT_SELECTED) {
                    connectBluetoothDevice(true, selectedDevicePosition);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.hint_device_connect, Toast.LENGTH_LONG).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onResume() {
        super.onResume();
        setDialogSizeFromSystem();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setDialogSizeFromSystem() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        int width = (int) (point.x * 0.9);  //Display 사이즈의 90%
        getWindow().getAttributes().width = width;
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPairedDevices() {
/*        rvPairedDevices = (RecyclerView) findViewById(R.id.rv_paired_devices);
        linearLayoutManagerForPaired = new LinearLayoutManager(this);
        rvPairedDevices.setLayoutManager(linearLayoutManagerForPaired);

        pairedDeviceArrayList = new ArrayList<>();
        pairedDeviceAdapter = new DeviceAdapter(pairedDeviceArrayList);

        rvPairedDevices.setAdapter(pairedDeviceAdapter);

        pairedDeviceAdapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                connectBluetoothDevice(true, position);
            }
        });*/

        pairedDeviceArrayList = new ArrayList<>();
        pairedDeviceArrayList.add(new DeviceData(getString(R.string.dialog_bluetooth_device_hint), ""));

        spBluetoothDevice = findViewById(R.id.sp_dialog_bluetooth_device);
        spinnerAdapter = new SpinnerAdapter(getApplicationContext(), pairedDeviceArrayList);
        spBluetoothDevice.setAdapter(spinnerAdapter);

        spBluetoothDevice.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) { // 이 조건을 걸어주지 않으면 여러번 호출됨
                if (pairedDeviceArrayList.size() == 1) { // 장치선택 아이템 밖에 없는 경
                    Toast.makeText(BluetoothDialogActivity.this.getApplicationContext(), R.string.none_paired, Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        });

        spBluetoothDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDevicePosition = position;
                //connectBluetoothDevice(true, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

/*    private void initNewDevices(){
        rvNewDevices = (RecyclerView) findViewById(R.id.rv_new_devices);
        linearLayoutManagerForNew = new LinearLayoutManager(this);
        rvNewDevices.setLayoutManager(linearLayoutManagerForNew);

        newDeviceArrayList = new ArrayList<>();
        newDeviceAdapter = new DeviceAdapter(newDeviceArrayList);

        rvNewDevices.setAdapter(newDeviceAdapter);

        newDeviceAdapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                connectBluetoothDevice(false, position);
            }
        });
    }*/

    private void setPairedDeviceNames() {
/*        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);*/

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                System.out.println(device.getName() + ":::::" + device.getAddress());
                spinnerAdapter.addItem(new DeviceData(device.getName(), device.getAddress()));
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.none_paired, Toast.LENGTH_LONG).show();
        }
        spinnerAdapter.notifyDataSetChanged();
        spBluetoothDevice.setAdapter(spinnerAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        //this.unregisterReceiver(mReceiver);
    }

/*    //Start device discover with the BluetoothAdapter
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.tv_title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }*/

    private void setEdtStudentName() {
        String studentName = sharedPref.getString(getString(R.string.lastly_registered_student_name), null);
        if (studentName != null) {
            edtStudentName.setText(studentName);
        }
    }

    private void saveStudentName(String studentName) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.lastly_registered_student_name), studentName);
        editor.commit();

    }

    private void connectBluetoothDevice(boolean paired, int position) {
        String studentName = edtStudentName.getText().toString();
        int nameLength = studentName.replace(" ", "").length();

        if (nameLength == 0) {
            Toast.makeText(getApplicationContext(), R.string.hint_student_name, Toast.LENGTH_LONG).show();
        } else if (nameLength > 10) {
            Toast.makeText(getApplicationContext(), R.string.hint_student_name_length, Toast.LENGTH_LONG).show();
        } else {
            // Cancel discovery because it's costly and we're about to connect
            //mBtAdapter.cancelDiscovery();

            String address = "";
            if (paired) {
                address = ((DeviceData) spinnerAdapter.getItem(position)).getMacAddress();
            }/*else{
                address = newDeviceAdapter.getItem(position).getMacAddress();
            }*/

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(EXTRA_STUDENT_NAME, studentName);

            saveStudentName(studentName);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

/*    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDeviceAdapter.addItem(new DeviceData(device.getName(), device.getAddress()));
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //setProgressBarIndeterminateVisibility(false);
                //setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    newDeviceAdapter.addItem(new DeviceData(noDevices, ""));
                }
            }
            newDeviceAdapter.notifyDataSetChanged();
        }
    };*/

}
