package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chatapp.databinding.ActivitySignupBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay

private lateinit var binding: ActivitySignupBinding
private val db = Firebase.firestore

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSG.setOnClickListener {
            val phone = binding.editTextPhoneDK.text.toString()
            val pass1 = binding.editTextPasswordDK.text.toString()
            val pass2 = binding.editTextPasswordDK2.text.toString()
            if(phone == null || phone == ""){
                setTB("Chưa nhập SDT!", R.color.red)
            }
            else if (pass1 == null || pass1 == ""){
                setTB("Chưa nhập mật khẩu!",R.color.red)
            }
            else if (pass1 != pass2){
                setTB("Mật khẩu nhập lại chưa chính xác!",R.color.red)
            }
            else {
                Signup(phone, pass1)
            }
        }
        binding.CTDN.setOnClickListener {
            CTDN()
        }
    }
    private fun CTDN(){
        val intent = Intent(this,SigninActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun Signup(phone: String, pass: String){
        if(!check(phone)){
            val newUser = hashMapOf(
                "username" to phone,
                "password" to pass,
                "account" to false,
                "active" to true
            )
            db.collection("user")
                .add(newUser)
                .addOnSuccessListener { documentReference ->
                    Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error adding document", e)
                }
            setTB("Đăng Kí Thành Công!", R.color.green)
            update()
        }
        else{
            setTB("Số điện thoại đã được sử dụng!",R.color.red)
        }
    }

    private fun check(phone : String): Boolean{
        val phoneCheck = db.collection("user").limit(1)
        var checkk = false
        phoneCheck
            .whereEqualTo("username",phone)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    checkk = true
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
            }
        return checkk
    }
    private fun update(){
        db.collection("user")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    if (doc.exists()) {
                        Log.d("Firestore", "Current data: ${doc.data}")
                    }
                }
            }
    }
    private fun setTB(s: String, color: Int){
        binding.textThongBao.text = s
        binding.textThongBao.setTextColor(ContextCompat.getColor(this, color))
    }
}