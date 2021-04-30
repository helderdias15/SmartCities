package ipvc.cmmv.ei22054

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ipvc.cmmv.ei22054.api.EndPoints
import ipvc.cmmv.ei22054.api.Ocurrencia
import ipvc.cmmv.ei22054.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.app.ComponentActivity.ExtraData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var ocurencia: List<Ocurrencia>
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false
        mMap = googleMap
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if(location != null){
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude,location.longitude),
                        15f
                    )
                )
            }
        }



        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getAllOcurencias()
        var position: LatLng
        var id: Int? = 0

        val sharedPref: SharedPreferences = getSharedPreferences(
            getString(R.string.login_p), Context.MODE_PRIVATE
        )

        if (sharedPref != null) {
            id = sharedPref.all[getString(R.string.id)] as Int?
        }

        call.enqueue(object : Callback<List<Ocurrencia>> {
            override fun onResponse(call: Call<List<Ocurrencia>>, response: Response<List<Ocurrencia>>) {

                if (response.isSuccessful) {

                    ocurencia = response.body()!!

                    for (occurrence in ocurencia) {

                        position = LatLng(occurrence.latitude, occurrence.longitude)

                        if (occurrence.id_utilizador == id) {
                            mMap.addMarker(MarkerOptions().position(position).title(occurrence.titulo + " - " + occurrence.descricao).icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)))
                        } else {
                            mMap.addMarker(MarkerOptions().position(position).title(occurrence.titulo + " - " + occurrence.descricao).icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

                        }

                    }
                }
            }

            override fun onFailure(call: Call<List<Ocurrencia>>, t: Throwable) {
                Toast.makeText(this@Mapa, "Erro", Toast.LENGTH_SHORT).show()
            }
        })

    }


}
