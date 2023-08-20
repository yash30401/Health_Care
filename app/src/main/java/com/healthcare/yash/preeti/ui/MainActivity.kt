package com.healthcare.yash.preeti.ui

import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ActivityMainBinding
import com.healthcare.yash.preeti.other.Constants.BACKSTACK
import com.healthcare.yash.preeti.ui.fragments.ConsultDoctor
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)
        hideBottomNavOnAuthFragment()


        Firebase.initialize(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        binding.bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.mainFragment)
                    true
                }

                R.id.chatFragment -> {
                    navController.navigate(R.id.chatFragment)
                    true
                }

                R.id.profileFrament -> {
                    navController.navigate(R.id.profileFrament)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun hideBottomNavOnAuthFragment() {
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.authentication2 || destination.id == R.id.otpFragment || destination.id == R.id.doctorDetailedView) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        val isLoggedIn = firebaseAuth.currentUser != null

        Log.d("FragTesting", isLoggedIn.toString())
        Log.d("FragTesting", currentDestination.toString())

        if (isLoggedIn && currentDestination?.id == R.id.mainFragment || isLoggedIn && currentDestination?.id == R.id.chatFragment || isLoggedIn && currentDestination?.id == R.id.profileFrament) {
            showExitConfirmationDialog()
        } else {
            super.onBackPressed()
        }


    }


    private fun showExitConfirmationDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Exit Confirmation")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ ->
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