package com.example.chatapp

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivitySigninBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

private lateinit var binding: ActivitySigninBinding
private val db = Firebase.firestore
class SigninActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.CTDK.setOnClickListener {
            binding.CTDK.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            CTDK()
        }

        binding.btnLG.setOnClickListener {
            val phone = binding.editTextPhone.text.toString()
            val pass = binding.editTextPassword.text.toString()
            if(phone == null || phone == ""){
                binding.editTextPhone.setBackgroundResource(R.drawable.red_border)
            }
            else if(pass == null || pass == ""){
                binding.editTextPassword.setBackgroundResource(R.drawable.red_border)
            }
            else{
                Login(phone, pass)
            }
        }
    }
    private fun CTDK(){
        val intent = Intent(this,SignupActivity::class.java)
        startActivity(intent)
    }
    private fun Login(phone: String, pass : String){
        val DN = db.collection("user").limit(1)
        DN
            .whereEqualTo("username",phone)
            .whereEqualTo("password",pass)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("Firestore", "No matching documents.")
                    showAlertDialog()
                } else {
                    for (document in result) {
                        val tt: Boolean = document.getBoolean("account")!!
                        if(tt){
                            Log.d("Firestoree", "Matching document ID: $document.id")
                            val intent = Intent(this,Trangchu::class.java)
                            intent.putExtra("id_user",document.id)
                            update(document.id)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            val intent = Intent(this,FillActivity::class.java)
                            intent.putExtra("id_user",document.id)
                            startActivity(intent)//
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
                showAlertDialog()
            }
    }
    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Oops!! Lỗi rồi >.<")
        builder.setMessage("Sai tài khoản hoặc mật khẩu rồi kìa!")
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss() // Đóng AlertDialog
        }
        // Tạo và hiển thị AlertDialog
        val dialog = builder.create()
        dialog.show()
    }
    private fun update(id: String){
        val userRef = db.collection("user").document(id)
        userRef
            .update("active", true)
            .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error updating document", e) }
        db.collection("user")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    if (doc.exists()) {
                        //Log.d("Firestore", "Current data: ${doc.data}")
                    }
                }
            }
    }
}