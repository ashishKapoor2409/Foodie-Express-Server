package com.example.android.foodieexpressserver.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.adapter.MyOrderAdapter
import com.example.android.foodieexpressserver.ui.foodlist.FoodListViewModel

class OrderFragment : Fragment()  {


    lateinit var recycler_order : RecyclerView
    lateinit var layoutAnimationController : LayoutAnimationController
    private var adapter : MyOrderAdapter ? = null

    lateinit private var orderViewModel: OrderViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order,container, false)
        initViews(root)
        orderViewModel =  ViewModelProvider(this).get(OrderViewModel::class.java)
        orderViewModel.messageError.observe(this, Observer { s->
            Toast.makeText(context,s, Toast.LENGTH_SHORT).show()
        })
        orderViewModel!!.getOrderModelList().observe(this, Observer { orderList ->
            if(orderList!=null ){
                adapter = MyOrderAdapter(context!!,orderList)
                recycler_order.adapter = adapter
                recycler_order.layoutAnimation = layoutAnimationController
            }
        })
        return root
    }

    private fun initViews(root: View?) {
        recycler_order = root!!.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        recycler_order.layoutManager = LinearLayoutManager(context)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)

    }
}