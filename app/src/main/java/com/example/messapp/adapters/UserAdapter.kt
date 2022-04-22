package com.example.messapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messapp.R
import com.example.messapp.activities.ChatActivity
import com.example.messapp.databinding.ItemConversationBinding
import com.example.messapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

class UserAdapter(
    private var context: Context,
    private var users: ArrayList<User>
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemConversationBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        val senderRoom = currentUserId + user.uid

        FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(senderRoom)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SimpleDateFormat")
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        val lastMsg = snapshot.child("lastMsg").getValue(String::class.java)
                        val time = snapshot.child("lastMsgTime").getValue(Long::class.java)
                        holder.binding.tvLastMsg.text = lastMsg
                        holder.binding.tvLastMsg.visibility = View.VISIBLE
                        if (time != null) {
                            holder.binding.tvTime.text = SimpleDateFormat("HH:mm").format(time)
                            holder.binding.tvTime.visibility = View.VISIBLE
                        }
                    } else {
                        holder.binding.tvLastMsg.text = "Tap to chat"
                        holder.binding.tvTime.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        if (user.profileImage != "No image") {
            Glide.with(context).load(user.profileImage).placeholder(R.drawable.avatar)
                .into(holder.binding.ivAvatar)
        }
        holder.binding.tvName.text = user.name
        holder.binding.clTextBox.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("image", user.profileImage)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}