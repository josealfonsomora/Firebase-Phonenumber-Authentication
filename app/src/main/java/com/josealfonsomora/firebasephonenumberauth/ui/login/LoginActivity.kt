package com.josealfonsomora.firebasephonenumberauth.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.auth.PhoneAuthProvider
import com.josealfonsomora.firebasephonenumberauth.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val phoneNumber = findViewById<EditText>(R.id.phoneNumber)
        val verificationCode = findViewById<EditText>(R.id.verificationCode)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless phone number is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.phoneNumberError != null) {
                phoneNumber.error = getString(loginState.phoneNumberError)
            }

        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE

            when (loginResult) {
                is LoginResult.Error -> showLoginFailed(loginResult.error)
                is LoginResult.Success -> updateUiWithUser(loginResult.success)
                is LoginResult.CodeSent -> updateUiWithCode(
                    loginResult.verificationId,
                    loginResult.token
                )
            }
        })

        phoneNumber.afterTextChanged {
            loginViewModel.loginDataChanged(phoneNumber.text.toString())
        }

        login.setOnClickListener {
            loading.visibility = View.VISIBLE

            loginViewModel.login(phoneNumber.text.toString())


            // can be launched in a separate asynchronous job
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.phoneNumber
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateUiWithCode(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ){
        phoneNumber.visibility = View.INVISIBLE
        verificationCode.visibility = View.VISIBLE

        login.setOnClickListener {
            loginViewModel.verify(verificationId, verificationCode.text.toString())
        }
    }
    private fun showLoginFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
