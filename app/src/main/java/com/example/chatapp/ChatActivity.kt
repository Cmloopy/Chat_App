package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.databinding.ActivityChatBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

private lateinit var binding: ActivityChatBinding
private val db = Firebase.firestore
private var storageRef = FirebaseStorage.getInstance().reference
class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
         setContentView(binding.root)

        val id = intent.getStringExtra("id_user")
        val id_fr = intent.getStringExtra("id_friend")

        if(id_fr != null) {
            val inforFR = db.collection("user").document(id_fr)
            inforFR.get()
                .addOnSuccessListener { document ->
                    if(document != null && document.exists()){
                        val urlIMG = document.getString("url_anhdd")
                        val fullname = document.getString("fullname")
                        binding.nameFriend.text = fullname
                        if(urlIMG != null) {
                            val imageRef = storageRef.child(urlIMG)
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(this)
                                    .load(uri)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.basicimg)
                                    .error(R.drawable.basicimg)
                                    .into(binding.avtFriend)
                            }.addOnFailureListener { exception ->
                                Log.e("Firestore", "Failed to download image", exception)
                            }
                        }
                    }
                }
        }
        //Xử lý chat ở đây!!
        if(id != null){
            frameChat(id)
        }

        binding.btnback1.setOnClickListener {
            onBackPressed()
        }

        binding.btnTCNFR.setOnClickListener {
            val intent = Intent(this, FriendProfileActivity::class.java)
            intent.putExtra("id_user", id)
            intent.putExtra("id_friend", id_fr)
            startActivity(intent)
        }
    }

    private fun frameChat(id: String) {
        val getChat = db.collection(id).document()
    }
}