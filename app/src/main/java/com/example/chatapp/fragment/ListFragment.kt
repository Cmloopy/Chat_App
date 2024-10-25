package com.example.chatapp.fragment
//fragment danh sach ban be
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.ChatActivity
import com.example.chatapp.SuggestActivity
import com.example.chatapp.adapter.ListFriendAdapter
import com.example.chatapp.databinding.FragmentListBinding
import com.example.chatapp.item.item_list_friend
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Kết nối Firebase
private val db = Firebase.firestore
private var id_user = ""
// Khai báo binding
private lateinit var binding: FragmentListBinding

class ListFragment : Fragment(), ListFriendAdapter.OnItemClickListener { // Implement OnItemClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Sử dụng ViewBinding
        binding = FragmentListBinding.inflate(inflater, container, false)

        // Lấy user ID từ arguments
        val id = arguments?.getString("id_user")
        if (id != null) {
            id_user = id
            val getListFr = db.collection("user").document(id)
            getListFr.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Lấy mảng từ document
                        val items = document.get("friend") as? List<Map<String, Number>>
                        val addlist = document.get("listadd") as? List<String>
                        if(addlist!=null && addlist.size > 0){
                            binding.newadd.visibility = View.VISIBLE
                        }
                        if (items != null) {
                            val newItemSort = items.sortedByDescending { (it["check"] as? Number)?.toInt() ?: 0 }
                            // Gọi hàm xử lý mảng sau khi lấy được
                            sorted(newItemSort)
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

        //danh sach add friend
        binding.listadd.setOnClickListener {
            val intent = Intent(requireContext(), SuggestActivity::class.java)
            intent.putExtra("id_user",id)
            startActivity(intent)
        }

        return binding.root
    }

    // Hàm sắp xếp và lấy danh sách bạn bè
    private fun sorted(items: List<Map<String, Number>>) {
        val limitedItems = items.take(40)

        data class FriendWithCheck(val id: String, val check: Int)
        // Lưu id và giá trị "check" vào một đối tượng
        val idListWithCheck = limitedItems.mapNotNull {
            val id = it["id"] as? String
            val check = it["check"] as? Int ?: 0
            if (id != null) FriendWithCheck(id, check) else null
        }

        var listHT: MutableList<item_list_friend> = mutableListOf()
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        for (friend in idListWithCheck) {
            val s = db.collection("user").document(friend.id)
            val task = s.get()
            tasks.add(task)
        }

        // Sử dụng Tasks.whenAllComplete để đợi tất cả các request hoàn thành
        Tasks.whenAllComplete(tasks).addOnCompleteListener {
            // Sau khi tất cả các task hoàn thành
            for ((index, task) in tasks.withIndex()) {
                val document = (task as Task<DocumentSnapshot>).result
                if (document != null && document.exists()) {
                    // Lấy các giá trị từ document
                    val id = document.id
                    val fullname = document.getString("fullname") ?: ""
                    val imageUrl = document.getString("url_anhdd") ?: ""
                    val isActive = document.getBoolean("active") ?: false

                    // Tạo đối tượng Friend và thêm vào danh sách
                    listHT.add(item_list_friend(imageUrl, fullname, id, isActive))
                }
            }

            // Sắp xếp lại listHT theo giá trị "check"
            update(listHT) // Cập nhật danh sách sau khi hoàn thành
        }
    }

    // Hàm cập nhật danh sách bạn bè trong RecyclerView
    private fun update(friendList: List<item_list_friend>) {
        if (binding.listBb.adapter == null) {
            // Nếu adapter chưa khởi tạo, tạo adapter mới và gán cho RecyclerView
            binding.listBb.layoutManager = LinearLayoutManager(requireContext())
            val adapter = ListFriendAdapter(friendList, this@ListFragment) // Truyền this@ListFragment làm listener
            binding.listBb.adapter = adapter
        } else {
            // Cập nhật dữ liệu cho adapter nếu đã tồn tại
            (binding.listBb.adapter as ListFriendAdapter).updateData(friendList)
        }
    }

    // Xử lý sự kiện khi click vào item
    override fun onItemClick(friend: item_list_friend) {
        // Mở màn hình chat bạn bè khi click vào item
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("id_friend", friend.id)
        intent.putExtra("id_user",id_user)
        startActivity(intent)
    }
}
