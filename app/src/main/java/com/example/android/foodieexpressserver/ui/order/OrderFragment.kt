package com.example.android.foodieexpressserver.ui.order

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.adapter.MyOrderAdapter
import com.example.android.foodieexpressserver.common.BottomSheetOrderFragment
import com.example.android.foodieexpressserver.model.eventBus.ChangeMenuClick
import com.example.android.foodieexpressserver.model.eventBus.LoadOrderEvent
import com.example.android.foodieexpressserver.ui.foodlist.FoodListViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class OrderFragment : Fragment()  {


    lateinit var recycler_order : RecyclerView
    lateinit var txt_order_filter : TextView
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
                txt_order_filter.setText(StringBuilder("Orders (")
                    .append(orderList.size)
                    .append(")"))
            }
        })
        return root
    }

    private fun initViews(root: View?) {

        setHasOptionsMenu(true)
        txt_order_filter = root!!.findViewById(R.id.txt_order_filter) as TextView
        recycler_order = root!!.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        recycler_order.layoutManager = LinearLayoutManager(context)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_list_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_filter){
            val bottomSheet = BottomSheetOrderFragment.instance
            bottomSheet!!.show(activity!!.supportFragmentManager,"OrderList")
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

    }

    override fun onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent::class.java))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent::class.java)
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onLoadOrder(event: LoadOrderEvent) {
        orderViewModel.loadOrder(event.status)
    }


}