package com.demo.placetracker

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mSearchText = findViewById(R.id.input_search);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
    }

    private fun fetchLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                currentLocation = location
                val supportMapFragment =
                    (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
                supportMapFragment.getMapAsync(this@MapsActivity)
                val v1 = supportMapFragment.view as ViewGroup
                val v2 = v1.getChildAt(0) as ViewGroup
                val v3 = v2.getChildAt(2) as ViewGroup
                val position = v3.getChildAt(0) as View
                val positionWidth = position.layoutParams.width
                val positionHeight = position.layoutParams.height
                //lay out position button
                val positionParams = RelativeLayout.LayoutParams(positionWidth, positionHeight)
                positionParams.setMargins(40, 0, 0, 180)
                positionParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                position.layoutParams = positionParams
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()
        mMap.setMyLocationEnabled(true)

        if (currentLocation != null) {
            val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            val markerOptions = MarkerOptions().position(latLng).title("I am here!")
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            googleMap.addMarker(markerOptions)
        } else {
            Toast.makeText(
                applicationContext,
                "Please Check the INTERNET/GPS Connection!",
                Toast.LENGTH_LONG
            )
                .show()
        }
        init()

    }


    private fun init() {
        mSearchText.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                geoLocate()
            }
            false
        }
    }

    private fun geoLocate() {
        //clear the old markers  in google maps
        mMap.clear()
        val searchString = mSearchText.text.toString()
        if (searchString == null || searchString == "") {
            Toast.makeText(applicationContext,"provide location",Toast.LENGTH_SHORT).show()
        } else {
            val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            try {
                list = geocoder.getFromLocationName(searchString, 1)
            } catch (e: IOException) {
                Log.e("----------------", "geoLocate: IOException: $e")

            }

            if (list.size > 0) {
                val address: Address = list[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(searchString))
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                Log.e(
                    "===============",
                    "geoLocate: found a location: $address"
                )
            } else {
                Toast.makeText(applicationContext, "Please Enter valid Address!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()
            }
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
