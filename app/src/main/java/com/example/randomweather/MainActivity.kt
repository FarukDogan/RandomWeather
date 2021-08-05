package com.example.randomweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest

import im.delight.android.location.SimpleLocation


import kotlinx.android.synthetic.main.activity_main.*


import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(),AdapterView.OnItemSelectedListener{

    private var tvSehir: TextView? =null
     var location:SimpleLocation?=null
    var latitude:String?=null
    var longitude:String?=null
    var gelenlat:String?=null
    var gelenlog:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Maps Activityden gelen değerler boş değilse kordinatlarına göre oankisehirt fonksiyonumuza yolluyoruz

        gelenlat =intent.getStringExtra("lat")
        gelenlog = intent.getStringExtra("log")




        if (gelenlat!=null){
            latitude=gelenlat
            longitude=gelenlog

            oankiSehriGetir(latitude,longitude)

        }

        //spinnerımızı ve spinner adapterimizi tanımlıyoruz

        val spinnerAdapter=ArrayAdapter.createFromResource(this,R.array.sehirler,R.layout.spinner_tek_satir)
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        spnSehirler.background.setColorFilter(resources.getColor(R.color.design_default_color_error),PorterDuff.Mode.SRC_ATOP)

        spnSehirler.setTitle("Şehir Seçin")
        spnSehirler.setPositiveButton("SEÇ")

        spnSehirler.adapter=spinnerAdapter



        spnSehirler.setOnItemSelectedListener(this)


   spnSehirler.setSelection(1)




    }
    // herhangibi bi Item seçildiğinde ne yapacagımızı tanımlıyoruz
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
       tvSehir=view as TextView?

// konum almak için izin maps activiye yönlendiriş
          if (position ==0 ){
                location=SimpleLocation(this)
                if (!location!!.hasLocationEnabled()){
                    Toast.makeText(this,"GPS AÇMANIZ GEREKİYOR",Toast.LENGTH_LONG).show()
                    SimpleLocation.openSettings(this)
                }else{
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),60)
                    }else{
                       location=SimpleLocation(this)
                        val intent =Intent(this,MapsActivity::class.java)
                        startActivity(intent)
                        
                    }
                }
//eğer spinnerdan bir item seçilirse guncel konum dışındakilerde şehre göre verileri getiriyoruz
            }else {
                if (gelenlat==null){
                    val secilenSehir =parent?.getItemAtPosition(position).toString()
                    tvSehir=view
                    verileriGetir(secilenSehir)
                }else{
                    //eğer burda gelenlatı null a çevirmezsek döngüye girip sürekli maps activitye atar kontrolümüzü gelenlat a göre yaptığımız için
                    gelenlat=null
                }


            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // izin işlemleri

        if (requestCode==60){
            if (grantResults.size> 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                val intent =Intent(this,MapsActivity::class.java)
                startActivity(intent)




            }else {
               spnSehirler.setSelection(1)
                Toast.makeText(this,"İzin Vermediğin için ne yazıkki seni metorolojiden mahrum bırakıyorum.",Toast.LENGTH_LONG).show()
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun oankiSehriGetir(lat:String?,long:String?) {


        val url=
            "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$long&appid=fe86144982b7b037b1ab92881151d764&lang=tr&units=metric"
        var sehirAdi :String?
        val havaDurumuObjeRequest2 = JsonObjectRequest(Request.Method.GET,url,null, object :Response.Listener<JSONObject>{
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onResponse(response: JSONObject?) {
// volley kütüphanesiyle yapılan json array ve object tanımlamaları
                val main = response?.getJSONObject("main")
                val sicaklik =main?.getInt("temp")
                sehirAdi = response?.getString("name")
                tvSehir?.text=sehirAdi

                val weather =response?.getJSONArray("weather")
                val aciklama = weather?.getJSONObject(0)?.getString("description")
                val icon = weather?.getJSONObject(0)?.getString("icon")
                //gelen icon verisine göre gecemi gündüz mü oldugunu anlayıp görünümü değiştiriyoruz
                if(icon?.last()=='d'){
                    rootLayout.background=getDrawable(R.drawable.bg)
                    tvTarih.setTextColor(resources.getColor(R.color.black))
                    tvSicaklik.setTextColor(resources.getColor(R.color.black))
                    textView4.setTextColor(resources.getColor(R.color.black))
                    tvSehir?.setTextColor(resources.getColor(R.color.black))
                    tvDurum.setTextColor(resources.getColor(R.color.black))


                }else {
                    rootLayout.background=getDrawable(R.drawable.gece)
                    tvTarih.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    tvSicaklik.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    textView4.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    tvSehir?.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    tvDurum.setTextColor(resources.getColor(R.color.design_default_color_surface))


                }
                val resimDosyaAdi =resources.getIdentifier("icon_"+icon?.sonKarakteriSil(),"drawable",packageName)
                imgSembol.setImageResource(resimDosyaAdi)

                tvTarih.text= tarihYazdir()
                tvSehir?.text=sehirAdi
                tvSicaklik.text=sicaklik.toString()
                tvDurum.text=aciklama



            }


        },object :Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {
                println("oanki sehir tetiklendi hata")

            }
        })


        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest2)

println("oankisehri getir tetiklendi")

    }



    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
// aynı işlemleri lat longla değil şehir adıyla spinnerimizden aldıgımız verilerle yapıyoruz
    fun verileriGetir(sehir:String){
        val url=
            "https://api.openweathermap.org/data/2.5/weather?q=$sehir&appid=fe86144982b7b037b1ab92881151d764&lang=tr&units=metric"
        val havaDurumuObjeRequest = JsonObjectRequest(Request.Method.GET,url,null, object :Response.Listener<JSONObject>{
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onResponse(response: JSONObject?) {

                val main = response?.getJSONObject("main")
                val sicaklik =main?.getInt("temp")
                val sehirAdi = response?.getString("name")
                val weather =response?.getJSONArray("weather")
                val aciklama = weather?.getJSONObject(0)?.getString("description")
                val icon = weather?.getJSONObject(0)?.getString("icon")
                if(icon?.last()=='d'){
                    rootLayout.background=getDrawable(R.drawable.bg)
                    tvTarih.setTextColor(resources.getColor(R.color.black))
                    tvSicaklik.setTextColor(resources.getColor(R.color.black))
                    textView4.setTextColor(resources.getColor(R.color.black))
                    tvSehir?.setTextColor(resources.getColor(R.color.black))
                    tvDurum.setTextColor(resources.getColor(R.color.black))


                }else {
                    rootLayout.background=getDrawable(R.drawable.gece)
                    tvTarih.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    tvSicaklik.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    textView4.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    tvSehir?.setTextColor(resources.getColor(R.color.design_default_color_surface))
                    tvDurum.setTextColor(resources.getColor(R.color.design_default_color_surface))


                }
                val resimDosyaAdi =resources.getIdentifier("icon_"+icon?.sonKarakteriSil(),"drawable",packageName)
                imgSembol.setImageResource(resimDosyaAdi)

                tvTarih.text= tarihYazdir()
                tvSehir?.text = sehirAdi
                tvSicaklik.text=sicaklik.toString()
                tvDurum.text=aciklama



            }


        },object :Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {

            }
        })


        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest)
        println("verileri getir tetiklendi")
    }

    fun tarihYazdir(): String {
        val takvim = Calendar.getInstance().time
        val formatliyici = SimpleDateFormat("EEEE,MMM yyyy", Locale("tr"))
        val tarih = formatliyici.format(takvim)
        return tarih
    }



private fun String.sonKarakteriSil(): String {
    return this.substring(0,this.length-1)

}
    fun openMap(view:View){
val intent =Intent(this,MapsActivity::class.java,)
        startActivity(intent)

    }
}
