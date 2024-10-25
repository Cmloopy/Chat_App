package com.example.chatapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivityPassBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

private lateinit var binding: ActivityPassBinding
private val db = Firebase.firestore

class PassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id_user")

        binding.btnBackk.setOnClickListener {
            onBackPressed()
        }

        binding.btnXNDMK.setOnClickListener {
            if(id != null){
                val updatePass = db.collection("user").document(id)
                updatePass.get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val pass = document.getString("password").toString()
                            val oldPass = binding.oldPass.text.toString()
                            val newPass1 = binding.newPass1.text.toString()
                            val newPass2 = binding.newPass2.text.toString()
                            Log.e("oops","$pass  $oldPass  $newPass1  $newPass2")
                            if(pass != oldPass){
                                Noti(1)
                            }
                            else if(newPass1.length < 6) {
                                Noti(0)
                            }
                            else if(newPass1 != newPass2){
                                Noti(2)
                            }
                            else if(pass == newPass1){
                                Noti(3)
                            }
                            else {
                                updatePass.update("password",newPass1)
                                Noti(8)
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.e("Firebase","Failed to get this $id document!")
                    }
            }
        }
    }

    private fun Noti(tt: Int){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Thông Báo!!")
        if(tt == 0){
            builder.setMessage("Mật khẩu mới cần tối thiểu 6 ký tự!")
        }
        else if (tt == 1){
            builder.setMessage("Nhập sai mật khẩu hiện tại!")
        }
        else if (tt == 2) {
            builder.setMessage("Mật khẩu mới nhập lại lần 2 không chính xác!")
        }
        else if(tt == 3) {
            builder.setMessage("Mật khẩu mới không được trùng với mật khẩu cũ!")
        }
        else {
            builder.setMessage("Thay đổi mật khẩu thành công!")
            binding.oldPass.setText("")
            binding.newPass1.setText("")
            binding.newPass2.setText("")
        }
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}