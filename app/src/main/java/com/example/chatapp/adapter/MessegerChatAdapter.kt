package com.example.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.item.item_messege

class MessegerChatAdapter(var listmess: List<item_messege>, val id: String): RecyclerView.Adapter<MessegerChatAdapter.MessegerChatViewHolder>() {

    inner class MessegerChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val ress = itemView.findViewById<LinearLayout>(R.id.Nhan)
        val send = itemView.findViewById<LinearLayout>(R.id.Gui)
        val messRes = itemView.findViewById<TextView>(R.id.messNhan)
        val messSend = itemView.findViewById<TextView>(R.id.messGui)

        fun bind(mess : item_messege){
            if(id == mess.id_sender){
                ress.visibility = View.GONE
                send.visibility = View.VISIBLE
                messSend.text = mess.messege
            }
            else{
                ress.visibility = View.VISIBLE
                send.visibility = View.GONE
                messRes.text = mess.messege
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessegerChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_mess, parent, false)
        return MessegerChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listmess.size
    }

    override fun onBindViewHolder(holder: MessegerChatViewHolder, position: Int) {
        holder.bind(listmess[position])
    }
}