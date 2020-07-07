package com.josealfonsomora.firebasephonenumberauth.service

import android.content.Context
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.josealfonsomora.firebasephonenumberauth.ui.login.LoginActivity
import dagger.hilt.android.qualifiers.ActivityContext
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PhoneNumberAuthService @Inject constructor(
    @ActivityContext private val context: Context
) {
    private val authSubject = PublishSubject.create<PhoneNumberAuthResult>()
    private val authObservable: Observable<PhoneNumberAuthResult> = authSubject.hide()

    fun auth(phoneNumber: String): Observable<PhoneNumberAuthResult> {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            context as LoginActivity, // Activity (for callback binding)
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    authSubject.onNext(PhoneNumberAuthResult.Completed(credential))
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    authSubject.onNext(PhoneNumberAuthResult.Error(exception))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    authSubject.onNext(PhoneNumberAuthResult.CodeSent(verificationId, token))
                }
            })

        return authObservable
    }

    fun verify(verificationId: String, verificationCode: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if(task.isSuccessful){
                authSubject.onNext(PhoneNumberAuthResult.VerificationCompleted(task.result?.user!!))
            }else{
                authSubject.onNext(PhoneNumberAuthResult.Error(task.exception!!))
            }
        }
    }
}

sealed class PhoneNumberAuthResult {
    data class Completed(val data: PhoneAuthCredential) : PhoneNumberAuthResult()
    data class VerificationCompleted(val data: FirebaseUser) : PhoneNumberAuthResult()
    data class CodeSent(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken
    ) : PhoneNumberAuthResult()

    data class Error(val exception: Exception) : PhoneNumberAuthResult()

}
