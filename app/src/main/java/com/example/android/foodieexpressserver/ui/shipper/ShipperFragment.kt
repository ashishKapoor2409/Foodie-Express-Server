package com.example.android.foodieexpressserver.ui.shipper

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.media.metrics.Event
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.adapter.MyCategoriesAdapter
import com.example.android.foodieexpressserver.adapter.MyShipperAdapter
import com.example.android.foodieexpressserver.ui.category.CategoryViewModel
import dmax.dialog.SpotsDialog
import androidx.lifecycle.Observer
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.model.FoodModel
import com.example.android.foodieexpressserver.model.ShipperModel
import com.example.android.foodieexpressserver.model.eventBus.ChangeMenuClick
import com.example.android.foodieexpressserver.model.eventBus.UpdateActiveEvent
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ShipperFragment : Fragment() {

    companion object {
        fun newInstance() = ShipperFragment()
    }

    private lateinit var shipperViewModel: ShipperViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MyShipperAdapter? = null
    private var recyclerView : RecyclerView? = null
    internal var shipperModels: List<ShipperModel> = ArrayList<ShipperModel>()
    internal var saveBeforeSearchList : List<ShipperModel> = ArrayList<ShipperModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.shipper_fragment, container, false)
        shipperViewModel =
            ViewModelProvider(this).get(ShipperViewModel::class.java)
        initViews(itemView)
        shipperViewModel.getMessageError().observe(this, Observer {
            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        })
        shipperViewModel.getShipperList().observe(this, Observer {
            dialog.dismiss()
            shipperModels = it
            if(saveBeforeSearchList.size == 0)
                saveBeforeSearchList = it
            adapter = MyShipperAdapter(context!!, shipperModels)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutAnimation = layoutAnimationController
        })
        return itemView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu,menu)
        //Create search view
        val menuItem = menu.findItem(R.id.action_search)

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName!!))
        //Event
        searchView.setOnQueryTextListener(object:androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(search: String?): Boolean {
                startSearchFood(search!!)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })
        //Clear text when click to clear button
        val closeButton = searchView.findViewById<View>(R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener {
            val ed = searchView.findViewById<View>(R.id.search_src_text) as EditText
            //Clear text
            ed.setText("")
            //Clear query
            searchView.setQuery("",false)
            //Collapse the action query
            searchView.onActionViewCollapsed()
            //Collapse the saerch widget
            menuItem.collapseActionView()
            //Restore result to original
            shipperViewModel.getShipperList().value = saveBeforeSearchList

        }

    }

    private fun startSearchFood(s: String) {
        val resultShipper: MutableList<ShipperModel> = ArrayList()
        for(i in shipperModels.indices) {
            val shipperModel = shipperModels[i]
            if(shipperModel.phone!!.toLowerCase().contains(s!!.toLowerCase())) {
                resultShipper.add(shipperModel)
            }
        }
        shipperViewModel!!.getShipperList().value = resultShipper

    }

    private fun initViews(root: View) {
        dialog = SpotsDialog.Builder().setContext(context)
            .setCancelable(false).build()
        dialog.show()
        recyclerView = root.findViewById(R.id.recycler_shipper)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recyclerView!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))
    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateActiveEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateActiveEvent::class.java)
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUpdateActiveEvent(updateActiveEvent: UpdateActiveEvent) {
        val updateData = HashMap<String,Any>()
        updateData.put("active",updateActiveEvent.active)
        FirebaseDatabase.getInstance()
            .getReference(Common.SHIPPER_REF)
            .child(updateActiveEvent.shipperModel!!.key!!)
            .updateChildren(updateData)
            .addOnFailureListener { e->
                Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener { avoid ->
                Toast.makeText(context,"Update state to"+updateActiveEvent.active,Toast.LENGTH_SHORT).show()
            }
    }

}