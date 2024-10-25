package com.example.absendulu_uts

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.*

class CameraFragment : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var checkBoxMasuk: CheckBox
    private lateinit var checkBoxPulang: CheckBox
    private lateinit var captureButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        imageView = view.findViewById(R.id.imageView)
        checkBoxMasuk = view.findViewById(R.id.checkBoxMasuk)
        checkBoxPulang = view.findViewById(R.id.checkBoxPulang)
        captureButton = view.findViewById(R.id.captureButton)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkBoxMasuk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxPulang.isEnabled = false
            } else {
                checkBoxPulang.isEnabled = true
            }
        }

        checkBoxPulang.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxMasuk.isEnabled = false
            } else {
                checkBoxMasuk.isEnabled = true
            }
        }

        captureButton.setOnClickListener {
            if (!checkBoxMasuk.isChecked && !checkBoxPulang.isChecked) {
                Toast.makeText(context, "Please select absensi type", Toast.LENGTH_SHORT).show()
            } else {
                openCamera()
            }
        }

        return view
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            saveAttendance(imageBitmap)
        }
    }

    private fun saveAttendance(image: Bitmap) {
        val user = auth.currentUser
        val timestamp = System.currentTimeMillis()
        val date = Date(timestamp)
        val absensiType = if (checkBoxMasuk.isChecked) "masuk" else "pulang"
        val imageBase64 = bitmapToBase64(image)
        val attendance = hashMapOf(
            "userId" to user?.uid,
            "timestamp" to date,
            "image" to imageBase64,
            "type" to absensiType
        )
        db.collection("attendance")
            .add(attendance)
            .addOnSuccessListener {
                Toast.makeText(context, "Attendance recorded", Toast.LENGTH_SHORT).show()
                Log.d("saveAttendance", "Attendance data saved: $attendance")
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to record attendance: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("saveAttendance", "Error saving attendance", e)
            }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}