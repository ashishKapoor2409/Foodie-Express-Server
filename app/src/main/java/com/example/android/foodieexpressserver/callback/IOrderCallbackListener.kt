package com.example.android.foodieexpressserver.callback

import com.example.android.foodieexpressserver.model.CategoryModel
import com.example.android.foodieexpressserver.model.OrderModel

interface IOrderCallbackListener {
    fun onOrderLoadSuccess(orderList:List <OrderModel>)
    fun onOrderLoadFailed(message:String)
}