package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.databinding.ActivityTrangchuBinding
import com.example.chatapp.fragment.AddFragment
import com.example.chatapp.fragment.ChatFragment
import com.example.chatapp.fragment.ListFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

private var db = Firebase.firestore
private lateinit var binding : ActivityTrangchuBinding
private var storageRef = FirebaseStorage.getInstance().reference
class Trangchu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrangchuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id_user")

        val bundle = Bundle()
        bundle.putString("id_user",id)

        var currentImgUrl: String? = null // Biến để giữ URL ảnh hiện tại

        if (id != null) {
            val getImgUrl = db.collection("user").document(id)

            getImgUrl.addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val imgUrl = document.getString("url_anhdd")

                    // Chỉ cập nhật nếu URL mới khác với URL hiện tại
                    if (imgUrl != null && imgUrl != currentImgUrl) {
                        currentImgUrl = imgUrl // Cập nhật URL hiện tại

                        val imageRef = storageRef.child(imgUrl)
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.basicimg)
                                .error(R.drawable.basicimg)
                                .into(binding.profileImage)
                        }.addOnFailureListener { exception ->
                            Log.e("Firestore", "Failed to download image", exception)
                        }
                    }
                    Log.d("Firestore", "Document data: $imgUrl")
                } else {
                    Log.d("Firestore", "No such document")
                }
            }
        }


        binding.profileImage.setOnClickListener {
            getProfileActivity(id)
        }

        val chatFragment = ChatFragment()
        chatFragment.arguments = bundle
        replaceFragment(chatFragment)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottom_chat -> {
                    val fragment = ChatFragment()
                    fragment.arguments = bundle
                    replaceFragment(fragment)
                    true
                }
                R.id.bottom_listfriend -> {
                    val fragment = ListFragment()
                    fragment.arguments = bundle
                    replaceFragment(fragment)
                    true
                }
                R.id.bottom_addfriend -> {
                    val fragment = AddFragment()
                    fragment.arguments = bundle
                    replaceFragment(fragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun getProfileActivity(id: String?) {
        if(id != null){
            val intent = Intent(this,ProfileActivity::class.java)
            intent.putExtra("id_user",id)
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,fragment).commit()
    }
}