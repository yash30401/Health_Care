package com.healthcare.yash.preeti.repositories

import android.util.Log
import com.bumptech.glide.RequestBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.healthcare.yash.preeti.models.User
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.URL
import com.healthcare.yash.preeti.utils.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class FirebaseMessagingRepository @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun getFCMToken(): Flow<NetworkResult<String>> {
        return flow {
            try {
                val token = firebaseMessaging.token.await()

                firestore.collection("Users").document(firebaseAuth.currentUser?.uid.toString())
                    .set(User(token)).await()

                emit(NetworkResult.Success(token.toString()))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message))
            }
        }.catch {
            NetworkResult.Error(it.message.toString(), null)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun callApi(jsonObject: JSONObject): Flow<NetworkResult<String>> {
        return flow {
            try {
                val JSON = "application/json; charset=utf-8".toMediaType()
                val okHttpClient = OkHttpClient()
                val requestBody = RequestBody.create(JSON, jsonObject.toString())
                val request = Request.Builder()
                    .url(URL)
                    .post(requestBody)
                    .header(
                        "Authorization",
                        "Bearer AAAA8GN9XBo:APA91bFC2Yue2UObI_rGQd4Bw5DZ66EkSG3hGw1IqtMt79g1eQiioiWiqAHHSMIxTxrZ771CuASvmeampE2dkvQvExddtqzJDrguf_yVYWlZbu-VRuTReB27i5bkg_ydDmzanuDSjlQX"
                    )
                    .build()
                okHttpClient.newCall(request).enqueue(object: Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("APICALLTESTING","onFailure:- ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d("APICALLTESTING","onResponse:- ${response.message.toString()}")
                    }

                })
                emit(NetworkResult.Success("Request Send Successfully"))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message))
            }
        }.catch {
            NetworkResult.Error(it.message,null)
        }.flowOn(Dispatchers.IO)
    }
}