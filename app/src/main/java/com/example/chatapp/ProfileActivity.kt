package com.example.chatapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.databinding.ActivityProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

private lateinit var binding: ActivityProfileBinding
private var storageRef = FirebaseStorage.getInstance().reference
private val db = Firebase.firestore
private lateinit var id:String
private var check = true

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idd = intent.getStringExtra("id_user")
        if(idd != null){
            id = idd
        }

        val getTT = db.collection("user").document(id)
        getTT.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Xử lý dữ liệu document hien len view
                    val imganhdd =  document.getString("url_anhdd")
                    val imganhbia = document.getString("url_anhbia")
                    val fullname = document.getString("fullname")
                    val dob = document.getString("dob")
                    val addr = document.getString("address")
                    val phone = document.getString("username")
                    val sex = document.getString("sex")
                    val listfr = document.get("friend") as? List<Map<String, Any>>

                    binding.fullname.text = fullname
                    binding.dob.text = dob
                    binding.address.text = addr
                    binding.phone.text = phone
                    binding.sex.text = sex
                    if(listfr!=null){
                        binding.friend.text = (listfr.size).toString()
                    }
                    else{
                        binding.friend.text = "0"
                    }
                    //load anh dai dien
                    if(imganhdd != null){
                        val imageRef = storageRef.child(imganhdd)
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu trữ cache để tăng tốc độ tải
                                .placeholder(R.drawable.basicimg)
                                .error(R.drawable.basicimg)
                                .into(binding.profileIMG)
                        }.addOnFailureListener { exception ->
                            // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh
                        }
                    }
                    Log.d("Firestore", "Document data: $imganhdd")
                    //load anh bia
                    if(imganhbia != null){
                        val imageRef = storageRef.child(imganhbia)
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu trữ cache để tăng tốc độ tải
                                .error(R.drawable.biaa)
                                .into(binding.coverIMG)
                        }.addOnFailureListener { exception ->
                            // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh
                        }
                    }
                    Log.d("Firestore", "Document data: $imganhbia")
                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener {

            }
        //nut back
        binding.back.setOnClickListener {
            onBackPressed()
        }
        //thay doi thong tin
        binding.changeinfor.setOnClickListener {

        }
        //doi anh dd
        binding.profileIMG.setOnClickListener {
            check = true
            openImagePicker()
        }
        // doi anh bia
        binding.coverIMG.setOnClickListener {
            check = false
            openImagePicker()
        }
        //doi mat khau
        binding.btnDMK.setOnClickListener {
            val intent = Intent(this, PassActivity::class.java)
            intent.putExtra("id_user",id)
            startActivity(intent)
        }
        // dang xuat
        binding.btnlogout.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }
    //override cua starActivityForResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                if(check){
                    binding.profileIMG.setImageURI(it)
                    saveIMG(it, id, check)
                }
                else{
                    binding.coverIMG.setImageURI(it)
                    saveIMG(it,id,check)
                }
            }
        }
    }
    //hien thi anh doi va upload anh len storage
    private fun saveIMG(imageUri: Uri, id: String, type: Boolean){
        val contentResolver = applicationContext.contentResolver
        val mimeType = contentResolver.getType(imageUri)
        val fileExtension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        var updateinfor: HashMap<String,Any> = hashMapOf()

        val randomId = UUID.randomUUID().toString()
        var urlImg = ""
        if(type){
            urlImg = "$id/anh_dai_dien/$randomId.$fileExtension"
            updateinfor.put("url_anhdd", urlImg)
        }
        else {
            urlImg = "$id/anh_bia/$randomId.$fileExtension"
            updateinfor.put("url_anhbia", urlImg)
        }
        val imageRef = storageRef.child(urlImg)

        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
                // Ảnh đã được tải lên thành công
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu tải lên thất bại
                Log.e("FirebaseStorage", "Error uploading image", exception)
            }
        updatei4(updateinfor,id)
    }
    //up load va update firestore
    private fun updatei4(updateinfor: HashMap<String, Any>, id: String) {
        val updatee = db.collection("user").document(id)
        updatee.update(updateinfor)
            .addOnSuccessListener { Log.d("firebase", "Add success") }
            .addOnFailureListener { Log.e("firebase", "Failed to add") }
        updatedb()
    }
    private fun updatedb(){
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
}