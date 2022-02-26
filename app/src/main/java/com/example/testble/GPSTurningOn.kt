package com.example.testble


import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class GpsTurningOn() {

    fun displayLocationSettingsRequest(activity: Activity, context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 100000
        locationRequest.fastestInterval = (100000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(activity)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(activity)
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener(object : OnCompleteListener<LocationSettingsResponse> {
            override fun onComplete(p0: Task<LocationSettingsResponse>) {

                try {
                    val status = p0.getResult(ApiException::class.java)
                    Log.i(
                        "TAG",
                        "All location settings are satisfied $status."
                    )
                } catch (ex: ApiException) {
                    when (ex.getStatusCode()) {

                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i(
                                "TAG",
                                "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                            )
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the result
                                // in onActivityResult().
                                val resolvableApiException = ex as ResolvableApiException
                                resolvableApiException.startResolutionForResult(activity, 1)
                            } catch (e: IntentSender.SendIntentException) {
                                Log.i("TAG", "PendingIntent unable to execute request.")
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                            "TAG",
                            "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                        )
                    }
                }

            }
        })
    }


}