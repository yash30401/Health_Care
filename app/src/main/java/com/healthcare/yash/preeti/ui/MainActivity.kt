package com.healthcare.yash.preeti.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        navController = navHostFragment.navController

    }



    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        val isLoggedIn = firebaseAuth.currentUser != null

        Toast.makeText(this, "Back Pressed", Toast.LENGTH_SHORT).show()
        Log.d("FraTesting",isLoggedIn.toString())
        Log.d("FraTesting",currentDestination.toString())

        if(isLoggedIn && currentDestination?.id ==R.id.mainFragment){
            showExitConfirmationDialog()
        }else{
            super.onBackPressed()
        }
    }


    private fun showExitConfirmationDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Exit Confirmation")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit"){_,_->
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}