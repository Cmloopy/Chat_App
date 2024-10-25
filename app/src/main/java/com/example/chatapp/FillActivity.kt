package com.example.chatapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivityFillBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID

private lateinit var binding: ActivityFillBinding
private val db = Firebase.firestore
private lateinit var profileImg: Uri
private var storageRef = FirebaseStorage.getInstance().reference

class FillActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id_user")

        var updateinfor: HashMap<String,Any> = hashMapOf(
            "account" to true
        )

        //Xử lý chọn ảnh dd
        binding.profileImage.setOnClickListener {
            openImagePicker()
        }

        //Spinner chọn địa chỉ
        val provinces = resources.getStringArray(R.array.vietnam_provinces)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinces)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedProvince = parent.getItemAtPosition(position).toString()
                updateinfor.put("address",selectedProvince)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                updateinfor.put("address", "An Giang")
            }
        }

        //Chọn tên
        binding.reloadfullname.setOnClickListener {
            val name1 = binding.editHo.text
            val name2 = binding.editTT.text

            binding.radioButton1.text = "$name1 $name2"
            binding.radioButton2.text = "$name2 $name1"
        }
        binding.radiogr.setOnCheckedChangeListener{ group, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            updateinfor.put("fullname",selectedRadioButton.text)
        }

        //Chọn giới tính
        val sx = resources.getStringArray(R.array.sex)
        val adapterr = ArrayAdapter(this, android.R.layout.simple_spinner_item, sx)
        adapterr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerGT.adapter = adapterr

        binding.spinnerGT.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGT = parent.getItemAtPosition(position).toString()
                updateinfor.put("sex",selectedGT)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                updateinfor.put("sex", "Nam")
            }
        }


        binding.btnXN.setOnClickListener {
            if(id != null){
                val day = binding.editDay.text.toString()
                val month = binding.editMonth.text.toString()
                val year = binding.editYear.text.toString()
                var dob:String?
                if(day.length == 1){
                    if (month.length == 1) {
                        dob = "0$day/0$month/$year"
                    }
                    else{
                        dob = "0$day/$month/$year"
                    }
                }
                else{
                    if (month.length == 1) {
                        dob = "$day/0$month/$year"
                    }
                    else{
                        dob = "$day/$month/$year"
                    }
                }
                if(dob!=null){
                    updateinfor.put("dob",dob)
                }
                var urlProfieImg = ""
                if(::profileImg.isInitialized){
                    urlProfieImg = saveProfileImg(profileImg, id)
                    updateinfor.put("url_anhdd",urlProfieImg)
                }
                updatei4(updateinfor, id)
                Noti(id)
            }
        }
    }

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
    private fun Noti(id: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Thông Báo!!")
        builder.setMessage("Cập nhật thông tin thành công!")
        builder.setPositiveButton("OK") { dialog, which ->
            val intent = Intent(this,Trangchu::class.java)
            intent.putExtra("id_user",id)
            startActivity(intent)
            finish()
        }
        val dialog = builder.create()
        dialog.show()
    }
    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                binding.profileImage.setImageURI(it)
                profileImg = it
            }
        }
    }
    private fun saveProfileImg(imageUri: Uri, id: String): String{
        val contentResolver = applicationContext.contentResolver
        val mimeType = contentResolver.getType(imageUri)
        val fileExtension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val randomId = UUID.randomUUID().toString()
        val urlProfileImg = "$id/anh_dai_dien/$randomId.$fileExtension"
        val imageRef = storageRef.child(urlProfileImg)

        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Ảnh đã được tải lên thành công
        }
        .addOnFailureListener { exception ->
            // Xử lý lỗi nếu tải lên thất bại
            Log.e("FirebaseStorage", "Error uploading image", exception)
        }
        return urlProfileImg
    }
}