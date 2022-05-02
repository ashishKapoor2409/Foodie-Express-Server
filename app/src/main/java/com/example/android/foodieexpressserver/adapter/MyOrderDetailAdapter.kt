package com.example.android.foodieexpressserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.model.AddonModel
import com.example.android.foodieexpressserver.model.CartItem
import com.example.android.foodieexpressserver.model.SizeModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MyOrderDetailAdapter(internal var context: Context,
internal var cartItemList: MutableList<CartItem>):RecyclerView.Adapter<MyOrderDetailAdapter.MyViewHolder>()  {


    val gson:Gson = Gson()

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var txt_food_name:TextView? = null
        var txt_food_size:TextView? = null
        var txt_food_addon:TextView? = null
        var txt_food_quantity:TextView? = null
        var img_food_image: ImageView? = null

        init {
            img_food_image = itemView.findViewById(R.id.img_food_image) as ImageView
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            txt_food_size = itemView.findViewById(R.id.txt_food_size) as TextView
            txt_food_addon = itemView.findViewById(R.id.txt_food_addon) as TextView
            txt_food_quantity = itemView.findViewById(R.id.txt_food_quantity) as TextView
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_order_detail_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(cartItemList[position].foodImage)
            .into(holder.img_food_image!!)
        holder.txt_food_name!!.setText(StringBuilder().append(cartItemList[position].foodName))
        val sizeModel:SizeModel = gson.fromJson(cartItemList[position].foodSize,
            object : TypeToken<SizeModel?>(){}.type)
        if(sizeModel != null) holder.txt_food_size!!.setText(StringBuilder("Size:").append(sizeModel.name))
        if(!cartItemList[position].foodAddOn.equals("Default")) {

            val  addonModels: List<AddonModel> = gson.fromJson(cartItemList[position].foodAddOn,
            object : TypeToken<List<AddonModel?>?>(){}.type)

            val addOnString = java.lang.StringBuilder()
            if(addonModels != null) {
                for(addonModel in addonModels) addOnString.append(addonModel.name).append(",")
                addOnString.delete(addOnString.length-1,addOnString.length)
                holder.txt_food_addon!!.setText(java.lang.StringBuilder("AddOn:").append(addOnString))


            }

        } else {
            holder!!.txt_food_addon!!.setText(StringBuilder("Addon: Default"))
        }


    }

    override fun getItemCount(): Int {
        return cartItemList.size
    }

}