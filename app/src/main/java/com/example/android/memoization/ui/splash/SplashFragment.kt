package com.example.android.memoization.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.android.memoization.MainActivity
import com.example.android.memoization.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        view.findViewById<ImageView>(R.id.splash_picture).setImageResource(R.drawable.splash)
        lifecycleScope.launch {
            delay(1000)
            findNavController().navigate(R.id.action_splashFragment_to_folderScreenFragment)
        }
    }
}