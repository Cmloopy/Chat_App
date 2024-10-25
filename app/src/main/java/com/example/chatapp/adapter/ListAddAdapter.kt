package com.example.chatapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.R
import com.example.chatapp.item.item_list_add_friend
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

private var storageRef = FirebaseStorage.getInstance().reference

class ListAddAdapter(
    var listaddfr: List<item_list_add_friend>,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<ListAddAdapter.ListAddViewHolder>()
{
    interface OnItemClickListener {
        fun onItemAddClick(friend: item_list_add_friend)
        fun onAcceptAddClick(friend: item_list_add_friend)
        fun onDeclineAddClick(friend: item_list_add_friend)
    }

    inner class ListAddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val anhddfr = itemView.findViewById<CircleImageView>(R.id.imgadd)
        val namefr = itemView.findViewById<TextView>(R.id.fullnameadd)
        val btnAccept = itemView.findViewById<Button>(R.id.btnXNKB)
        val btnDecline = itemView.findViewById<Button>(R.id.btnHUY)
        val trangthai = itemView.findViewById<TextView>(R.id.trangthai)

        fun bind(friend: item_list_add_friend, clickListener: OnItemClickListener){
            namefr.text = friend.fullname

            // Hiển thị ảnh đại diện nếu có
            if (friend.url.isNotEmpty()) {
                val imageRef = storageRef.child(friend.url)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(itemView.context)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu trữ cache để tăng tốc độ tải
                        .placeholder(R.drawable.basicimg)
                        .error(R.drawable.basicimg)
                        .into(anhddfr)
                }.addOnFailureListener { exception ->
                    // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh
                }
            }
            // Gắn sự kiện click vào itemView
            itemView.setOnClickListener {
                clickListener.onItemAddClick(friend)
            }
            // Xử lý sự kiện click vào button Accept
            btnAccept.setOnClickListener {
                clickListener.onAcceptAddClick(friend)
                btnAccept.visibility = View.GONE
                btnDecline.visibility = View.GONE
                trangthai.visibility = View.VISIBLE
            }
            // Xử lý sự kiện click vào button Decline
            btnDecline.setOnClickListener {
                clickListener.onDeclineAddClick(friend)
                btnAccept.visibility = View.GONE
                btnDecline.visibility = View.GONE
                trangthai.visibility = View.VISIBLE
                trangthai.setText("Yêu cầu đã được hủy!")
                trangthai.setTextColor(Color.RED)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAddViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_addfr, parent, false)
        return ListAddViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaddfr.size
    }

    override fun onBindViewHolder(holder: ListAddViewHolder, position: Int) {
        // Gọi hàm bind và truyền listener vào
        holder.bind(listaddfr[position], listener)
    }
}