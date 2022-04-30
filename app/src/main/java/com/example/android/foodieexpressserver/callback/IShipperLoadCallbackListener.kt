package com.example.android.foodieexpressserver.callback

import android.app.AlertDialog
import android.provider.MediaStore
import android.widget.Button
import android.widget.RadioButton
import com.example.android.foodieexpressserver.model.CategoryModel
import com.example.android.foodieexpressserver.model.OrderModel
import com.example.android.foodieexpressserver.model.ShipperModel

interface IShipperLoadCallbackListener {
    fun onShipperLoadSuccess(shippersList:List <ShipperModel>)
    fun onShipperLoadSuccess(pos:Int, orderModel: OrderModel?,
                             shippersList:List <ShipperModel>,
    dialog: AlertDialog?,ok:Button?, cancel: Button?,
    rdi_shipping:RadioButton?,rdi_shipped: RadioButton?,
    rdi_cancelled:RadioButton?,rdi_delete:RadioButton?,
    rdi_restore_placed:RadioButton?)

    fun onShipperLoadFailed(message:String)
}