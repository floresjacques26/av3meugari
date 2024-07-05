package com.coding.meugari

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coding.meugari.adapters.BookmarkAdapter
import com.coding.meugari.adapters.BookmarkData
import com.coding.meugari.adapters.LayerAdapter
import com.coding.meugari.adapters.LayerData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.maps.android.data.kml.KmlLayer
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.concurrent.Executors


class HomeActivity : AppCompatActivity() {

    //region private Variables

    private var mMap: GoogleMap? = null
    private var layer: KmlLayer? = null

    private var googleMapType: Int? = null
    private var maplayer:Int? = null

    private var markerCurrentLocation: Marker? = null
    private var markerBookmarkLocation: Marker? = null
    private var markerCurrentLocationFirstShow = true

    private var fabCompass: FloatingActionButton? = null
    private var fabBookmark: FloatingActionButton? = null
    private var fabSearch: FloatingActionButton? = null
    private var fabLocation: FloatingActionButton? = null
    private var fabLayerMap: FloatingActionButton? = null

    private var locationManager: LocationManager? = null
    private var LOCATION_REFRESH_TIME: Long = 5000 // 5 seconds to update
    private var LOCATION_REFRESH_DISTANCE = 100f // 500 meters to update
    private val PERMISSION_CODE = 1

    private var bottomNavigation : CurvedBottomNavigation? = null

    private var vwMap: View? = null
    private var lvLayers: ListView? = null
    private var lvBookmarks: ListView? = null
    private var frOptions: View? = null

    private var listBookmarks = mutableListOf<BookmarkData>()

    private var addBookmarkOnNextClick : Boolean? = null

    private var markerGhostTrucks = mutableListOf<Marker>()

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        vwMap = findViewById<View>(R.id.fMaps)
        vwMap!!.visibility = View.VISIBLE

        frOptions = findViewById<View>(R.id.frOptions)
        frOptions!!.visibility = View.INVISIBLE

        lvBookmarks = findViewById<ListView>(R.id.lvBookmarks)
        lvBookmarks!!.visibility = View.INVISIBLE

        lvLayers = findViewById<ListView>(R.id.lvLayers)
        lvLayers!!.visibility = View.INVISIBLE

        val listLayers = mutableListOf<LayerData>()
        listLayers.add(LayerData(0, R.drawable.avatar_a, "Coleta Convencional", "Coleta realizada por caminhões da prefeitura nas ruas."))
        listLayers.add(LayerData(1, R.drawable.avatar_c, "Coleta Seletiva", "Semelhante a coleta convencional, porém utilizando recipientes separados para cada tipo de resíduo."))
        listLayers.add(LayerData(2, R.drawable.avatar_b, "Coleta Flex Vidros", "Pontos específicos com grandes containers exclusivos para recolhimento de vidros."))
        listLayers.add(LayerData(3, R.drawable.avatar_e, "Coleta Orgânicos", "Locais com recipientes especiais para coleta de lixo orgânico, como restos de alimentos por exemplo."))
        listLayers.add(LayerData(4, R.drawable.avatar_d, "Mapa normal", "Mapa do google normal, sem informações sobre coletas."))
        lvLayers!!.adapter = LayerAdapter(this, listLayers)

        //region BottomNavigation
        bottomNavigation = findViewById<View>(R.id.bottomNavigation) as CurvedBottomNavigation?
        bottomNavigation?.add(CurvedBottomNavigation.Model(0, "Mapa", R.drawable.baseline_map_location_dot_solid))
        bottomNavigation?.add(CurvedBottomNavigation.Model(1, "Visualização", R.drawable.baseline_auto_awesome_motion_24))
        bottomNavigation?.add(CurvedBottomNavigation.Model(2, "Favoritos", R.drawable.baseline_bookmarks_24))
        bottomNavigation?.add(CurvedBottomNavigation.Model(3, "Notícias", R.drawable.baseline_newspaper_24))
        bottomNavigation?.show(0, true)
        bottomNavigation!!.setOnShowListener { model: CurvedBottomNavigation.Model ->

            when (model.id) {
                0 -> {
                    fabsVisible(true)
                    vwMap!!.visibility =  View.VISIBLE
                    lvLayers!!.visibility = View.INVISIBLE
                    lvBookmarks!!.visibility = View.INVISIBLE
                    frOptions!!.visibility = View.INVISIBLE
                }
                1 -> {
                    fabsVisible(false)
                    vwMap!!.visibility =  View.VISIBLE
                    lvLayers!!.visibility = View.VISIBLE
                    lvBookmarks!!.visibility = View.INVISIBLE
                    frOptions!!.visibility = View.INVISIBLE
                }
                2 -> {
                    updateBookmarkList()
                    fabsVisible(false)
                    vwMap!!.visibility =  View.VISIBLE
                    lvLayers!!.visibility = View.INVISIBLE
                    lvBookmarks!!.visibility = View.VISIBLE
                    frOptions!!.visibility = View.INVISIBLE
                }
                3 -> {
                    fabsVisible(false)
                    vwMap!!.visibility =  View.INVISIBLE
                    lvLayers!!.visibility = View.INVISIBLE
                    lvBookmarks!!.visibility = View.INVISIBLE
                    frOptions!!.visibility = View.VISIBLE
                }
            }
        }
        //endregion BottomNavigation

        //region FABs

        fabCompass = findViewById<View>(R.id.fabCompass) as FloatingActionButton?
        fabCompass?.setOnClickListener {
            mMap!!.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder()
                        .target(mMap!!.cameraPosition.target)
                        .zoom(mMap!!.cameraPosition.zoom)
                        .bearing(0f)
                        .build()
                )
            )
        }

        fabBookmark = findViewById<View>(R.id.fabBookmark) as FloatingActionButton?
        fabBookmark?.setOnClickListener {
            if(addBookmarkOnNextClick == null || addBookmarkOnNextClick == false){
                fabBookmarkActive(true)
                snackbar("Selecione um local no mapa para adicioná-lo como favorito.")
            } else {
                fabBookmarkActive(false)
            }
        }

        fabSearch = findViewById<View>(R.id.fabSearch) as FloatingActionButton?
        fabSearch?.setOnClickListener {
            alertDialog("Mensagem:",
            "A funcionalidade de pesquisar por endereços no mapa está prevista " +
                    "para ser implementada na próxima versão deste aplicativo.")
        }

        fabLocation = findViewById<View>(R.id.fabLocation) as FloatingActionButton?
        fabLocation?.setOnClickListener {
            if (markerCurrentLocation != null) {
                val latLng = LatLng(
                    markerCurrentLocation!!.position.latitude,
                    markerCurrentLocation!!.position.longitude
                )
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                showInfoWindow(markerCurrentLocation!!)
            }
        }

        fabLayerMap = findViewById<View>(R.id.fabLayerMap) as FloatingActionButton?
        fabLayerMap?.setOnClickListener {
            googleMapType =
                if (googleMapType == null || googleMapType == GoogleMap.MAP_TYPE_NORMAL) {
                    GoogleMap.MAP_TYPE_HYBRID
                } else {
                    GoogleMap.MAP_TYPE_NORMAL
                }
            mMap!!.mapType = googleMapType!!
        }

        //endregion FABs

        //region Google Map
        val fMaps = supportFragmentManager.findFragmentById(R.id.fMaps) as SupportMapFragment?
        fMaps?.getMapAsync { googleMap ->
            mMap = googleMap
            mMap!!.uiSettings.isCompassEnabled = false

            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    //LatLng(-27.593839, -48.565282) //-15.797163, -47.871723
                    LatLng(-27.593839, -48.565282),
                    10f
                )
            )
            //changeMapLayer(R.raw.coleta_convencional)
            mMap!!.setOnMapClickListener { latLng ->
                if(addBookmarkOnNextClick == true){
                    bookmarkInputDialog(latLng)
                } else {
                    val markerTruck = mMap!!.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .icon(getMarkerIconFromDrawable(R.drawable.baseline_local_shipping_24))
                            .title("Caminhão " + ((100..999).random()))
                    )
                    markerGhostTrucks.add(markerTruck!!)
                }
            }
            mMap!!.setOnMapLongClickListener { latLng ->
                if(addBookmarkOnNextClick == true){
                    bookmarkInputDialog(latLng)
                }
            }
            changeMapLayer(R.raw.coleta_convencional)
        }
        //endregion Google Map

        if(getNextBookmarkId() == 0){
            writeBookmark(BookmarkData(0, R.drawable.map_pin, "Nenhum local favorito", ""))
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        getLastLocation()

    }

    //region FABs visibility

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    private fun getMarkerIconFromDrawable(drawable: Int): BitmapDescriptor {
        return getMarkerIconFromDrawable(resources.getDrawable(drawable, null))
    }

    private fun fabsVisible(visible: Boolean) {
        if (visible) {
            fabCompass!!.visibility = View.VISIBLE
            fabBookmark!!.visibility = View.VISIBLE
            fabSearch!!.visibility = View.VISIBLE
            fabLocation!!.visibility = View.VISIBLE
            fabLayerMap!!.visibility = View.VISIBLE
        } else {
            fabCompass!!.visibility = View.INVISIBLE
            fabBookmark!!.visibility = View.INVISIBLE
            fabSearch!!.visibility = View.INVISIBLE
            fabLocation!!.visibility = View.INVISIBLE
            fabLayerMap!!.visibility = View.INVISIBLE
        }
    }

    //endregion FABs visibility

    //region Snackbar / AlertDialog / bookmarkInputDialog

    private fun alertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("Ok", null)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun snackbar(message: String) {
        val snackbar = Snackbar.make(findViewById<View>(R.id.bottomNavigation), message, Snackbar.LENGTH_LONG)
        val view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackbar.show()
    }

    private fun bookmarkInputDialog(latLng: LatLng) {
        val v: View = LayoutInflater.from(this)
            .inflate(R.layout.activity_home_input_dialog, null)

        val txNome = v.findViewById<EditText>(R.id.txNome)
        txNome.hint = "Nome do local favorito"

        val alert = AlertDialog.Builder(this)
            .setView(v)
            .setTitle("Salvar local favorito:")
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .setCancelable(false)
            .create()

        alert.setOnShowListener {

            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                txNome.requestFocus()
                if (txNome.text.toString() == "") {
                    snackbar("Por favor digite um nome para o local favorito.")
                } else {
                    if(getNextBookmarkId() == 1){
                        if(getBookmark(0).title == "Nenhum local favorito"){
                            clearAllBookmarks()
                            updateBookmarkList()
                        }
                    }
                    val name = txNome.text.toString()
                    val location : String = latLng.latitude.toString() + "," + latLng.longitude.toString()
                    writeBookmark(name, location)
                    if (markerBookmarkLocation != null) { markerBookmarkLocation!!.remove() }
                    markerBookmarkLocation = mMap!!.addMarker(MarkerOptions().position(latLng).title(name))
                    fabBookmarkActive(false)
                    alert.dismiss()
                }
            }

            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                fabBookmarkActive(false)
                alert.dismiss()
            }

        }

        alert.show()
    }

    //endregion Toast / Snackbar / AlertDialog / bookmarkInputDialog

    private fun fabBookmarkActive(state: Boolean){
        if(state){
            if (layer != null) {
                layer!!.removeLayerFromMap()
                layer = null
            }
            fabBookmark?.imageTintList = ContextCompat.getColorStateList(this, R.color.red)
            addBookmarkOnNextClick = true
        } else {
            changeMapLayer(maplayer!!)
            fabBookmark?.imageTintList = ContextCompat.getColorStateList(this, R.color.mapFab)
            addBookmarkOnNextClick = false
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_CODE
            )
            return
        }

        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
            LOCATION_REFRESH_DISTANCE
        ) { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            if (markerCurrentLocation != null) {
                markerCurrentLocation!!.remove()
            }
            markerCurrentLocation =
                mMap!!.addMarker(MarkerOptions().position(latLng).title("Você está aqui!"))
            if (markerCurrentLocationFirstShow) {
                markerCurrentLocationFirstShow = false
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    fun showBottomNavigation(id: Int){
        bottomNavigation?.show(id, true)
    }

    fun changeMapLayer(rawLayer: Int) {
        maplayer = rawLayer
        try {
            if (layer != null) {
                layer!!.removeLayerFromMap()
                layer = null
            }
            if (rawLayer != 0) {
                layer = KmlLayer(mMap, rawLayer, this)
                layer!!.addLayerToMap()
                if(rawLayer == R.raw.coleta_convencional) {
                    layer!!.setOnFeatureClickListener { feature ->
                        if (feature.hasProperty("name")) {
                            if (!feature.hasProperty("description")) {
                                alertDialog(feature.getProperty("name"), "")
                            } else {
                                val name = feature.getProperty("name")
                                val descr = feature.getProperty("description").replace("<br>", "\n")
                                    .replace("<BR>", "\n")
                                alertDialog(name, descr)
                            }
                        }
                    }
                }
            }
        } catch (e: XmlPullParserException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }


    private fun updateBookmarkList(){
        listBookmarks.clear()
        val sp = getPreferences(Context.MODE_PRIVATE)
        var ct  = 0
        while (true){
            if(!sp.contains("Bookmark_$ct")){
                break
            }
            listBookmarks.add(Gson().fromJson(
                sp.getString("Bookmark_$ct", ""),
                BookmarkData::class.java)
            )
            ct++
        }
        val lvBookmarks = findViewById<ListView>(R.id.lvBookmarks)
        lvBookmarks.adapter = BookmarkAdapter(this, listBookmarks, mMap)
    }

    private fun writeBookmark(bookmarkData: BookmarkData){
        writeBookmark(Gson().toJson(bookmarkData))
    }

    private fun writeBookmark(name: String, location: String){
        writeBookmark(
            BookmarkData(
                getNextBookmarkId(),
                R.drawable.map_pin,
                name,
                location
            )
        )
    }

    private fun writeBookmark(bookmark: String){
        val sp = getPreferences(Context.MODE_PRIVATE)
        val ct = getNextBookmarkId(sp)
        with (sp.edit()) {
            putString("Bookmark_$ct", bookmark)
            apply()
        }
    }

    private fun getNextBookmarkId(): Int {
        val sp = getPreferences(Context.MODE_PRIVATE)
        return getNextBookmarkId(sp)
    }

    private fun getNextBookmarkId(sp: SharedPreferences): Int {
        var ct  = 0
        while (true){
            if(!sp.contains("Bookmark_$ct")){
                break
            }
            ct++
        }
        return ct
    }

    private fun getBookmark(key: Int): BookmarkData {
        val sp = getPreferences(Context.MODE_PRIVATE)

        val bookmarkData = Gson().fromJson(
            sp.getString("Bookmark_$key", ""),
            BookmarkData::class.java)

        return bookmarkData
    }

    private fun clearAllBookmarks(){
        val sp = getPreferences(Context.MODE_PRIVATE)
        sp.edit().clear().apply()
    }

    private fun showInfoWindow(marker: Marker){
        showInfoWindow(marker, 100)
    }

    private fun showInfoWindow(marker: Marker, delayMillis: Long){
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            handler.postDelayed ({
                if(!marker.isInfoWindowShown){
                    marker.showInfoWindow()
                }
            }, delayMillis)
        }
    }

    fun getBookmarkLocation(): Marker? {
        return this.markerBookmarkLocation
    }
    fun setBookmarkLocation(marker: Marker?) {
        this.markerBookmarkLocation = marker
    }

}