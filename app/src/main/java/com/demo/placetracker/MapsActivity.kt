package com.demo.placetracker

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private lateinit var mMap: GoogleMap
    private var googleApiClient: GoogleApiClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var lastLocation: Location
    private var currentuserLocationMarker: Marker? = null
    private val Request_User_Location_Code: Int = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission()
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.search_address -> {
                var addressField: EditText = findViewById(R.id.location_search)
                var address: String = addressField.text.toString()
                var addressList: List<Address>? = null
                if (!TextUtils.isEmpty(address)) {
                    var geocoder: Geocoder = Geocoder(this)
                    try {
                        addressList = geocoder.getFromLocationName(address, 1)
                        if (addressList != null) {
                            val location = addressList!![0]
                            val latLng = LatLng(location.latitude, location.longitude)
                            mMap!!.addMarker(MarkerOptions().position(latLng).title(address))
                            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                            Toast.makeText(
                                applicationContext,
                                location.latitude.toString() + " " + location.longitude,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Location not found",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun checkUserLocationPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    Array<String>(10) { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    Request_User_Location_Code
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    Array<String>(10) { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    Request_User_Location_Code
                )


            }
            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Request_User_Location_Code ->
                if (grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient()
                        }
                        mMap.isMyLocationEnabled = true

                    }
                }
        }
    }

    @Synchronized
    fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClient!!.connect()
    }

    override fun onConnected(p0: Bundle?) {
        locationRequest = LocationRequest().setInterval(1100)
        locationRequest = LocationRequest().setFastestInterval(1100)
        locationRequest =
            LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                this
            )
        }

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {

        lastLocation = location
        currentuserLocationMarker?.remove()
        var latLng = LatLng(location.latitude, location.longitude)
        var markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("User current location")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

        currentuserLocationMarker = mMap.addMarker(markerOptions)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomBy(12F))
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)

        }
    }
    companion object {
        lateinit var mMap: GoogleMap
        var currentLocation: Location? = null
        lateinit var fusedLocationProviderClient: FusedLocationProviderClient
        lateinit var mSearchText: EditText
        private const val REQUEST_CODE = 101
        var list: List<Address> = ArrayList()

    }

}


