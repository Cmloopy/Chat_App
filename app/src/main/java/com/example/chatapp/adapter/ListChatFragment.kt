package com.example.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.R
import com.example.chatapp.item.item_chat_fragment
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

private var storageRef = FirebaseStorage.getInstance().reference

class ListChatFragment(
    var list_chat_frg: List<item_chat_fragment>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ListChatFragment.ListChatViewHolder>() {

    var oldList = list_chat_frg

    interface OnItemClickListener{
        fun onItemClick(chat: item_chat_fragment)
    }

    inner class ListChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val anhdd = itemView.findViewById<CircleImageView>(R.id.anhddfriend)
        val fname = itemView.findViewById<TextView>(R.id.nameFr)
        val tt = itemView.findViewById<CircleImageView>(R.id.tthd)
        val ntext = itemView.findViewById<TextView>(R.id.newMess)

        fun bind(chat: item_chat_fragment, clickListener: OnItemClickListener){
            fname.text = chat.fullName
            if(!chat.tthd){
                tt.visibility = View.GONE
            }
            ntext.text = chat.newMess
            if (chat.url.isNotEmpty()) {
                val imageRef = storageRef.child(chat.url)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(itemView.context)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu trữ cache để tăng tốc độ tải
                        .placeholder(R.drawable.basicimg)
                        .error(R.drawable.basicimg)
                        .into(anhdd)
                }.addOnFailureListener { exception ->
                    // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_fragment, parent, false)
        return ListChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list_chat_frg.size
    }

    override fun onBindViewHolder(holder: ListChatViewHolder, position: Int) {
        holder.bind(list_chat_frg[position], listener)
    }

    fun updateData(newItems: List<item_chat_fragment>) {
        list_chat_frg = newItems
        if (list_chat_frg != oldList) {
            notifyDataSetChanged() // Thông báo rằng dữ liệu đã thay đổi
        }
    }
}