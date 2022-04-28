package com.example.android.foodieexpressserver.ui.order

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.Remote.IFCMService
import com.example.android.foodieexpressserver.Remote.RetrofitFCMClient
import com.example.android.foodieexpressserver.SizeAddonEditActivity
import com.example.android.foodieexpressserver.adapter.MyOrderAdapter
import com.example.android.foodieexpressserver.callback.IMyButtonCallback
import com.example.android.foodieexpressserver.common.BottomSheetOrderFragment
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.common.MySwipeHelper
import com.example.android.foodieexpressserver.model.FCMResponse
import com.example.android.foodieexpressserver.model.FCMSendData
import com.example.android.foodieexpressserver.model.OrderModel
import com.example.android.foodieexpressserver.model.TokenModel
import com.example.android.foodieexpressserver.model.eventBus.AddonSizeEditEvent
import com.example.android.foodieexpressserver.model.eventBus.ChangeMenuClick
import com.example.android.foodieexpressserver.model.eventBus.LoadOrderEvent
import com.example.android.foodieexpressserver.ui.foodlist.FoodListViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.create

class OrderFragment : Fragment()  {


    private val compositeDisposable = CompositeDisposable()
    lateinit var ifcmService: IFCMService
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
                updateTextCounter()
            }
        })
        initViews(root)
        return root
    }

    private fun initViews(root: View?) {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService::class.java)

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
                                            updateTextCounter()
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
                                showEditDialog(adapter!!.getItemAtPosition(pos),pos)
                            }
                        })
                )
            }

        }
    }

    private fun showEditDialog(orderModel: OrderModel, pos: Int) {
        var layout_dialog: View?=null
        var builder:AlertDialog.Builder?=null

        var rdi_shipping: RadioButton? = null
        var rdi_shipped: RadioButton? = null
        var rdi_cancelled: RadioButton? = null
        var rdi_restore_placed: RadioButton? = null
        var rdi_delete: RadioButton? = null

        if(orderModel.orderStatus == -1) {
            layout_dialog = LayoutInflater.from(context!!)
                .inflate(R.layout.layout_dialog_cancelled,null)
            builder = AlertDialog.Builder(context!!)
                .setView(layout_dialog)
            rdi_restore_placed = layout_dialog.findViewById<View>(R.id.rdi_restore_placed) as RadioButton
            rdi_delete = layout_dialog.findViewById<View>(R.id.rdi_delete) as RadioButton
        } else if(orderModel.orderStatus == 0) {
            layout_dialog = LayoutInflater.from(context!!)
                .inflate(R.layout.layout_dialog_shipping,null)
            builder = AlertDialog.Builder(context!!,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(layout_dialog)
            rdi_shipping = layout_dialog.findViewById<View>(R.id.rdi_shipping) as RadioButton
            rdi_cancelled = layout_dialog.findViewById<View>(R.id.rdi_cancelled) as RadioButton
        }
        else {
                layout_dialog = LayoutInflater.from(context!!)
                    .inflate(R.layout.layout_dialog_shipping,null)
                builder = AlertDialog.Builder(context!!,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                    .setView(layout_dialog)
            rdi_shipped = layout_dialog.findViewById<View>(R.id.rdi_shipped) as RadioButton
            rdi_cancelled = layout_dialog.findViewById<View>(R.id.rdi_cancelled) as RadioButton
        }
        val btn_ok = layout_dialog.findViewById<View>(R.id.btn_ok) as Button
        val btn_cancel = layout_dialog.findViewById<View>(R.id.btn_cancel) as Button

        val txt_status = layout_dialog.findViewById<View>(R.id.txt_status) as TextView

        txt_status.setText(java.lang.StringBuilder("Order Status(")
            .append(Common.convertStatusToString(orderModel.orderStatus))
            .append(")"))

        val dialog = builder.create()
        dialog.show()
        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
        btn_ok.setOnClickListener {
            dialog.dismiss()
                if(rdi_cancelled != null && rdi_cancelled.isChecked) {
                    updateOrder(pos,orderModel,-1)
                } else if(rdi_shipping != null && rdi_shipping.isChecked) {
                    updateOrder(pos,orderModel,1)
                } else if(rdi_shipped != null && rdi_shipped.isChecked) {
                    updateOrder(pos,orderModel,2)
                } else if(rdi_restore_placed != null && rdi_restore_placed.isChecked) {
                    updateOrder(pos,orderModel,0)
                } else if(rdi_delete != null && rdi_delete.isChecked) {
                    deleteOrder(pos,orderModel,0)
                }
        }







    }

    private fun deleteOrder(pos: Int, orderModel: OrderModel, status: Int) {

        if(!TextUtils.isEmpty(orderModel.key)) {

            FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(orderModel.key!!)
                .removeValue()
                .addOnFailureListener { throwable ->  Toast.makeText(context!!,""+throwable.message,
                    Toast.LENGTH_SHORT).show() }
                .addOnSuccessListener {
                    adapter!!.removeItem(pos)
                    adapter!!.notifyItemRemoved(pos)
                    updateTextCounter()
                    Toast.makeText(context!!,"Update Order Success!!",
                        Toast.LENGTH_SHORT).show()

                }
        } else {
            Toast.makeText(context!!,"Order number must not be null or empty",Toast.LENGTH_SHORT).show()
        }


    }

    private fun updateOrder(pos: Int, orderModel: OrderModel, status: Int) {
        if(!TextUtils.isEmpty(orderModel.key)) {
            val update_data = HashMap<String,Any>()
            update_data.put("orderStatus",status)

            FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(orderModel.key!!)
                .updateChildren(update_data)
                .addOnFailureListener { throwable ->  Toast.makeText(context!!,""+throwable.message,
                Toast.LENGTH_SHORT).show() }
                .addOnSuccessListener {


                    val dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
                    dialog.show()

                    FirebaseDatabase.getInstance()
                        .getReference(Common.TOKEN_REF)
                        .child(orderModel.userId!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()) {

                                    val tokenModel = snapshot.getValue(TokenModel::class.java)
                                    val notiData = HashMap<String,String>()
                                    notiData.put(Common.NOTI_TITLE,"Your order was update")
                                    notiData.put(Common.NOTI_CONTENT,StringBuilder("Your order")
                                        .append(orderModel.key)
                                        .append(" was update to ")
                                        .append(Common.convertStatusToString(status)).toString())

                                    val sendData = FCMSendData(tokenModel!!.token!!,notiData)
                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ fcmResponse ->
                                            dialog.dismiss()
                                            if (fcmResponse.success == 1) {
                                                Toast.makeText(
                                                    context!!,
                                                    "Update order Successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context!!,
                                                    "Failed to send notification",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },{t: Throwable? ->
                                            Toast.makeText(context!!,"Order was sent but notification failed",Toast.LENGTH_SHORT).show()
                                        })
                                    )


                                } else {
                                    dialog.dismiss()
                                    Toast.makeText(context,"Token not found",Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                dialog.dismiss()
                                Toast.makeText(context,""+error.message,Toast.LENGTH_SHORT).show()
                            }

                        })
                    adapter!!.removeItem(pos)
                    adapter!!.notifyItemRemoved(pos)
                    updateTextCounter()


                }
        } else {
            Toast.makeText(context!!,"Order number must not be null or empty",Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateTextCounter() {
        txt_order_filter.setText(StringBuilder("Orders (")
            .append(adapter!!.itemCount)
            .append(")"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_list_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_filter){
            val bottomSheet = BottomSheetOrderFragment.instance
            bottomSheet!!.show(activity!!.supportFragmentManager,"OrderList")
            return true
        }
        else
            return super.onOptionsItemSelected(item)
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