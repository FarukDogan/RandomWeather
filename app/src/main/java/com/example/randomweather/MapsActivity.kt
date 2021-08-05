package com.example.randomweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener: LocationListener
   // lateinit var sharedPreferences: SharedPreferences


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater =menuInflater
        menuInflater.inflate(R.menu.random_places,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.rastgele){
// rastgele butonuna basınca rastgele lat ve long oluşturup tanımlıyoruz

            val randomlat=(0..90).random().toDouble()
            val randomlong=(-125..179).random().toDouble()
            val latUzanti = ("$randomlat"+"1")
            val lonUzanti = ("$randomlong"+"1")


            val denkgelenlokasyon=LatLng(latUzanti.toDouble(),lonUzanti.toDouble())

            yenile(denkgelenlokasyon)

            println(denkgelenlokasyon)


        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)



        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("ServiceCast")
    override fun onMapReady(googleMap: GoogleMap) {

// mapimiz açıldıgında kullanıcın güncel konumu veyyahut son konumunu gösterdiğimiz kodlarımız
        mMap = googleMap
        mMap.setOnMapLongClickListener(dinleyici)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location) {

                mMap.clear()
                val guncelKonum = LatLng(p0.latitude, p0.longitude)

                println("${p0.latitude}" + "${p0.longitude}")

                mMap.addMarker(MarkerOptions().position(guncelKonum).title("Guncel Konumunuz"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guncelKonum, 12f))

                val geocoder = Geocoder( this@MapsActivity, Locale.getDefault())
                try {
                    val adresListesi = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    if (adresListesi.size > 0) {
                        println(adresListesi.get(0).toString())
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1f, locationListener)
            val sonBilinenkonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (sonBilinenkonum != null) {
                val sonbilinenLatLang = LatLng(sonBilinenkonum.latitude, sonBilinenkonum.longitude)
                mMap.addMarker(MarkerOptions().position(sonbilinenLatLang).title("Son Bilinen Konumunuz"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonbilinenLatLang, 12f))
            }

        }


    }
    fun yenile(red: LatLng){
// random gelen kordinatlarımıza göre kamerayı hareket ettirip markerın yerini değiştirdiğimiz fonksiyon

        mMap.clear()


        val denkgelenlokasyon=LatLng(red.latitude,red.longitude)
        mMap.addMarker(MarkerOptions().position(denkgelenlokasyon).title("Güzel Seçim"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(denkgelenlokasyon,8f))
        println(denkgelenlokasyon)
        println("denk gelen çalışdı")

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==1){
            if (grantResults.size > 0){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,4f,locationListener)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    val dinleyici =object : GoogleMap.OnMapLongClickListener {
        //mapimize uzun tıklayınca marker koymasını sağladıgımız fonksiyonumuz
        override fun onMapLongClick(p0: LatLng?) {
            mMap.clear()

            val geocoder=Geocoder( this@MapsActivity, Locale.getDefault())

            if (p0!=null){
                var adres = ""
                try {
                    val adresListesi = geocoder.getFromLocation(p0.latitude,p0.longitude,1)

                    if (adresListesi.size>0){
                        if (adresListesi.get(0).thoroughfare !=null){
                            adres+=  adresListesi.get(0).thoroughfare
                            if (adresListesi.get(0).subThoroughfare !=null){
                                adres+= adresListesi.get(0).subThoroughfare
                            }
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
                mMap.addMarker(MarkerOptions().position(p0).title(adres))

                val alert=AlertDialog.Builder( this@MapsActivity)

                alert.setTitle("$adres ")

                alert.setMessage("Seçtiğin Yerin Hava Durumunu Görmek İstermisin?")
                alert.setCancelable(false)

                alert.setPositiveButton("Evet") { dialog ,which ->
                    val lat =p0.latitude.toString()
                    val log = p0.longitude.toString()


                    val formatlat:Double= String.format("%.2f",lat).toDouble()
                    val formatlog:Double= String.format("%.2f",log).toDouble()

                    val intent =Intent( this@MapsActivity,MainActivity::class.java)

                    intent.putExtra("lat",formatlat)
                    intent.putExtra("log",formatlog)

                 //   lattt=formatlat
                   // longggg=formatlog

/*
                    sharedPreferences=applicationContext.getSharedPreferences("com.example.randomweather",Context.MODE_PRIVATE)
                    var sharedLong= sharedPreferences.getInt("long",0)
                    var sharedLat= sharedPreferences.getInt("lat",0)
                    formatlat=sharedLat.toDouble()
                    formatlog=sharedLong.toDouble()*/
                    println(formatlat)
                    println(formatlog)
                    startActivity(intent)
                    finish()



                }
                alert.setNegativeButton("Hayır"){ dialog , which ->
                    Toast.makeText( this@MapsActivity,"Merak hep seni diri tutar.",Toast.LENGTH_LONG).show()
                }
                alert.show()


            }

        }

    }


}
