package com.example.chatapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.ChatActivity
import com.example.chatapp.adapter.ListChatFragment
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.item.item_chat_fragment
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore


private val db = Firebase.firestore
private var id_user = ""
private lateinit var binding: FragmentChatBinding
class ChatFragment : Fragment(), ListChatFragment.OnItemClickListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater,container,false)

        val id = arguments?.getString("id_user")
        if(id != null){
            id_user = id
        }

        val listChat = mutableListOf<item_chat_fragment>() // Danh sách để lưu dữ liệu chat

        // Lấy các document từ collection "chat" mà chứa id_user trong mảng id
        db.collection("chat")
            .whereArrayContains("id", id_user)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val tasks = mutableListOf<Task<*>>() // Danh sách lưu các Task để chờ hoàn tất

                    for (document in documents) {
                        val dataMap = document.data
                        val ids = dataMap["id"] as List<String> // Mảng id trong document

                        // Lấy id của người bạn (id không phải id_user)
                        val idFriend = ids.firstOrNull { it != id_user } ?: continue

                        // Tạo task lấy tin nhắn mới nhất
                        val messageTask = document.reference.collection("message")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { messageSnapshot ->
                                if (!messageSnapshot.isEmpty) {
                                    val latestMessage = messageSnapshot.documents.firstOrNull()?.getString("content") ?: ""

                                    // Tạo task lấy thông tin bạn từ collection "user"
                                    val userTask = db.collection("user").document(idFriend).get()
                                        .addOnSuccessListener { frDoc ->
                                            if (frDoc.exists()) {
                                                val url = frDoc.getString("url_anhdd").orEmpty()
                                                val fullname = frDoc.getString("fullname") ?: ""
                                                val tthd = frDoc.getBoolean("active") ?: false

                                                // Thêm vào danh sách chat
                                                listChat.add(item_chat_fragment(idFriend, url, tthd, fullname, latestMessage, document.id))
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Err", "User query failed with exception: ${e.localizedMessage}")
                                        }
                                    tasks.add(userTask) // Thêm userTask vào danh sách
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Err", "Message query failed with exception: ${e.localizedMessage}")
                            }
                        tasks.add(messageTask) // Thêm messageTask vào danh sách
                    }

                    // Đợi tất cả các tasks hoàn thành trước khi gọi updateAdapter
                    Tasks.whenAllComplete(tasks).addOnCompleteListener {
                        Log.d("test", listChat.toString())
                    }
                } else {
                    Log.d("Info", "No matching documents found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Err", "Query failed with exception: ${e.localizedMessage}")
            }

        // Hàm cập nhật Adapter với danh sách kết quả
        listChat.add(item_chat_fragment("AzE7K7xNKbU8J5qdqlQC","AzE7K7xNKbU8J5qdqlQC/anh_dai_dien/d212436b-c7c2-426b-9924-2c95802b0bbd.jpg", true, "Chau Le", "messs testtttt", "qB9u5xKZAn2AgCeKg3DO"))
        updateAdapter(listChat) // Cập nhật adapter với dữ liệu chat

        return binding.root
    }

    private fun updateAdapter(listChat: MutableList<item_chat_fragment>) {
        if (binding.listchat.adapter == null) {
            // Nếu adapter chưa khởi tạo, tạo adapter mới và gán cho RecyclerView
            binding.listchat.layoutManager = LinearLayoutManager(requireContext())
            val adapter = ListChatFragment(listChat, this@ChatFragment)
            binding.listchat.adapter = adapter
        } else {
            // Cập nhật dữ liệu cho adapter nếu đã tồn tại
            (binding.listchat.adapter as ListChatFragment).updateData(listChat)
        }
    }

    override fun onItemClick(chat: item_chat_fragment) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("id_user",id_user)
        intent.putExtra("id_friend",chat.id_friend)
        intent.putExtra("id_chat", chat.id_chat)
        startActivity(intent)
    }
}