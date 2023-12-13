package com.healthcare.yash.preeti.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ActivityMainBinding
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedView
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedViewArgs
import com.healthcare.yash.preeti.viewmodels.AppointmentViewModel
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(){

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

        // Preload Razorpay Checkout for better performance
        Checkout.preload(applicationContext)


        // Configure Firebase App Check with Play Integrity provider
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
        firebaseAuth = FirebaseAuth.getInstance()
    }

    // Method to hide bottom navigation on specific fragments
    private fun hideBottomNavOnAuthFragment() {
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.authentication2 || destination.id == R.id.otpFragment || destination.id == R.id.doctorDetailedView) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }
    }

    // Handle back button press
    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        val isLoggedIn = firebaseAuth.currentUser != null

        Log.d("FragTesting", isLoggedIn.toString())
        Log.d("FragTesting", currentDestination.toString())

        // Check if the user is logged in and on specific fragments
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

    // Implementation of PaymentResultWithDataListener

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

//    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
//        Log.d("PAYMENTRESULT","SUCCESS:- ${p0}")
//        val frag = DoctorDetailedView()
//        try {
//            frag.paymentStatus("Success")
//        }catch (e:Exception){
//            Log.d("PAYMENTERROR","SUCCESS BLOCK:- "+e.message.toString())
//        }
//
//    }
//
//    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
//        Log.d("PAYMENTRESULT","ERROR:- ${p0}")
//        val frag = DoctorDetailedView()
//        frag.paymentStatus("Error")
//    }
//
//    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {
//    }

}