package com.example.android.foodieexpressserver.ui.shipper

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpressserver.callback.ICategoryCallBackListener
import com.example.android.foodieexpressserver.callback.IShipperLoadCallbackListener
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.model.CategoryModel
import com.example.android.foodieexpressserver.model.ShipperModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ShipperViewModel : ViewModel(), IShipperLoadCallbackListener {

    private var shipperListMutable : MutableLiveData<List<ShipperModel>>? = null
    private var messageError : MutableLiveData<String> = MutableLiveData()
    private val shipperCallBackListener : IShipperLoadCallbackListener

    init {
        shipperCallBackListener = this
    }

    fun getShipperList() : MutableLiveData<List<ShipperModel>> {
        if(shipperListMutable == null) {
            shipperListMutable = MutableLiveData()
            loadShipper()
        }
        return shipperListMutable!!

    }

    private fun loadShipper() {
        val tempList = ArrayList<ShipperModel>()
        val shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
        shipperRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for(itemSnapshot in p0!!.children) {
                    val model = itemSnapshot.getValue<ShipperModel>(ShipperModel::class.java)
                    model!!.key = itemSnapshot.key
                    tempList.add(model)
                }
                shipperCallBackListener.onShipperLoadSuccess(tempList)
            }

            override fun onCancelled(p0: DatabaseError) {
                shipperCallBackListener.onShipperLoadFailed(p0.message)
            }


        })
    }

    fun getMessageError(): MutableLiveData<String>{
        return messageError
    }

    override fun onShipperLoadSuccess(shippersList: List<ShipperModel>) {
        shipperListMutable!!.value = shippersList

    }

    override fun onShipperLoadFailed(message: String) {
        messageError.value = message

    }

}