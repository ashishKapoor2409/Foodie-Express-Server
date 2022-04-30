package com.example.android.foodieexpressserver

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.databinding.ActivityHomeBinding
import com.example.android.foodieexpressserver.model.eventBus.CategoryClick
import com.example.android.foodieexpressserver.model.eventBus.ChangeMenuClick
import com.example.android.foodieexpressserver.model.eventBus.ToastEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private var menuClick: Int = -1
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout;
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHome.toolbar)

        subscribeToTopic(Common.getNewOrderTopic())

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_slideshow,R.id.nav_order,R.id.nav_shipper
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                item.isChecked = true
                drawerLayout!!.closeDrawers()
                if(item.itemId == R.id.nav_sign_out){
                    signOut()
                }
                else if(item.itemId == R.id.nav_category) {
                    if(menuClick != item.itemId) {
                        navController.popBackStack()
                        navController.navigate(R.id.nav_category)
                    }
                }
                else if(item.itemId == R.id.nav_shipper) {
                    if(menuClick != item.itemId) {
                        navController.popBackStack()
                        navController.navigate(R.id.nav_shipper)
                    }
                }
                else if(item.itemId == R.id.nav_order) {
                    if(menuClick != item.itemId) {
                        navController.popBackStack()
                        navController.navigate(R.id.nav_order)
                    }
                }

                menuClick = item!!.itemId

                return true
            }

        })
        val headerView = navView.getHeaderView(0)
        val txt_user = headerView.findViewById<View>(R.id.txt_user) as TextView
        Common.setSpanString("Hey",Common.currentServerUser!!.name,txt_user)
    }

    private fun subscribeToTopic(newOrderTopic: String) {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(newOrderTopic)
            .addOnFailureListener { message ->
                Toast.makeText(this@HomeActivity,""+message.message,Toast.LENGTH_SHORT).show()
                    }
            .addOnCompleteListener {task ->
                if(!task.isSuccessful)
                    Toast.makeText(this@HomeActivity,"Subscribe topic failed",Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOut() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Sign Out")
            .setMessage("Do you really want to exit")
            .setNegativeButton("CANCEL",{dialogInterfce,_ ->
                dialogInterfce.dismiss()
            })
            .setPositiveButton("OK"){dialogInterface,_ ->
                Common.foodSelected = null
                Common.categorySelected = null
                Common.currentServerUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@HomeActivity,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategoryClick(event: CategoryClick) {
        if(event.isSuccess) {
               if(menuClick != R.id.nav_food_list) {
                   navController.navigate(R.id.nav_food_list)
                   menuClick = R.id.nav_food_list
               }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onChangeMenuEvent(event: ChangeMenuClick) {
        if(!event.isFromFoodList) {
            navController.popBackStack(R.id.nav_category,true)
            navController!!.navigate(R.id.nav_category)
        }
        menuClick = -1
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onToastEvent(event: ToastEvent) {
        if(event.isUpdate) {
            Toast.makeText(this, "Update Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Delete Success", Toast.LENGTH_SHORT).show()
        }
        EventBus.getDefault().postSticky(ChangeMenuClick(event.isBackFromFoodList))
    }
}