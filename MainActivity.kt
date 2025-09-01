package com.babetech.ucb_admin_access

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.Scaffold
import com.babetech.ucb_admin_access.ui.AppScreen
import com.babetech.ucb_admin_access.ui.ScannerScreen
import com.babetech.ucb_admin_access.ui.theme.UCBADMINACCESSTheme
import com.babetech.ucb_admin_access.viewmodel.ScannerViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val PERMISSION_REQUEST_CODE = 1001

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Demander les permissions nécessaires
        checkAndRequestBluetoothPermissions()

        setContent {
            UCBADMINACCESSTheme {
                AppScreen()

            }
        }
    }

    private fun checkAndRequestBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

//    // Pour vérifier le retour utilisateur si besoin
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                Log.i("MainActivity", "Toutes les permissions accordées ✅")
//            } else {
//                Log.e("MainActivity", "Certaines permissions refusées ❌")
//            }
//        }
//    }
}


fun hasBluetoothPermissions(context: Context): Boolean {
    val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    return requiredPermissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

