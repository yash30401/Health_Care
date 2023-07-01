package com.healthcare.yash.preeti.utils

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await():T{
    return suspendCancellableCoroutine { cont->
        addOnCompleteListener {
            if(it.exception !=null){
                cont.resumeWithException(it.exception!!)
            }else{
                cont.resume(it.result, null)
            }
        }
    }
}
