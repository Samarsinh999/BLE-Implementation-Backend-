package com.example.blenoui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var scanResultAdapter: ScanResultAdapter
    private lateinit var scanResults: MutableList<ScanResult>
    var bluetoothGatt: BluetoothGatt? = null

//    private var bluetoothService : BluetoothLeServices? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        scanResults = ArrayList()
        scanResultAdapter = ScanResultAdapter(scanResults)

        // Set up RecyclerView with a LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = scanResultAdapter

        scanResultAdapter.setOnItemClickListener(object : ScanResultAdapter.OnItemClickListener {
            @SuppressLint("HardwareIds", "LongLogTag", "SuspiciousIndentation")
            override fun onItemClick(scanResult: ScanResult) {
                    bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
                    val address = scanResult.device?.address
                    if (address!= null){
                        connect(address, applicationContext)
                        Log.d("SCAN RESULT", "onDeviceFound ${scanResult.scanRecord?.deviceName}")
                    }
            }
        })

        val scanButton = findViewById<Button>(R.id.scan_button)
        scanButton.setOnClickListener {
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show()
            bluetoothAdapter.bluetoothLeScanner.startScan(leScanCallback)

        }

        val stopButton = findViewById<Button>(R.id.stop_scan)
        stopButton.setOnClickListener {
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show()
            bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            with(result.device) {
                Log.i("Ble","Devices triggreed ${name ?: "Unnamed"}, address: $address, RSSI: ${result.rssi}")
                scanResults.add(result)
                scanResultAdapter.notifyDataSetChanged()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun connect(address: String, context: Context){
        val device= bluetoothAdapter.getRemoteDevice(address)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE )
        }
    }

    fun disConnect(context: Context){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt?.close()
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("State Connected", "The details are ${status}, and ${newState}")
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("State Connected", "The details are ${status}, and ${newState}")
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.d("CONNECTION_MANAGER", "onServicesDiscovered ")

            gatt?.services?.forEach { service ->
                service?.characteristics?.forEach { char ->
                    if (char.uuid == UUID.fromString("74686562-6c75-6172-6d6f-722e636f6d01")) {
                            gatt.writeCharacteristic(
                                char,
                                byteArrayOf(0x08, 0x13, 0xAA.toByte(), 0x01, 0x00),
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            )
                    }
                    if (char.uuid == UUID.fromString("74686562-6c75-6172-6d6f-722e636f6d02")) {
                            gatt.setCharacteristicNotification(char, true)
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d("Connection Manager", "onCharacteristicWrite")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.d("Connection Manager", "onCharacteristicChanged ${value}")
        }
    }
}








