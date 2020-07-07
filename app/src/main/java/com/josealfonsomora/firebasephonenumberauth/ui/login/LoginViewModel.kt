package com.josealfonsomora.firebasephonenumberauth.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.josealfonsomora.firebasephonenumberauth.data.LoginRepository
import com.josealfonsomora.firebasephonenumberauth.data.Result

import com.josealfonsomora.firebasephonenumberauth.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(phoneNumber: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(phoneNumber)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.phoneNumber))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
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
}
