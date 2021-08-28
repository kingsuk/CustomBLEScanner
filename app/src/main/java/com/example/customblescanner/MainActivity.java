package com.example.customblescanner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

public class MainActivity extends AppCompatActivity {

    String TAG = "kingsuk";

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean scanning;
    private Handler handler = new Handler();
    private ArrayList<BluetoothDevice> bleDeviceList = new ArrayList<BluetoothDevice>();
    private ArrayList<String> bleDeviceName = new ArrayList<String>();


    BluetoothGatt gatt;

    ArrayAdapter<String> listViewAdapter;



    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    UUID BATTERY_SERVICE_SERVICE_UUID = convertFromInteger(0x180F);
    UUID BATTERY_LEVEL_SERVICE_UUID = convertFromInteger(0x2A19);
    UUID UUID_SERVICE_MILI = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    UUID UUID_CHAR_REALTIME_STEPS = UUID.fromString("0000ff06-0000-1000-8000-00805f9b34fb");
    UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION_D = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.list);

        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(getApplicationContext(),"BLE Supported",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"BLE Not Supported",Toast.LENGTH_LONG).show();

        }
        getPermissions();

        //getting BLE System adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        listViewAdapter = new ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item,
                bleDeviceName);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice currentDevice = bleDeviceList.get(position);
                //connectGatt(currentDevice.getAddress());
                //currentDevice = mBluetoothAdapter.getRemoteDevice("14:08:15:02:1F:58");
                gatt = currentDevice.connectGatt(getApplicationContext(), true, gattCallback,BluetoothDevice.TRANSPORT_LE);
                gatt.connect();

            }
        });


        scanLeDevice();

    }

//    private boolean connectGatt(final String address) {
//        if (mBluetoothAdapter == null || address == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//        if (mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        final BluetoothDevice device = mBluetoothAdapter
//                .getRemoteDevice(address);
//        if (device == null) {
//            Log.w(TAG, "Device not found.  Unable to connect.");
//            return false;
//        }
//
//        mBluetoothGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
//        Log.d(TAG, "Trying to create a new connection.");
//        return mBluetoothGatt.connect();
//    }



    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == STATE_CONNECTED){
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BluetoothGattCharacteristic characteristics = gatt.getService(BATTERY_SERVICE_SERVICE_UUID).getCharacteristic(BATTERY_LEVEL_SERVICE_UUID);
            //BluetoothGattCharacteristic charec = characteristics.getCharacteristic(UUID_DESCRIPTOR_UPDATE_NOTIFICATION_D);
            gatt.readCharacteristic(characteristics);

//            List<BluetoothGattCharacteristic> characteristics = gatt.getService(UUID_SERVICE_MILI).getCharacteristics();
//            for(BluetoothGattCharacteristic characteristic : characteristics)
//            {
//                gatt.setCharacteristicNotification(characteristic,true);
//                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                gatt.writeDescriptor(descriptor);
//            }

//            gatt.setCharacteristicNotification(characteristic,true);
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(descriptor);
            //gatt.readCharacteristic(characteristic);
            Log.i(TAG,characteristics.toString());
//                            .getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID);
//            BluetoothGattDescriptor descriptor =
//                    characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
//
//            descriptor.setValue(
//                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(characteristic.getUuid().equals(BATTERY_LEVEL_SERVICE_UUID))
            {
                processData(characteristic);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private void processData(BluetoothGattCharacteristic characteristic)
    {
        final byte[] data = characteristic.getValue();
        String val = String.valueOf(characteristic.getValue());
        ArrayList<String> retrunData = new ArrayList<>();
        if (data != null && data.length > 0) {

            for (byte byteChar : data)
            {
                int retrunDataInt = unsignedByteToInt(byteChar);
                retrunData.add(String.valueOf(retrunDataInt));
            }

            Log.i(TAG, "Data Received: " + retrunData.get(0));
        }

        Toast.makeText(getApplicationContext(),retrunData.get(0),Toast.LENGTH_LONG).show();


    }

//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                // successfully connected to the GATT Server
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                // disconnected from the GATT Server
//            }
//        }
//    };

    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    mBluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            mBluetoothAdapter.startLeScan(leScanCallback);
        } else {
            scanning = false;
            mBluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.i("kingsuk",device.getAddress().toString());
                            if(!bleDeviceList.contains(device))
                            {
                                if(device.getName()!=null)
                                {
                                    bleDeviceName.add(device.getName()+" / "+device.getAddress());
                                }
                                else
                                {
                                    bleDeviceName.add(device.getAddress());
                                }
                                bleDeviceList.add(device);

                                listViewAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };
    public void getPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app needs background location access");
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_REQUEST_FINE_LOCATION);
                            }

                        });
                        builder.show();
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }

                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
    }
    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

}