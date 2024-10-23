package com.example.absendulu_uts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val nameEditText: EditText = view.findViewById(R.id.editTextName)
        val nimEditText: EditText = view.findViewById(R.id.editTextNim)
        val button: Button = view.findViewById(R.id.button_save)
        button.setOnClickListener {
            val name = nameEditText.text.toString()
            val nim = nimEditText.text.toString()
            // Save name and NIM logic here
            Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
        }
        return view
    }
}