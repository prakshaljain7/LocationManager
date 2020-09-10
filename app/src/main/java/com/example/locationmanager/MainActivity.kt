package com.example.locationmanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var textViewLat:TextView
    private lateinit var textViewLong:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewLat = findViewById(R.id.tv_lat)
        textViewLong = findViewById(R.id.tv_long)
        getContinuousLocation(this)
    }

    private fun getContinuousLocation(context: Context) {
        GlobalScope.launch {
            while (true) {
                getLastKnownLocation(context)
                delay(1000)
            }
        }
    }

    private fun getLastKnownLocation(context: Context) {

        val isLocationEnabled = isLocationServiceEnabled(context)
        if (isLocationEnabled.first) {

            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(applicationContext)


            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.requestLocationUpdates(LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 5000
            },object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations){
                        // Update UI with location data
                        // ...
                        textViewLat.text = location.latitude.toString()
                        textViewLong.text = location.longitude.toString()
                        Toast.makeText(
                            context,
                            "lat  ${location.latitude} long ${location.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }, Looper.getMainLooper())

        } else {
            Toast.makeText(context, "service not available", Toast.LENGTH_SHORT).show()
        }

    }

    private fun isLocationServiceEnabled(context: Context): Pair<Boolean, Boolean> {
        var gpsEnabled = false
        var networkEnabled = false
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager != null) {
            try {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
                Log.d("wert", ex.toString())
            }
            try {
                networkEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return Pair(gpsEnabled || networkEnabled, gpsEnabled)
    }
}