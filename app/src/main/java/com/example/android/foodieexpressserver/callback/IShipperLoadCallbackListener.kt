package com.example.android.foodieexpressserver.callback

import com.example.android.foodieexpressserver.model.CategoryModel
import com.example.android.foodieexpressserver.model.ShipperModel

interface IShipperLoadCallbackListener {
    fun onShipperLoadSuccess(shippersList:List <ShipperModel>)
    fun onShipperLoadFailed(message:String)
}