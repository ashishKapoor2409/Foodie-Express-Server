package com.example.android.foodieexpressserver.ui.order

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
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
import com.example.android.foodieexpressserver.SizeAddonEditActivity
import com.example.android.foodieexpressserver.adapter.MyOrderAdapter
import com.example.android.foodieexpressserver.callback.IMyButtonCallback
import com.example.android.foodieexpressserver.common.BottomSheetOrderFragment
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.common.MySwipeHelper
import com.example.android.foodieexpressserver.model.eventBus.AddonSizeEditEvent
import com.example.android.foodieexpressserver.model.eventBus.ChangeMenuClick
import com.example.android.foodieexpressserver.model.eventBus.LoadOrderEvent
import com.example.android.foodieexpressserver.ui.foodlist.FoodListViewModel
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
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
        orderViewModel =  ViewModelProvider(this).get(OrderViewModel::class.java)
        orderViewModel.messageError.observe(this, Observer { s->
            Toast.makeText(context,s, Toast.LENGTH_SHORT).show()
        })
        orderViewModel!!.getOrderModelList().observe(this, Observer { orderList ->
            if(orderList!=null ){
                adapter = MyOrderAdapter(context!!,orderList.toMutableList())
                recycler_order.adapter = adapter
                recycler_order.layoutAnimation = layoutAnimationController
                txt_order_filter.setText(StringBuilder("Orders (")
                    .append(orderList.size)
                    .append(")"))
            }
        })
        initViews(root)
        return root
    }

    private fun initViews(root: View?) {

        setHasOptionsMenu(true)
        txt_order_filter = root!!.findViewById(R.id.txt_order_filter) as TextView
        recycler_order = root!!.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        recycler_order.layoutManager = LinearLayoutManager(context)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        val swipe = object : MySwipeHelper(context!!, recycler_order!!, width/6) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(
                    MyButton(context!!,
                        "Directions",
                        30,
                        0,
                        Color.parseColor("#9b0000"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {

                            }
                        })
                )

                buffer.add(
                    MyButton(context!!,
                        "Call",
                        30,
                        0,
                        Color.parseColor("#560027"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {

                                Dexter.withContext(activity)
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(object: PermissionListener{
                                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                                            val orderModel = adapter!!.getItemAtPosition(pos)
                                            val intent = Intent()
                                            intent.setAction(Intent.ACTION_DIAL)
                                            intent.setData(Uri.parse(java.lang.StringBuilder("tel: ")
                                                .append(orderModel.userPhone).toString()))
                                            startActivity(intent)
                                        }

                                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                                            Toast.makeText(context,"You must accept this permission "
                                                    +p0!!.permissionName,
                                            Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onPermissionRationaleShouldBeShown(
                                            p0: PermissionRequest?,
                                            p1: PermissionToken?
                                        ) {
                                        }

                                    }).check()


                            }
                        })
                )

                buffer.add(
                    MyButton(context!!,
                        "Remove",
                        30,
                        0,
                        Color.parseColor("#12005e"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                val orderModel = adapter!!.getItemAtPosition(pos)
                                val builder = AlertDialog.Builder(context!!)
                                    .setTitle("Delete")
                                    .setMessage("Do you really want to delete this order?")
                                    .setNegativeButton("CANCEL"){dialogInterface, i -> dialogInterface.dismiss() }
                                    .setPositiveButton("DELETE"){dialogInterface, i ->
                                    FirebaseDatabase.getInstance()
                                        .getReference()
                                        .child(orderModel.key!!)
                                        .removeValue()
                                        .addOnFailureListener {
                                            Toast.makeText(context!!,""+it.message,Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnSuccessListener {
                                            adapter!!.removeItem(pos)
                                            adapter!!.notifyItemRemoved(pos)
                                            txt_order_filter.setText(StringBuilder("Orders (")
                                                .append(adapter!!.itemCount)
                                                .append(")"))
                                            dialogInterface.dismiss()
                                            Toast.makeText(context!!,"Order has been deleted",Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                val dialog = builder.create()
                                dialog.show()

                                val btn_negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                                btn_negative.setTextColor(Color.LTGRAY)
                                val btn_positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                btn_positive.setTextColor(Color.RED)


                            }
                        })
                )

                buffer.add(
                    MyButton(context!!,
                        "Edit",
                        30,
                        0,
                        Color.parseColor("#333639"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                            }
                        })
                )
            }

        }
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