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
private val chatIds = mutableListOf<String>()

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

        getChatIdsByUserId(id_user) //update chatIds
        println(chatIds)

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
        intent.putExtra("id_chat", id)
        startActivity(intent)
    }
    fun getChatIdsByUserId(targetId: String) {
        // Thực hiện truy vấn
        db.collection("chat")
            .whereArrayContains("id", targetId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    println("Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    chatIds.clear() // Xóa danh sách cũ để cập nhật danh sách mới

                    for (document in snapshots) {
                        chatIds.add(document.id) // Lấy ID của document chứa targetId
                    }
                }
            }
    }
}