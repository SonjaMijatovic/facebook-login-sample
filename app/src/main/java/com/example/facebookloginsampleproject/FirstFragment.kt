package com.example.facebookloginsampleproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.facebookloginsampleproject.databinding.FragmentFirstBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    @Inject
    lateinit var callbackManager: CallbackManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            logIn()
        }
    }

    private fun logIn() {
        val loginManager = LoginManager.getInstance()
        loginManager.logInWithReadPermissions(
            activity = requireActivity(),
            permissions = listOf(
                FACEBOOK_READ_PERMISSION_PUBLIC_PROFILE,
                FACEBOOK_READ_PERMISSION_EMAIL,
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
                    val accessTokenString = result.accessToken.token
                    val authTokenString = result.authenticationToken?.token.toString()
                    Log.e("FB login", "accessToken: $accessTokenString")
                    Log.e("FB login", "authenticationToken: $authTokenString")
                    binding.textviewAccessToken.text = accessTokenString
                    binding.textviewAuthToken.text = authTokenString
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
            bundleOf(FACEBOOK_GRAPH_REQUEST_FIELDS to FACEBOOK_GRAPH_REQUEST_FIELDS_VALUES)
        request.parameters = parameters
        request.executeAsync()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private companion object {
        const val FACEBOOK_READ_PERMISSION_PUBLIC_PROFILE = "public_profile"
        const val FACEBOOK_READ_PERMISSION_EMAIL = "email"
        const val FACEBOOK_GRAPH_REQUEST_FIELDS = "fields"
        const val FACEBOOK_GRAPH_REQUEST_FIELDS_VALUES =
            "id,name,first_name,last_name,email,picture"
    }
}