package com.coding.meugari.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.coding.meugari.HomeActivity
import com.coding.meugari.R


class LayerAdapter(
    val context : Context,
    val myDataList: MutableList<LayerData>
) : BaseAdapter() {

    override fun getCount(): Int {
        return myDataList.size
    }

    override fun getItem(position: Int): LayerData {
        return myDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var mView = convertView
        if(mView == null){
            mView = LayoutInflater.from(context).inflate(R.layout.activity_home_listitem_layers, parent, false)
        }

        mView?.setOnClickListener{
            changeLayer(context as HomeActivity, getItem(position).code)
        }

        val ibNavigate = mView?.findViewById<ImageButton>(R.id.list_item_navigate)
        ibNavigate?.setOnClickListener {
            val ctx = context as HomeActivity
            changeLayer(ctx, getItem(position).code)
            ctx.showBottomNavigation(0)
        }

        val image = mView?.findViewById<ImageView>(R.id.list_item_image)
        val name = mView?.findViewById<TextView>(R.id.list_item_name)
        val descr = mView?.findViewById<TextView>(R.id.list_item_descr)

        val currentItem = getItem(position)
        image?.setImageResource(currentItem.image)
        name?.text = currentItem.name
        descr?.text = currentItem.description

        return  mView!!
    }

    private fun changeLayer(context: HomeActivity, item: Int) {
        when (item){
            0 -> { context.changeMapLayer(R.raw.coleta_convencional) }
            1 -> { context.changeMapLayer(R.raw.coleta_seletiva) }
            2 -> { context.changeMapLayer(R.raw.coleta_flex_vidros) }
            3 -> { context.changeMapLayer(R.raw.coleta_flex_organicos) }
            4 -> { context.changeMapLayer(0) }
        }
    }
}