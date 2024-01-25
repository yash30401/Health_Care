package com.healthcare.yash.preeti.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import com.google.firebase.messaging.FirebaseMessaging
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.VideoCalling.models.MessageModel
import com.healthcare.yash.preeti.VideoCalling.repository.SocketRepository
import com.healthcare.yash.preeti.VideoCalling.utils.NewMessageInterface
import com.healthcare.yash.preeti.databinding.ActivityMainBinding
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.Constants.FIREBASEMESSAGINTOKEN
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedView
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedViewArgs
import com.healthcare.yash.preeti.utils.await
import com.healthcare.yash.preeti.viewmodels.AppointmentViewModel
import com.healthcare.yash.preeti.viewmodels.FirebaseMessagingViewModel
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NewMessageInterface{

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val firebaseMessagingViewModel by viewModels<FirebaseMessagingViewModel>()
    lateinit var socketRepository: SocketRepository

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

        socketRepository = SocketRepository(this)
        socketRepository.initSocket(firebaseAuth.currentUser?.uid.toString())
        socketRepository.sendMessageToSocket(MessageModel("store_user",firebaseAuth.uid,null,null))

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

        getFCMToken()
    }



    // Method to hide bottom navigation on specific fragments
    private fun hideBottomNavOnAuthFragment() {
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.authentication2 || destination.id == R.id.otpFragment || destination.id == R.id.doctorDetailedView || destination.id==R.id.chattingFragment) {
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

    private fun getFCMToken() {
        firebaseMessagingViewModel.getFCMToken()

        lifecycleScope.launch{
            firebaseMessagingViewModel.token.collect{
                when(it){
                    is NetworkResult.Error -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Error Block:- ${it.message}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Loading Block:- ${it.message}")
                    }
                    is NetworkResult.Success -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Success Block:- ${it.data.toString()}")
                    }
                    else -> {}
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onNewMessage(message: MessageModel) {

    }


}