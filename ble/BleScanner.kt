package com.babetech.ucb_admin_access.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled

class BleScanner(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    var onMatriculeDetected: ((String) -> Unit)? = null

    // Map pour mémoriser les matricules déjà envoyés avec leur timestamp
    private val sentMatricules = mutableMapOf<String, Long>()

    private val TEN_MINUTES_MILLIS = 10 * 60 * 1000L

    fun startScanning() {
        if (!hasRequiredPermissions()) {
            Log.e("BleScanner", "Permissions requises manquantes")
            logPermissionsState()
            return
        }

        if (bluetoothAdapter?.isEnabled != true) {
            Log.e("BleScanner", "Bluetooth désactivé")
            return
        }

        try {
            scanner?.startScan(scanCallback)
            Log.i("BleScanner", "Scan BLE démarré")
        } catch (e: SecurityException) {
            Log.e("BleScanner", "SecurityException : permission manquante au moment du scan : ${e.message}")
        }
    }

    fun stopScanning() {
        if (!hasRequiredPermissions()) {
            Log.e("BleScanner", "Permissions requises manquantes")
            logPermissionsState()
            return
        }

        try {
            scanner?.stopScan(scanCallback)
            Log.i("BleScanner", "Scan BLE arrêté")
        } catch (e: SecurityException) {
            Log.e("BleScanner", "SecurityException : permission manquante au moment de l'arrêt du scan : ${e.message}")
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun logPermissionsState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i("BleScanner", "BLUETOOTH_SCAN permission = ${
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
            }")
        }
        Log.i("BleScanner", "ACCESS_FINE_LOCATION permission = ${
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        }")

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.i("BleScanner", "Location enabled = ${isLocationEnabled(locationManager)}")
        Log.i("BleScanner", "Bluetooth Adapter enabled = ${bluetoothAdapter?.isEnabled}")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i("BleScanner", "Scan result reçu : ${result.device.address} RSSI=${result.rssi}")

            val manufacturerData = result.scanRecord?.manufacturerSpecificData
            if (manufacturerData != null && manufacturerData.size() > 0) {
                for (i in 0 until manufacturerData.size()) {
                    val id = manufacturerData.keyAt(i)
                    val dataBytes = manufacturerData.valueAt(i)
                    val rawString = dataBytes?.toString(Charsets.UTF_8) ?: "null"
                    Log.i("BleScanner", "ManufacturerData ID=0x${id.toString(16).uppercase()} value=$rawString")
                }
            } else {
                Log.i("BleScanner", "Aucune ManufacturerData trouvée pour ce scan.")
            }

            val data = result.scanRecord?.getManufacturerSpecificData(0xFFAA)
            val rawMatricule = data?.toString(Charsets.UTF_8)
            val matricule = rawMatricule?.filter { it.isLetterOrDigit() || it in listOf('/', '.', '-') }

            Log.i("BleScanner", "RSSI=${result.rssi} | Matricule brute = $rawMatricule | Matricule nettoyé = $matricule")

            if (result.rssi > -50 && !matricule.isNullOrEmpty()) {
                val now = System.currentTimeMillis()
                val lastSentTime = sentMatricules[matricule]

                if (lastSentTime == null || now - lastSentTime > TEN_MINUTES_MILLIS) {
                    Log.i("BleScanner", "Détection matricule nettoyé=$matricule RSSI=${result.rssi} -> envoyé")
                    onMatriculeDetected?.invoke(matricule)
                    sentMatricules[matricule] = now
                } else {
                    Log.i("BleScanner", "Matricule $matricule déjà envoyé il y a moins de 10 minutes, ignoré.")
                }
            }
        }
    }
}
