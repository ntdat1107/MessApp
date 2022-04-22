package com.example.messapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messapp.R
import com.example.messapp.databinding.ItemMsgReceiveBinding
import com.example.messapp.databinding.ItemMsgSentBinding
import com.example.messapp.models.Message
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter(
    val context: Context,
    private val msgList: ArrayList<Message>,
    private val senderRoom: String,
    private val receiverRoom: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT: Int = 1
    private val ITEM_RECEIVE: Int = 2

    class SendViewHolder(val binding: ItemMsgSentBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    class ReceiveViewHolder(val binding: ItemMsgReceiveBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_SENT) {
            return SendViewHolder(
                ItemMsgSentBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        } else {
            return ReceiveViewHolder(
                ItemMsgReceiveBinding.inflate(
                    LayoutInflater.from(context),
                    parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = msgList[position]
        val reactionList = arrayOf(
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
        )
        val config = reactionConfig(context) {
            reactions {
                resId { R.drawable.ic_fb_like }
                resId { R.drawable.ic_fb_love }
                resId { R.drawable.ic_fb_laugh }
                reaction { R.drawable.ic_fb_wow scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_sad scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_angry scale ImageView.ScaleType.FIT_XY }
            }
        }
        val popup = ReactionPopup(context, config, { pos ->
            true.also {
                if (pos > -1) {
                    msg.react = pos

                    FirebaseDatabase.getInstance().reference
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(msg.messageID)
                        .setValue(msg)

                    FirebaseDatabase.getInstance().reference
                        .child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(msg.messageID)
                        .setValue(msg)

                    if (holder is SendViewHolder) {
                        holder.binding.ivReact.setImageResource(reactionList[pos])
                        holder.binding.ivReact.visibility = View.VISIBLE
                    } else if (holder is ReceiveViewHolder) {
                        holder.binding.ivReact.setImageResource(reactionList[pos])
                        holder.binding.ivReact.visibility = View.VISIBLE
                    }
                }
            }
        })

        holder.apply {
            when (holder) {
                is SendViewHolder -> {
                    if (msg.message != "photo") {
                        holder.binding.tvMsgSend.text = msg.message
                        holder.binding.tvMsgSend.visibility = View.VISIBLE
                        holder.binding.ivMsgSend.visibility = View.GONE
                    } else {
                        holder.binding.tvMsgSend.visibility = View.GONE
                        holder.binding.ivMsgSend.visibility = View.VISIBLE
                        Glide.with(context).load(msg.imageUrl)
                            .placeholder(R.drawable.place_holder)
                            .centerCrop()
                            .into(holder.binding.ivMsgSend)
                    }

                    holder.binding.llMsgSend.setOnTouchListener { v, event ->
                        popup.onTouch(v, event)
                    }
                    if (msg.react != -1) {
                        holder.binding.ivReact.setImageResource(reactionList[msg.react])
                        holder.binding.ivReact.visibility = View.VISIBLE
                    } else {
                        holder.binding.ivReact.visibility = View.GONE
                    }
                }
                is ReceiveViewHolder -> {
                    if (msg.message != "photo") {
                        holder.binding.tvMsgReceive.text = msg.message
                        holder.binding.tvMsgReceive.visibility = View.VISIBLE
                        holder.binding.ivMsgReceive.visibility = View.GONE
                    } else {
                        holder.binding.tvMsgReceive.visibility = View.GONE
                        holder.binding.ivMsgReceive.visibility = View.VISIBLE
                        Glide.with(context).load(msg.imageUrl)
                            .placeholder(R.drawable.place_holder)
                            .centerCrop()
                            .into(holder.binding.ivMsgReceive)
                    }
                    holder.binding.llMsgReceive.setOnTouchListener { v, event ->
                        popup.onTouch(v, event)
                    }
                    if (msg.react > -1) {
                        holder.binding.ivReact.setImageResource(reactionList[msg.react])
                        holder.binding.ivReact.visibility = View.VISIBLE
                    } else {
                        holder.binding.ivReact.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    override fun getItemViewType(position: Int): Int {
        val msg: Message = msgList[position]
        return if (FirebaseAuth.getInstance().uid == msg.senderID) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }
}