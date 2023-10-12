package com.nagel.wordnotification.presentation.addingwords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.MenuSelectingActionsOnWordBinding

class MenuSelectingActionsOnWord(
    private val delete: () -> Unit
) : DialogFragment() {

    private lateinit var binding: MenuSelectingActionsOnWordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MenuSelectingActionsOnWordBinding.inflate(inflater, container, false)
        binding.delete.setOnClickListener {
            delete.invoke()
            dismiss()
        }
        return binding.root
    }
}