package com.healthcare.yash.preeti.ui.fragments


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentMainBinding
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.Constants.HEADERLAYOUTTAG
import com.healthcare.yash.preeti.other.Constants.MAINFRAGMENTTAG
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val viewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setDisplayShowHomeEnabled(true)

        drawerLayout = rootView.findViewById(R.id.drawerLayout)
        navigationView = rootView.findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            activity, drawerLayout, toolbar, 0, R.string.app_name
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }

                R.id.aboutApp -> {
                    Log.d(MAINFRAGMENTTAG, "About App")
                    true
                }

                R.id.privacyPolicy -> {
                    Log.d(MAINFRAGMENTTAG, "Privacy Policy")
                    true
                }

                R.id.contactUs -> {
                    Log.d(MAINFRAGMENTTAG, "Contact Us")
                    true
                }

                R.id.logout -> {
                    //Logout User
                    val bottomNav =
                        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                    bottomNav.animation =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
//                    viewModel.logout()
//                    findNavController().navigate(R.id.action_mainFragment_to_authentication2)
                    true
                }
                // Add more navigation items and their handling
                else -> false
            }
        }

        setupNavigationHeader()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupNavigationHeader() {
        val headerLayout = navigationView.getHeaderView(0)
        val userPic = headerLayout.findViewById<ImageView>(R.id.ivUserPic)
        val name = headerLayout.findViewById<TextView>(R.id.tvName)
        val email = headerLayout.findViewById<TextView>(R.id.tvEmail)

        val currentUser = firebaseAuth.currentUser

        Log.d(HEADERLAYOUTTAG, "Display Name:- ${currentUser?.displayName.toString()}")
        Log.d(HEADERLAYOUTTAG, "Photo Url:- ${currentUser?.photoUrl.toString()}")
        Log.d(HEADERLAYOUTTAG, "Phone Number:- ${currentUser?.phoneNumber.toString()}")



        if (currentUser?.phoneNumber.toString() == "" || currentUser?.phoneNumber == null) {
            Glide.with(this).load(currentUser?.photoUrl).centerCrop().into(userPic)
        } else {

            val hiddenPhoneNumberText =
                "+91${currentUser?.phoneNumber?.get(3)}${currentUser?.phoneNumber?.get(4)}******${
                    currentUser?.phoneNumber?.get(
                        11
                    )
                }${
                    currentUser?.phoneNumber?.get(12)
                }"
            name.text = hiddenPhoneNumberText
        }


        if (currentUser?.displayName.toString() == "" || currentUser?.displayName == null) {
            Log.d(HEADERLAYOUTTAG, "User Logged In Using Phone Number")
        } else {
            name.text = currentUser?.displayName.toString()
        }

        firebaseAuth.currentUser?.email?.let {
            email.text = it
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}