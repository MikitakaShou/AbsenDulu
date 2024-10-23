package com.example.absendulu_uts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {
    private lateinit var nameEditText: EditText
    private lateinit var nimEditText: EditText
    private lateinit var nameTextView: TextView
    private lateinit var nimTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var digitalClock: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            digitalClock.text = currentTime
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        nameEditText = view.findViewById(R.id.editTextName)
        nimEditText = view.findViewById(R.id.editTextNim)
        nameTextView = view.findViewById(R.id.textViewName)
        nimTextView = view.findViewById(R.id.textViewNim)
        saveButton = view.findViewById(R.id.button_save)
        digitalClock = view.findViewById(R.id.digitalClock)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("profiles").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val nim = document.getString("nim")
                        if (name != null && nim != null) {
                            displayProfile(name, nim)
                        }
                    } else {
                        // Show input fields if profile does not exist
                        showInputFields()
                    }
                }
                .addOnFailureListener {
                    // Show input fields if there is an error
                    showInputFields()
                }
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val nim = nimEditText.text.toString()
            if (name.isNotEmpty() && nim.isNotEmpty()) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val profile = hashMapOf(
                        "name" to name,
                        "nim" to nim
                    )
                    db.collection("profiles").document(userId).set(profile)
                        .addOnSuccessListener {
                            displayProfile(name, nim)
                            Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to save profile", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Please enter both name and NIM", Toast.LENGTH_SHORT).show()
            }
        }

        handler.post(updateTimeRunnable)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun displayProfile(name: String, nim: String) {
        nameEditText.visibility = View.GONE
        nimEditText.visibility = View.GONE
        saveButton.visibility = View.GONE
        nameTextView.visibility = View.VISIBLE
        nimTextView.visibility = View.VISIBLE
        nameTextView.text = name
        nimTextView.text = nim
    }

    private fun showInputFields() {
        nameEditText.visibility = View.VISIBLE
        nimEditText.visibility = View.VISIBLE
        saveButton.visibility = View.VISIBLE
        nameTextView.visibility = View.GONE
        nimTextView.visibility = View.GONE
    }
}