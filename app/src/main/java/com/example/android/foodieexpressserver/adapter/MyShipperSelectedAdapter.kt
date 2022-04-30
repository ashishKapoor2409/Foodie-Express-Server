package com.example.android.foodieexpressserver.adapter

import android.content.Context
import android.media.Image
import android.media.metrics.Event
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.callback.IRecyclerItemClickListener
import com.example.android.foodieexpressserver.model.CategoryModel
import com.example.android.foodieexpressserver.model.ShipperModel
import com.example.android.foodieexpressserver.model.eventBus.UpdateActiveEvent
import org.greenrobot.eventbus.EventBus

class MyShipperSelectedAdapter(internal var context: Context,
                       internal var shipperList : List<ShipperModel>):
    RecyclerView.Adapter<MyShipperSelectedAdapter.MyViewHolder>(){

    var lastCheckedImageView :ImageView? = null
    var selectedShipper: ShipperModel?=null
        private set


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var txt_name: TextView? = null
        var txt_phone: TextView? = null
        var img_checked: ImageView? = null

        var iRecyclerItemClickListener: IRecyclerItemClickListener? = null

        fun setClick(iRecyclerItemClickListener: IRecyclerItemClickListener) {
            this.iRecyclerItemClickListener = iRecyclerItemClickListener
        }
        internal var listener: IRecyclerItemClickListener?=null

        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }
        init {
            txt_name = itemView.findViewById(R.id.txt_name) as TextView
            txt_phone = itemView.findViewById(R.id.txt_phone) as TextView
            img_checked = itemView.findViewById(R.id.img_checked) as ImageView
            itemView.setOnClickListener(this)
        }

        override fun onClick(view : View?) {
            iRecyclerItemClickListener!!.onItemClick(view!!,adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected,parent,false)
        return MyViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.setText(shipperList[position].name)
        holder.txt_phone!!.setText(shipperList[position].phone)
        holder.setClick(object : IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                if(lastCheckedImageView != null) {
                    lastCheckedImageView!!.setImageResource(0)
                }
                holder.img_checked!!.setImageResource(R.drawable.ic_baseline_check_24)
                lastCheckedImageView = holder.img_checked
                selectedShipper = shipperList[pos]

            }
        })

    }

    override fun getItemCount(): Int {
        return shipperList.size
    }
}