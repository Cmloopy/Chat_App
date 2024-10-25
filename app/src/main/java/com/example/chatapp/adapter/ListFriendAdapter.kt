package com.example.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.R
import com.example.chatapp.item.item_list_friend
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

private var storageRef = FirebaseStorage.getInstance().reference

class ListFriendAdapter(
    var listbb: List<item_list_friend>,
    private val listener: OnItemClickListener // Sử dụng interface custom
) : RecyclerView.Adapter<ListFriendAdapter.ListFriendViewHolder>() {

    var oldList = listbb

    // Interface để xử lý sự kiện click
    interface OnItemClickListener {
        fun onItemClick(friend: item_list_friend)
    }

    inner class ListFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val anhdd = itemView.findViewById<CircleImageView>(R.id.anhddlist)
        val fullname = itemView.findViewById<TextView>(R.id.fullnamee)
        val tt = itemView.findViewById<CircleImageView>(R.id.tt)

        // Thêm hàm bind để gắn dữ liệu và sự kiện click
        fun bind(friend: item_list_friend, clickListener: OnItemClickListener) {
            fullname.text = friend.fullname

            // Hiển thị ảnh đại diện nếu có
            if (friend.url_anhdd.isNotEmpty()) {
                val imageRef = storageRef.child(friend.url_anhdd)
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

            // Ẩn trạng thái nếu không active
            if (!friend.isActive) {
                tt.visibility = View.GONE
            }

            // Gắn sự kiện click vào itemView
            itemView.setOnClickListener {
                clickListener.onItemClick(friend)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListFriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_litst_fragment, parent, false)
        return ListFriendViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listbb.size
    }

    override fun onBindViewHolder(holder: ListFriendViewHolder, position: Int) {
        // Gọi hàm bind và truyền listener vào
        holder.bind(listbb[position], listener)
    }

    fun updateData(newItems: List<item_list_friend>) {
        listbb = newItems
        if (listbb != oldList) {
            notifyDataSetChanged() // Thông báo rằng dữ liệu đã thay đổi
        }
    }
}
