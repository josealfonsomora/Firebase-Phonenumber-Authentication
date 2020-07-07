package com.josealfonsomora.firebasephonenumberauth.ui.login

import com.google.firebase.auth.PhoneAuthProvider

/**
 * Authentication result : success (user details) or error message.
 */
sealed class LoginResult {
    class Success(val success: LoggedInUserView) : LoginResult()
    class Error(val error: String) : LoginResult()
    class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : LoginResult()
}
