package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.databinding.ActivityFriendProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

private lateinit var binding: ActivityFriendProfileBinding
private val db = Firebase.firestore
private val storageRef = FirebaseStorage.getInstance().reference
private var id_user = ""
private var id_friend = ""
class FriendProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id_user")
        val id_fr = intent.getStringExtra("id_friend")
        if(id_fr != null){
            id_friend = id_fr
            val getTT = db.collection("user").document(id_friend)
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
                    val listAdd = document.get("listadd") as? List<String>

                    binding.FrFullname.text = fullname
                    binding.FrDob.text = dob
                    binding.FrAddress.text = addr
                    binding.FrPhone.text = phone
                    binding.FrSex.text = sex
                    if(listfr!=null){
                        binding.FrFriend.text = (listfr.size).toString()
                    }
                    else{
                        binding.FrFriend.text = "0"
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
                                .into(binding.FrProfileIMG)
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
                                .into(binding.FrCoverIMG)
                        }.addOnFailureListener { exception ->
                            // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh
                        }
                    }
                    //neu la ban be thi hien nut trang thai
                    if(id != null) id_user = id
                    Log.d("check", "$id_user    $id_friend")
                    if(listfr != null){
                        val found = listfr.any { it["id"] == id_user }
                        if(found){
                            binding.bb.visibility = View.VISIBLE
                            Log.d("checkBTNbb", "ok")
                        }
                        else Log.d("checkBTNbb", "not a friend")
                    }
                    Log.d("Firestore", "Document data: $imganhbia")
                    //lay tt user de set button
                    val getTTT = db.collection("user").document(id_user)
                    getTTT.get().addOnSuccessListener { document ->
                        if(document!= null && document.exists()){
                            var check1 = false
                            var check2 = false
                            var check3 = false
                            val listadd = document.get("listadd") as? List<String>
                            val listfriend = document.get("friend") as? List<Map<String,Any>>
                            if(listadd != null){
                                if(id_friend in listadd) check1 = true
                            }
                            if(listAdd != null){
                                if(id_user in listAdd) check2 = true
                            }
                            if(listfriend != null){
                                for (i in listfriend){
                                    if(i["id"] == id_friend) check3 = true
                                }
                            }
                            Log.d("check Check", "$check1    $check2    $check3")
                            //neu co id thi hien nut huy loi moi (do da gui roi)
                            if(check2 && !check3){
                                binding.huyloimoi.visibility = View.VISIBLE
                                Log.d("checkBTNhuylm", "ok")
                            }
                            //neu k co id nhung id_fr lai co trong listadd cua user thi hien nut xac nhan
                            else if(check1 && !check3){
                                binding.xacnhanketban.visibility = View.VISIBLE
                                Log.d("checkBTNxacnhan", "ok")
                            }
                            //2 ben deu k co(tuc la chua ai gui loi moi) thi hien nut gui loi moi
                            else if(check3){
                                binding.bb.visibility = View.VISIBLE
                            }
                            else binding.guiloimoi.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener {

            }
        }
        binding.Frback.setOnClickListener {
            onBackPressed()

        }
        binding.btnFrChat.setOnClickListener {
            val intent = Intent(this,ChatActivity::class.java)
            intent.putExtra("id_user",id_user)
            intent.putExtra("id_friend",id_friend)
            startActivity(intent)
        }
        binding.bb.setOnClickListener {
            //Hien dialog xac nhan huy ket ban
            chooseBox() //BUG
        }
        binding.huyloimoi.setOnClickListener {
            //Click vao huy loi moi da gui
            val listFrAdd = db.collection("user").document(id_friend)
            listFrAdd.update("listadd",FieldValue.arrayRemove(id_user))
                .addOnSuccessListener {
                    Toast.makeText(this, "Hủy lời mời thành công!",Toast.LENGTH_SHORT).show()
                    binding.huyloimoi.visibility = View.GONE
                    binding.guiloimoi.visibility = View.VISIBLE
                }
        }
        binding.xacnhanketban.setOnClickListener {
            //hien bottom check hoi 2 lua chon dong y hoac huy
            showBottomSheetDialog()
        }
        binding.guiloimoi.setOnClickListener {
            //click gui loi moi ket ban
            val listFrAdd = db.collection("user").document(id_friend)
            listFrAdd.update("listadd",FieldValue.arrayUnion(id_user))
                .addOnSuccessListener {
                    Toast.makeText(this, "Gửi lời mời thành công!",Toast.LENGTH_SHORT).show()
                    binding.guiloimoi.visibility = View.GONE
                    binding.huyloimoi.visibility = View.VISIBLE
                }
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)

        bottomSheetDialog.setContentView(view)

        view.findViewById<TextView>(R.id.accept).setOnClickListener {
            val newFriend = hashMapOf(
                "id" to id_friend,
                "check" to 0
            )
            val userFriendList = db.collection("user").document(id_user)
            userFriendList.update("listadd",FieldValue.arrayRemove(id_friend))
            userFriendList.update("friend",FieldValue.arrayUnion(newFriend))
                .addOnSuccessListener {
                    val newFriend2 = hashMapOf(
                        "id" to id_user,
                        "check" to 0
                    )
                    val frFriendList = db.collection("user").document(id_friend)
                    frFriendList.update("friend",FieldValue.arrayUnion(newFriend2))
                        .addOnSuccessListener {
                            binding.xacnhanketban.visibility = View.GONE
                            binding.bb.visibility = View.VISIBLE
                        }
                        .addOnFailureListener {
                            Log.e("Add","Failed")
                        }
                }
                .addOnFailureListener {
                    Log.e("Add","Failed")
                }
            bottomSheetDialog.dismiss()
        }
        view.findViewById<TextView>(R.id.cancel).setOnClickListener {
            val userFriendList = db.collection("user").document(id_user)
            userFriendList.update("listadd",FieldValue.arrayRemove(id_friend))
            binding.xacnhanketban.visibility = View.GONE
            binding.guiloimoi.visibility = View.VISIBLE
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun chooseBox() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Thông Báo!!")
        builder.setMessage("Bạn chắc chắn muốn hủy kết bạn?")
        builder.setPositiveButton("OK") { dialog, _ ->
            val user = db.collection("user").document(id_user)
            // Lấy mảng từ Firestore
            user.get().addOnSuccessListener { document ->
                if (document != null) {
                    // Lấy mảng hiện tại từ Firestore
                    val array = document.get("friend") as? ArrayList<Map<String, Any>>

                    if (array != null) {
                        Log.e("check", "$array")
                        // Xác định phần tử cần xóa dựa trên key hoặc value
                        val updatedArray = array.filter { map ->
                            map["id"] != id_friend
                        }
                        Log.e("id_friend", id_friend)
                        Log.e("CheckListBB", "$updatedArray")
                        // Cập nhật mảng đã xóa phần tử lên Firestore
                        user.update("friend", updatedArray)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Đã xóa bb khỏi mảng")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Lỗi khi xóa", e)
                            }
                    }
                }
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Lỗi khi lấy tài liệu", e)
            }
            val friend = db.collection("user").document(id_friend)
            // Lấy mảng từ Firestore
            friend.get().addOnSuccessListener { document ->
                if (document != null) {
                    // Lấy mảng hiện tại từ Firestore
                    val array = document.get("friend") as? ArrayList<Map<String, Any>>

                    if (array != null) {
                        // Xác định phần tử cần xóa dựa trên key hoặc value
                        val updatedArray = array.filter { map ->
                            map["id"] != id_user
                        }
                        Log.e("id_user", id_user)
                        Log.e("CheckListBBfr", "$updatedArray")
                        // Cập nhật mảng đã xóa phần tử lên Firestore
                        friend.update("friend", updatedArray)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Đã xóa bb khỏi mảng")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Lỗi khi xóa", e)
                            }
                    }
                }
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Lỗi khi lấy tài liệu", e)
            }
            binding.bb.visibility = View.GONE
            binding.guiloimoi.visibility = View.VISIBLE
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}