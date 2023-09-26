package com.sumon.qrscanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sumon.qrscanner.databinding.FragmentResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject


class ResultFragment : Fragment() {

    private val TAG = "ResultFragment"
    private lateinit var binding: FragmentResultBinding

    //
    private var scanResult: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentResultBinding.inflate(inflater, container, false)

        arguments?.get("result")?.toString()?.let {
            scanResult = it
            Log.d(TAG, "onCreateView: $it")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanResult?.let {
            lifecycleScope.launch {
                val response = makePostRequest(it)
                Log.d(TAG, "response: ${response?.body.toString()}")

                // Handle the response here
                response?.let {
                    val jsonObject = JSONObject(response.body?.string())
                    // Now you can access JSON data
                    val message = jsonObject.getString("message")

                    binding.tvResponseMsg.visibility = View.VISIBLE
                    if (response.code == 200) {
                        binding.tvResponseMsg.text = message
                        binding.animationView.setAnimation(R.raw.animation_state_success)
                    } else {
                        binding.tvResponseMsg.text = message
                        binding.animationView.setAnimation(R.raw.animation_state_error)
                        binding.animationView.speed = 0.5f
                    }
                    binding.animationView.playAnimation()
                } ?: kotlin.run {
                    binding.tvResponseMsg.visibility = View.VISIBLE
                    binding.tvResponseMsg.text = "Invalid response."
                    binding.animationView.setAnimation(R.raw.animation_state_bug)
                    binding.animationView.playAnimation()
                }

            }

            binding.btnScanAgain.visibility = View.VISIBLE
            binding.btnScanAgain.setOnClickListener {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        } ?: kotlin.run {
            // corrupted
            binding.tvResponseMsg.visibility = View.VISIBLE
            binding.tvResponseMsg.text = "Invalid result."
            binding.animationView.setAnimation(R.raw.animation_state_bug)
            binding.animationView.playAnimation()
        }

        binding.btnScanAgain.setOnClickListener {
            findNavController().popBackStack()
        }

    }


    private suspend fun makePostRequest(s: String): Response? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                // Define the request parameters
                val formBody = FormBody.Builder()
                    .add("client_id", requireContext().applicationContext.packageName)
                    .add("url", "$s")
                    .build()

                // Define the POST request
                val request = Request.Builder()
                    .url("https://admin.etceventsltd.com/public/api/ticket_validate") // Replace with your API endpoint
                    .post(formBody)
                    .build()

                // Execute the request and get the response
                client.newCall(request).execute()
            } catch (e: Exception) {
                // Handle exceptions
                "Error: ${e.message}"
                null
            }
        }
    }

}