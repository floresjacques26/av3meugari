package com.coding.meugari.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.coding.meugari.HomeActivity
import com.coding.meugari.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.qamar.curvedbottomnaviagtion.log
import java.util.concurrent.Executors


class BookmarkAdapter(
    val context : Context,
    val myDataList: MutableList<BookmarkData>,
    val mMap: GoogleMap?
) : BaseAdapter() {

    override fun getCount(): Int {
        return myDataList.size
    }

    override fun getItem(position: Int): BookmarkData {
        return myDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var mView = convertView
        if(mView == null){
            mView = LayoutInflater.from(context).inflate(R.layout.activity_home_listitem_bookmarks, parent, false)
        }

        mView?.setOnClickListener{
            val selectedItem = getItem(position) as BookmarkData
            val ctx = context as HomeActivity
            if(selectedItem.location.isEmpty()){
                return@setOnClickListener
            }

            val getLatLng = selectedItem.location.split(",")
            val selectedItemPosition = LatLng(getLatLng[0].toDouble(), getLatLng[1].toDouble())

            var markerBookmarkLocation : Marker? = ctx.getBookmarkLocation()

            if (markerBookmarkLocation != null) {
                log("marker null")

                val exists = (markerBookmarkLocation.position == selectedItemPosition)

                markerBookmarkLocation.remove()
                (context as HomeActivity).setBookmarkLocation(null)

                if(exists){
                    log("marker exists")
                    return@setOnClickListener
                }

            }

            log("marker add")
            ctx.setBookmarkLocation(
                mMap!!.addMarker(
                    MarkerOptions().position(selectedItemPosition).title(selectedItem.title)
                )
            )

            mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder()
                        .target(selectedItemPosition)
                        .zoom(mMap.cameraPosition.zoom)
                        .build()
                )
            )
            showInfoWindow(ctx, 100)

        }

        val lvBookmarksImDelete = mView?.findViewById<ImageButton>(R.id.list_item_delete)
        lvBookmarksImDelete?.setOnClickListener {

            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Apagar local favorito")
            builder.setMessage(
                "Tem certeza de que deseja remover o local favorito '" +
                    getItem(position).title + "'?")

            builder.setNegativeButton("Não") { dialog, which ->

            }

            builder.setPositiveButton("Sim") { dialog, which ->

                val bookmarkData = getItem(position)

                val sp = (context as Activity).getPreferences(Context.MODE_PRIVATE)
                sp.edit().remove("Bookmark_${bookmarkData.code}").apply()

                myDataList.remove(bookmarkData)
                this.notifyDataSetChanged()

            }

            builder.show()

        }

        val image = mView?.findViewById<ImageView>(R.id.list_item_image)
        val title = mView?.findViewById<TextView>(R.id.list_item_title)
        val location = mView?.findViewById<TextView>(R.id.list_item_location)

        val currentItem = getItem(position)
        image?.setImageResource(currentItem.image)
        title?.text = currentItem.title
        location?.text = currentItem.location

        return  mView!!
    }

    private fun showInfoWindow(context: HomeActivity, delayMillis: Long){
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            handler.postDelayed ({
                val markerBookmarkLocation : Marker? = context.getBookmarkLocation()
                if(!markerBookmarkLocation?.isInfoWindowShown!!){
                    markerBookmarkLocation.showInfoWindow()
                }
            }, delayMillis)
        }
    }

}