package com.example.chatapp
// Activity hien thi danh sach loi moi ket ban
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.ListAddAdapter
import com.example.chatapp.databinding.ActivitySuggestBinding
import com.example.chatapp.item.item_list_add_friend
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

private lateinit var binding: ActivitySuggestBinding
private val db = Firebase.firestore
private var id_user = ""
class SuggestActivity : AppCompatActivity(), ListAddAdapter.OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuggestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id_user")
        if(id != null){
            id_user = id
            val getListAdd = db.collection("user").document(id)
            getListAdd.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Lấy mảng từ document
                        val addlist = document.get("listadd") as? List<String>
                        if (addlist != null) {
                            // Gọi hàm xử lý mảng sau khi lấy được
                            sorted(addlist)
                        } else {
                            Log.d("Firestore", "No array found in the field")
                        }
                    } else {
                        Log.d("Firestore", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Firestore", "Error getting document: ", exception)
                }
        }
        binding.btnBackListFr.setOnClickListener {
            onBackPressed()
        }
    }

    private fun sorted(addlist: List<String>) {
        val newAddlist = addlist.reversed()

        var listaddfr: MutableList<item_list_add_friend> = mutableListOf()
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        for (id in newAddlist){
            val s = db.collection("user").document(id)
            val task = s.get()
            tasks.add(task)
        }
        // Sử dụng Tasks.whenAllComplete để đợi tất cả các request hoàn thành
        Tasks.whenAllComplete(tasks).addOnCompleteListener {
            //Sau khi tất cả các task hoàn thành
            for (task in tasks) {
                val document = task.result
                if (document != null && document.exists()) {
                    // Lấy các giá trị từ document
                    val id = document.id
                    val fullname = document.getString("fullname") ?: ""
                    val imageUrl = document.getString("url_anhdd") ?: ""
                    // Tạo đối tượng Friend và thêm vào danh sách
                    listaddfr.add(item_list_add_friend(imageUrl, fullname, id))
                }
            }
            update(listaddfr) // Cập nhật danh sách sau khi hoàn thành
        }
    }

    private fun update(listaddfr: MutableList<item_list_add_friend>){
        binding.listLM.layoutManager = LinearLayoutManager(this)
        val adapter = ListAddAdapter(listaddfr, this@SuggestActivity)
        binding.listLM.adapter = adapter
    }

    override fun onItemAddClick(friend: item_list_add_friend) {
        val intent = Intent(this, FriendProfileActivity::class.java)
        intent.putExtra("id_user", id_user)
        intent.putExtra("id_friend", friend.id_fr)
        startActivity(intent)
    }

    override fun onAcceptAddClick(friend: item_list_add_friend) {
        val updateListFr = db.collection("user").document(id_user)
        updateListFr.update("listadd",FieldValue.arrayRemove(friend.id_fr))
            .addOnSuccessListener { Log.d("firestore", "Remove Success") }
            .addOnFailureListener { Log.e("firestore", "Failed to remove") }
        val newFr = mapOf(
            "check" to 0,
            "id" to friend.id_fr
        )
        updateListFr.update("friend",FieldValue.arrayUnion(newFr))
            .addOnSuccessListener { Log.d("firestore", "Success") }
            .addOnFailureListener { Log.e("firestore", "Failed") }
        val newFrr = mapOf(
            "check" to 0,
            "id" to id_user
        )
        val updateListFrr = db.collection("user").document(friend.id_fr)
        updateListFrr.update("friend",FieldValue.arrayUnion(newFrr))
            .addOnSuccessListener { Log.d("firestore", "Success") }
            .addOnFailureListener { Log.e("firestore", "Failed") }
    }

    override fun onDeclineAddClick(friend: item_list_add_friend) {
        val updateListFr = db.collection("user").document(id_user)
        updateListFr.update("listadd",FieldValue.arrayRemove(friend.id_fr))
            .addOnSuccessListener { Log.d("firestore", "Remove Success") }
            .addOnFailureListener { Log.e("firestore", "Failed to remove") }
    }


}