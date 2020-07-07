package com.josealfonsomora.firebasephonenumberauth.ui.login

import android.util.Patterns
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.josealfonsomora.firebasephonenumberauth.R
import com.josealfonsomora.firebasephonenumberauth.data.LoginRepository
import com.josealfonsomora.firebasephonenumberauth.disposeWith
import com.josealfonsomora.firebasephonenumberauth.service.PhoneNumberAuthResult
import com.josealfonsomora.firebasephonenumberauth.service.PhoneNumberAuthService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LoginViewModel @ViewModelInject constructor(
    private val loginRepository: LoginRepository,
    private val authService: PhoneNumberAuthService,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(phoneNumber: String) {
        authService
            .auth(phoneNumber)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                _loginResult.value = when (result) {
                    is PhoneNumberAuthResult.Completed -> LoginResult.Success(success = LoggedInUserView(phoneNumber = phoneNumber))
                    is PhoneNumberAuthResult.Error -> LoginResult.Error(error = result.exception.message ?: "Error")
                    is PhoneNumberAuthResult.CodeSent -> LoginResult.CodeSent(result.verificationId, result.token)
                    is PhoneNumberAuthResult.VerificationCompleted -> LoginResult.Success(success = LoggedInUserView(phoneNumber = phoneNumber))
                }
            }
            .disposeWith(disposables)
    }

    fun loginDataChanged(phoneNumber: String) {
        if (!isPhoneNumberValid(phoneNumber)) {
            _loginForm.value = LoginFormState(phoneNumberError = R.string.invalid_phone_number)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String) =
        Patterns.PHONE.matcher(phoneNumber).matches()

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun verify(verificationId: String, verificationCode: String) {
        authService.verify(verificationId,verificationCode)
    }
}
