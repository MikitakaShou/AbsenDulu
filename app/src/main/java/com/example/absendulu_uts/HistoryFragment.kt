package com.example.absendulu_uts

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {
    private lateinit var imageViewMasuk: ImageView
    private lateinit var imageViewPulang: ImageView
    private lateinit var textViewMasukTimestamp: TextView
    private lateinit var textViewPulangTimestamp: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        imageViewMasuk = view.findViewById(R.id.imageViewMasuk)
        imageViewPulang = view.findViewById(R.id.imageViewPulang)
        textViewMasukTimestamp = view.findViewById(R.id.textViewMasukTimestamp)
        textViewPulangTimestamp = view.findViewById(R.id.textViewPulangTimestamp)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadAttendanceImages()

        return view
    }

    private fun loadAttendanceImages() {
        val user = auth.currentUser
        db.collection("attendance")
            .whereEqualTo("userId", user?.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageBase64 = document.getString("image")
                    val type = document.getString("type")
                    val timestamp = document.getDate("timestamp")
                    if (imageBase64 != null && type != null && timestamp != null) {
                        val bitmap = base64ToBitmap(imageBase64)
                        val formattedTimestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(timestamp)
                        if (type == "masuk") {
                            imageViewMasuk.setImageBitmap(bitmap)
                            textViewMasukTimestamp.text = formattedTimestamp
                        } else if (type == "pulang") {
                            imageViewPulang.setImageBitmap(bitmap)
                            textViewPulangTimestamp.text = formattedTimestamp
                        }
                        Log.d("loadAttendanceImages", "Attendance data loaded: $document")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load attendance: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("loadAttendanceImages", "Error loading attendance", e)
            }
    }

    private fun base64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}