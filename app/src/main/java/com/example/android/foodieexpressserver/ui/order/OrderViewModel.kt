package com.example.android.foodieexpressserver.ui.order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpressserver.callback.IOrderCallbackListener
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.model.FoodModel
import com.example.android.foodieexpressserver.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class OrderViewModel : ViewModel(), IOrderCallbackListener {

    private val orderModelList = MutableLiveData<List<OrderModel>>()
    val messageError = MutableLiveData<String>()
    private val orderCallbackListener: IOrderCallbackListener

    init {
        orderCallbackListener = this
    }

    fun getOrderModelList():MutableLiveData<List<OrderModel>>{
        loadOrder(0)
        return orderModelList
    }

    fun loadOrder(status: Int) {
        val tempList : MutableList<OrderModel> = ArrayList()
        val orderRef = FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .orderByChild("orderStatus")
            .equalTo(status.toDouble())
        orderRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(itemSnapShot in snapshot.children) {
                    val orderModel = itemSnapShot.getValue(OrderModel::class.java)
                    orderModel!!.key = itemSnapShot.key
                    tempList.add(orderModel)
                }
                orderCallbackListener.onOrderLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                orderCallbackListener.onOrderLoadFailed(error.message)
            }

        })

    }

    override fun onOrderLoadSuccess(orderList: List<OrderModel>) {
        if(orderList.size >= 0) {
            Collections.sort(orderList){t1,t2 ->
                if(t1.createDate < t2.createDate) return@sort -1
                if(t1.createDate == t2.createDate) 0 else 1
            }
            orderModelList.value = orderList
        }
    }

    override fun onOrderLoadFailed(message: String) {
        messageError.value = message
    }

}