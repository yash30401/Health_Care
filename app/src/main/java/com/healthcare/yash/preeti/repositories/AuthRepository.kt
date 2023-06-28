package com.healthcare.yash.preeti.repositories

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.Constants.FACEBOOKTEST
import com.healthcare.yash.preeti.other.Constants.TAG
import com.healthcare.yash.preeti.utils.await
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {

    //Getting current User
    val currentUser: FirebaseUser? = firebaseAuth.currentUser

    /*
    signinWithPhoneNumber Using Credentials
    * Using Await extension function
    */
    suspend fun signinWithPhoneNo(credential: PhoneAuthCredential): NetworkResult<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            NetworkResult.Success(result.user!!)
        } catch (e: Exception) {
            NetworkResult.Error(e.message)
        }
    }

    fun signInWithFacebook(
        callbackManager: CallbackManager,
        fragment: Fragment
    ): NetworkResult<FirebaseUser>? {

        var networkResult: NetworkResult<FirebaseUser>? = null

        LoginManager.getInstance().logInWithReadPermissions(
            fragment,
            callbackManager,
            listOf("public_profile")
        )

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.d(TAG, "onCancel: Facebook Signin Cancel")
                }

                override fun onError(error: FacebookException) {
                    Log.e(FACEBOOKTEST, error.message.toString())
                }

                override fun onSuccess(result: LoginResult) {
                    val bundle = Bundle()
                    bundle.putString("fields", "id, first_name, last_name ")
                    val request = GraphRequest.newMeRequest(
                        result.accessToken
                    ) { fbObject, response ->
                        Log.v("LoginFragment Success", response.toString())

                        try {
                            Log.d(Constants.FACEBOOKTEST, "onSuccess: fbObject $fbObject")
                            val id = fbObject?.getString("id")
                            val firstName = fbObject?.getString("first_name")
                            val lastName = fbObject?.getString("last_name")
                            Log.d(Constants.FACEBOOKTEST, "onSuccess: id $id")
                            Log.d(Constants.FACEBOOKTEST, "onSuccess: email $firstName")
                            Log.d(Constants.FACEBOOKTEST, "onSuccess: email $lastName")

                            CoroutineScope(Dispatchers.IO).launch {
                                networkResult = handleFacebookLogin(result.accessToken)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    request.parameters = bundle
                    request.executeAsync()
                }
            })
        return networkResult
    }

    private suspend fun handleFacebookLogin(token: AccessToken): NetworkResult<FirebaseUser> {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        return try {
            val credential = FacebookAuthProvider.getCredential(token.token)
            val result = firebaseAuth.signInWithCredential(credential).await()
            NetworkResult.Success(result.user!!)
        } catch (e: Exception) {
            NetworkResult.Error(e.message)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}