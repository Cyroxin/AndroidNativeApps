package com.example.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.example.bluetooth.helper.BleWrapper
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap
import android.content.Intent





@Suppress("DeferredResultUnused")
class MainActivity : AppCompatActivity() {
    companion object {
        const val SCAN_PERIOD: Long = 3000
    }

    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var mScanning : Boolean = false
    private var mScanResults: HashMap<String, ScanResult>? = null

    private var mConnections: MutableList<Pair<BluetoothGatt, BleWrapper>> = mutableListOf()

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBle()

        val list = findViewById<LinearLayout>(R.id.list)

        findViewById<Button>(R.id.scan).setOnClickListener { btnScan ->
            (btnScan as Button).text = "Scanning..."

            var found = 0
            while(true)
            {
                val e = list.children.elementAtOrNull(found) ?: break

                if(mConnections.any { con -> con.first.device.address == e.findViewById<TextView>(R.id.scanaddr)?.text })
                    found++
                else
                    list.removeViewAt(found)
            }


            startScan{ e -> run{
                CoroutineScope(Dispatchers.IO).async {
                    withContext(Dispatchers.Main) {
                        btnScan.text = "Scan"

                        if(e == null) {
                            mScanResults?.forEach {
                                var scanresults = mutableListOf<Int>()

                                val cell : LinearLayout =
                                    layoutInflater.inflate(R.layout.scanview, null) as LinearLayout

                                var enabled = true

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    enabled = it.value.isConnectable
                                }



                                val name = cell.findViewById<TextView>(R.id.scanname)

                                if (it.value.device.name != null && it.value.device.name.isNotEmpty())
                                    name.text = it.value.device.name
                                else
                                    name.text = "unknown"


                                val address = cell.findViewById<TextView>(R.id.scanaddr)
                                address.text = it.key

                                val rssi = cell.findViewById<TextView>(R.id.scansignal)
                                rssi.text = it.value.rssi.toString() + "dBm"

                                val connect = cell.findViewById<Button>(R.id.scanconnect)

                                val data = cell.findViewById<TextView>(R.id.scandata)
                                data.setOnClickListener {
                                    val intent = Intent(this@MainActivity, MonitorActivity::class.java)
                                    val data = intent.putExtra("data",scanresults.toIntArray())
                                    startActivity(intent)
                                }


                                val obj = object : BleWrapper.BleCallback {
                                    override fun onDeviceReady(gatt: BluetoothGatt, bleWrapper: BleWrapper) {
                                        Log.d("DBG", "DeviceReady")
                                        mConnections.add(
                                            Pair(
                                                gatt,
                                                bleWrapper
                                            )
                                        )

                                        connect.text = "Disconnect"
                                        connect.setOnClickListener (null)
                                        connect.setOnClickListener {
                                            Log.d("DBG","Remove called")
                                            mConnections.first { con -> con.first.device.address == address.text}.first.disconnect()
                                        }

                                        for (gattService in gatt.services) {
                                            Log.d("DBG", "Service ${gattService.uuid}")
                                            if (gattService.uuid == BleWrapper.HEART_RATE_SERVICE_UUID) {
                                                Log.d("DBG", "Found Heart Monitor")

                                                // Request notifications
                                                bleWrapper.getNotifications(gatt,gattService.uuid,gattService.getCharacteristic(BleWrapper.HEART_RATE_MEASUREMENT_CHAR_UUID).uuid)
                                                data.visibility = View.VISIBLE
                                            }
                                        }
                                    }

                                    override fun onDeviceDisconnected() {
                                        Log.d(
                                            "DBG",
                                            "DeviceDisconnected ${address.text}"
                                        )
                                        if(mConnections.any { con -> con.first.device.address == address.text.toString() }) {
                                            connect.text = "Connect"
                                            mConnections.removeAll { con -> con.first.device.address == address.text.toString() }
                                            connect.setOnClickListener (null)
                                            connect.setOnClickListener {
                                                val mBleWrapper = BleWrapper(this@MainActivity, address.text.toString())
                                                mBleWrapper.addListener(this)
                                                mBleWrapper.connect(false)
                                            }
                                            data.visibility = View.GONE
                                        }
                                        else
                                        {
                                            Toast.makeText(applicationContext,"Could not connect to the device",Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    override fun onNotify(characteristic: BluetoothGattCharacteristic) {

                                        if(characteristic.uuid == BleWrapper.HEART_RATE_MEASUREMENT_CHAR_UUID)
                                        {
                                            var format : Int = 0
                                            if(characteristic.properties and 0x01 != 0) // A single bit indicates the format
                                                format = BluetoothGattCharacteristic.FORMAT_UINT16
                                            else
                                                format = BluetoothGattCharacteristic.FORMAT_UINT8

                                                val value = characteristic.getIntValue(format, 1)!!
                                                Log.d(
                                                    "DBG",
                                                    "${address.text} onNotify ${(value)}"
                                                )

                                                // Avoid infinitely allocating more to the results
                                                val newresult = scanresults.takeLast(63).toMutableList()
                                                newresult.add(value)
                                                scanresults = newresult
                                                data.text = value.toString()

                                        }
                                    }
                                }


                                if(mConnections.any { con -> con.first.device.address == address.text.toString() }) {
                                    connect.text = "Disconnect"
                                    Log.d("DBG","Remove called")
                                    connect.setOnClickListener (null)
                                    connect.setOnClickListener {  mConnections.first { con -> con.first.device.address == address.text}.first.disconnect() }
                                }
                                else {
                                    connect.text = "Connect"
                                    connect.setOnClickListener (null)
                                    connect.setOnClickListener {
                                        val mBleWrapper = BleWrapper(this@MainActivity, address.text.toString())
                                        mBleWrapper.addListener(obj)
                                        mBleWrapper.connect(false)
                                    }
                                }


                                connect.isEnabled = enabled

                                list.addView(cell)


                            }
                        }
                        else Toast.makeText(applicationContext, e, Toast.LENGTH_LONG).show()

                    }
                }}
            }
        }
    }

    private fun initBle()
    {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
    }

    // Runs onFinish at scan end with error as parameter
    private fun startScan(onFinish : (String?) -> Unit)
    {
        if(hasPermissions())
        {
            mScanning = true
            mScanResults = HashMap()
            val mScanCallback = BtleScanCallback()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
            val filter: List<ScanFilter>? = null

            // Stops scanning after a pre-defined scan period.
            CoroutineScope(Dispatchers.IO).async {
                delay(SCAN_PERIOD)

                Log.d("DBG", "Scan stop")
                mBluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(mScanCallback)
                mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
                mScanning = false
                onFinish(null)
            }
            mBluetoothAdapter!!.bluetoothLeScanner.startScan(filter, settings, mScanCallback)
        }
        else
            onFinish("Please enable bluetooth and ensure your device supports bluetooth")
    }


    private inner class BtleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.d("DBG", "BLE Scan Failed with code $errorCode")
        }
        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address

            if(!mConnections.any { it.first.device.address == device.address})
                mScanResults!![deviceAddress] = result
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("DBG", "Device address: $deviceAddress (${result.isConnectable})")
            }
        }
    }

    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return true // assuming that the user grants permission
        }
        return true
    }
}