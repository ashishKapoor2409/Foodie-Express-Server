package com.example.android.foodieexpressserver.ui.foodlist

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.SizeAddonEditActivity
import com.example.android.foodieexpressserver.adapter.MyFoodListAdapter
import com.example.android.foodieexpressserver.callback.IMyButtonCallback
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.common.MySwipeHelper
import com.example.android.foodieexpressserver.model.FoodModel
import com.example.android.foodieexpressserver.model.eventBus.AddonSizeEditEvent
import com.example.android.foodieexpressserver.model.eventBus.ChangeMenuClick
import com.example.android.foodieexpressserver.model.eventBus.ToastEvent
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FoodListFragment : Fragment() {

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST: Int = 1234
    private lateinit var foodListViewModel: FoodListViewModel

    var recycler_food_list : RecyclerView?=null
    var layoutAnimationController : LayoutAnimationController? = null

    var adapter : MyFoodListAdapter? = null
    var foodModelList : List<FoodModel> = ArrayList<FoodModel>()

    private var img_food:ImageView?=null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var dialog: AlertDialog

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodListViewModel =
            ViewModelProvider(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list,container, false)

        initViews(root)
        foodListViewModel.getMutableFoodModelListData().observe(this, Observer{
            if(it != null) {
                foodModelList = it!!
                adapter = MyFoodListAdapter(context!!, foodModelList)
                recycler_food_list!!.adapter = adapter
                recycler_food_list!!.layoutAnimation = layoutAnimationController
            }
        })
        return root
    }

    private fun initViews(root: View?) {

        dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        recycler_food_list = root!!.findViewById(R.id.recycler_food_list) as RecyclerView
        recycler_food_list!!.setHasFixedSize(true)
        recycler_food_list!!.layoutManager = LinearLayoutManager(context)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        val swipe = object : MySwipeHelper(context!!, recycler_food_list!!, width/6) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(
                    MyButton(context!!,
                        "Delete",
                        30,
                        0,
                        Color.parseColor("#9b0000"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                Common.foodSelected = foodModelList[pos]
                                val builder = AlertDialog.Builder(context!!)
                                builder.setTitle("Delete")
                                    .setMessage("Do you really want to delete food ?")
                                    .setNegativeButton("CANCEL",{dialogInterface, _ ->
                                        dialogInterface.dismiss()
                                    })
                                    .setPositiveButton("DELETE",{dialogInterface, i->
                                        Common.categorySelected!!.foods!!.removeAt(pos)
                                        updateFood(Common.categorySelected!!.foods,true)
                                    })
                                val deleteDialog = builder.create()
                                deleteDialog.show()

                            }
                        })
                )

                buffer.add(
                    MyButton(context!!,
                        "Update",
                        30,
                        0,
                        Color.parseColor("#560027"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                showUpdateDialog(pos)
                            }
                        })
                )

                buffer.add(
                    MyButton(context!!,
                        "Size",
                        30,
                        0,
                        Color.parseColor("#12005e"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                Common.foodSelected = foodModelList!![pos]
                                startActivity(Intent(context!!,SizeAddonEditActivity::class.java))
                                EventBus.getDefault().postSticky(AddonSizeEditEvent(false,pos))
                            }
                        })
                )

                buffer.add(
                    MyButton(context!!,
                        "Addon",
                        30,
                        0,
                        Color.parseColor("#333639"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                Common.foodSelected = foodModelList!![pos]
                                startActivity(Intent(context!!,SizeAddonEditActivity::class.java))
                                EventBus.getDefault().postSticky(AddonSizeEditEvent(true,pos))
                            }
                        })
                )
            }

        }


    }

    private fun showUpdateDialog(pos: Int) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Update")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context!!).inflate(R.layout.layout_update_food,null)
        val edt_food_name = itemView.findViewById<View>(R.id.edt_food_name) as EditText
        val edt_food_price = itemView.findViewById<View>(R.id.edt_food_price) as EditText
        val edt_food_description = itemView.findViewById<View>(R.id.edt_food_description) as EditText
        img_food = itemView.findViewById<View>(R.id.img_food_image) as ImageView

        edt_food_name.setText(StringBuilder("").append(Common.categorySelected!!.foods!![pos].name))
        edt_food_price.setText(StringBuilder("").append(Common.categorySelected!!.foods!![pos].price))
        edt_food_description.setText(StringBuilder("").append(Common.categorySelected!!.foods!![pos].description))

        Glide.with(context!!).load(Common.categorySelected!!.foods!![pos].image).into(img_food!!)
        img_food!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST)
        }

        builder.setNegativeButton("CANCEL",{dialogInterface,_ ->
            dialogInterface.dismiss()
        })

        builder.setPositiveButton("UPDATE") { dialogInterface, _ ->
            val updateFood = Common.categorySelected!!.foods!![pos]
            updateFood.name = edt_food_name.text.toString()
            updateFood.price = if (TextUtils.isEmpty(edt_food_price.text))
                0
            else
                edt_food_price.text.toString().toLong()
            updateFood.description = edt_food_description.text.toString()

            if (imageUri != null) {
                dialog.setMessage("Uploading....")
                dialog.show()

                val imageName = UUID.randomUUID().toString()
                val imageFolder = storageReference.child("images/$imageName")
                Log.d("Hello123",imageFolder.toString())
                imageFolder.putFile(imageUri!!)
                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Toast.makeText(context!!, "" + e.message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress =
                            100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Uploaded $progress%")
                    }
                    .addOnSuccessListener { taskSnapshot ->
                        dialog.dismiss()
                        imageFolder.downloadUrl.addOnSuccessListener { uri ->
                            dialog.dismiss()
                            updateFood.image = uri.toString()
                            Common.categorySelected!!.foods!![pos] = updateFood
                            updateFood(Common.categorySelected!!.foods!!, false)
                        }
                    }

            } else {
                Common.categorySelected!!.foods!![pos] = updateFood
                updateFood(Common.categorySelected!!.foods!!, false)
            }
        }
        builder.setView(itemView)
        val updateDialog = builder.create()
        updateDialog.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.data != null) {
                imageUri = data.data
                img_food!!.setImageURI(imageUri)
            }
        }
    }

    private fun updateFood(foods: MutableList<FoodModel>?,isDelete : Boolean) {
        val updateData =  HashMap<String, Any>()

        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .updateChildren(updateData)
            .addOnFailureListener { e-> Toast.makeText(context!!,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    foodListViewModel.getMutableFoodModelListData()
                    EventBus.getDefault().postSticky(ToastEvent(!isDelete,true))
                }
            }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}