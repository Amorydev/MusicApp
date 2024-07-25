package com.amory.musicapp.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amory.musicapp.R
import com.amory.musicapp.activities.LoginActivity
import com.amory.musicapp.config.AuthStateManager
import com.amory.musicapp.databinding.FragmentProfileBinding
import com.amory.musicapp.model.AuthResponse
import com.amory.musicapp.retrofit.APICallAuth
import com.amory.musicapp.retrofit.RetrofitClient
import com.bumptech.glide.Glide
import net.openid.appauth.AuthState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var mStateManager: AuthStateManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mStateManager = AuthStateManager.getInstance(requireContext())
        getAccount()

        binding.signOutBtn.setOnClickListener {
            signOut()
        }
    }

    private fun getAccount() {
        val service = RetrofitClient.retrofitInstance.create(APICallAuth::class.java)
        val callAccount = service.getAccount()
        callAccount.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    if (_binding != null) {
                        initView(response.body())
                    }
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to load account details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initView(body: AuthResponse?) {
        binding.nameAccountTXT.text = "${body?.firstName} ${body?.lastName}"
        Glide.with(binding.root).load(body?.picture).into(binding.imvAccount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signOut() {
        val currentState: AuthState = mStateManager.getCurrent()
        val clearState: AuthState? = currentState.authorizationServiceConfiguration?.let {
            AuthState(it)
        }
        if (currentState.lastRegistrationResponse != null) {
            clearState?.update(currentState.lastRegistrationResponse)
        }
        if (clearState != null) {
            mStateManager.replace(clearState)
        }
        val mainIntent = Intent(requireContext(), LoginActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        requireActivity().finish()
    }
}
