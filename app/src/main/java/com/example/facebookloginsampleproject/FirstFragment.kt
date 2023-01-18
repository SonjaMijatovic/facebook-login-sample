package com.example.facebookloginsampleproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.facebookloginsampleproject.databinding.FragmentFirstBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    lateinit var callbackManager: CallbackManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager =  CallbackManager.Factory.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            logIn()
        }
    }

    private fun logIn() {
        val loginManager = LoginManager.getInstance()
        loginManager.logInWithReadPermissions(
            activity = context as Activity,
            permissions = listOf(
                "public_profile",
                "email"
            )
        )
        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.e("FB login", "canceled")
                }

                override fun onError(error: FacebookException) {
                    Log.e("FB login", "onError ${error.localizedMessage}")
                }

                override fun onSuccess(result: LoginResult) {
                    Log.e("FB login", "success ${result.accessToken.token}")
                    Log.e("FB login", "success ${result.authenticationToken?.token}")
                    getUserDetails()
                }
            }
        )
    }

    private fun getUserDetails() {
        val facebookAccessToken = AccessToken.getCurrentAccessToken()
        if (facebookAccessToken == null) {
            Log.e("FB getUserDetails", "access token is null")
            return
        }
        val request = GraphRequest.newMeRequest(facebookAccessToken) { _, response ->
                val userDetailsJsonString = response?.rawResponse
                if (userDetailsJsonString.isNullOrBlank()) {
                    Log.e("FB getUserDetails", "Graph response - user details is null")
                }
        }
        val parameters =
            bundleOf("fields" to "id,name,first_name,last_name,email,picture")
        request.parameters = parameters
        request.executeAsync()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}