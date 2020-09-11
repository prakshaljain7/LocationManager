package com.example.locationmanager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.Provider

class MainActivity : AppCompatActivity() {
    private lateinit var textViewLat:TextView
    private lateinit var textViewLong:TextView
    private lateinit var criteria: Criteria
    private lateinit var bestProvider:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewLat = findViewById(R.id.tv_lat)
        textViewLong = findViewById(R.id.tv_long)
        criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        getLastKnownLocation(this)
    }

    private fun getContinuousLocation(context: Context) {
        GlobalScope.launch {
            while (true) {
                getLastKnownLocation(context)
                delay(1000)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation(): Location? {
        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            Log.i("insideProvider",provider)
            val l: Location = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
                bestProvider = provider
            }
        }
        return bestLocation
    }
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(context: Context) {
        Log.i("method","called")
        val isLocationEnabled = isLocationServiceEnabled(context)
        if (isLocationEnabled.first) {

            val mHandler = Handler(Looper.getMainLooper())

            val locationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val lastKnownLocation = getLastLocation()
            Toast.makeText(
                context,
                "lat  ${lastKnownLocation?.latitude} long ${lastKnownLocation?.longitude}",
                Toast.LENGTH_SHORT
            ).show()

            val locationListener: LocationListener = object : LocationListener {
                override
                fun onLocationChanged(p0: Location) {
                    Log.i("inside","locationChanged")
                    mHandler.post {
                        textViewLat.text = p0.latitude.toString()
                        textViewLong.text = p0.longitude.toString()
                        Toast.makeText(
                            context,
                            "lat  ${p0?.latitude} long ${p0?.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override
                fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) {
                    Log.i("inside","status change")
                }

                override
                fun onProviderEnabled(provider: String) {
                    Log.i("inside","enable")
                }

                override
                fun onProviderDisabled(provider: String) {
                    Log.i("inside","disable")
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                0f,
                locationListener
            )

        } else {
            Log.i("inside","False")
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