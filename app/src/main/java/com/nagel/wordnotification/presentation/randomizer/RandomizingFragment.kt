package com.nagel.wordnotification.presentation.randomizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nagel.wordnotification.databinding.FragmentRandomizerBinding


class RandomizingFragment : Fragment() {

    private lateinit var binding: FragmentRandomizerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRandomizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = RandomizingFragment()
    }
}