package com.example.messapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.messapp.R
import com.example.messapp.adapters.MessageAdapter
import com.example.messapp.databinding.ActivityChatBinding
import com.example.messapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var binding: ActivityChatBinding? = null
    private lateinit var mDb: FirebaseDatabase

    private var name: String = ""
    private var receiverUid: String = ""
    private var receiverImage: String = ""
    private var msgList: ArrayList<Message> = ArrayList()
    private lateinit var messageAdapter: MessageAdapter
    private var senderRoom: String = ""
    private var receiverRoom: String = ""
    private var mSelectedImage: Uri? = null
    private lateinit var mProgressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarChat)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding?.ivBack!!.setOnClickListener { onBackPressed() }

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("Updating image...")

        // Implement send msg
        mDb = FirebaseDatabase.getInstance()
        if (intent.hasExtra("name")) {
            name = intent.getStringExtra("name").toString()
        }
        if (intent.hasExtra("uid")) {
            receiverUid = intent.getStringExtra("uid").toString()
        }
        if (intent.hasExtra("image")) {
            receiverImage = intent.getStringExtra("image").toString()
        }
        if (receiverImage != "No image") {
            Glide.with(this).load(receiverImage).placeholder(R.drawable.avatar)
                .into(binding?.ivAvatar!!)
        }
        binding?.tvName!!.text = name
        val senderUid: String = FirebaseAuth.getInstance().currentUser!!.uid
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid


        msgList = ArrayList()
        messageAdapter = MessageAdapter(this, msgList, senderRoom, receiverRoom)

        binding?.rvMsgList!!.layoutManager = LinearLayoutManager(this)
        binding?.rvMsgList!!.adapter = messageAdapter

        mDb.reference.child("presence")
            .child(receiverUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status != null) {
                            if (status.isNotEmpty()) {
                                if (status == "Online") {
                                    binding?.tvStatus!!.text = status
                                    binding?.tvStatus!!.visibility = View.VISIBLE
                                } else {
                                    binding?.tvStatus!!.visibility = View.GONE
                                }
                            }
                        } else {
                            binding?.tvStatus!!.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        mDb.reference.child("chats")
            .child(senderRoom)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    msgList.clear()
                    for (data in snapshot.children) {
                        val message = data.getValue<Message>()
                        if (message != null) {
                            message.messageID = data.key.toString()
                            msgList.add(message)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding?.rvMsgList!!.scrollToPosition(msgList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding?.ivSend!!.setOnClickListener {
            val msg: String = binding?.etMsg!!.text.toString()
            if (!TextUtils.isEmpty(msg.trim { it <= ' ' })) {
                val message =
                    Message(message = msg, senderID = senderUid, timestamp = Date().time)
                binding?.etMsg!!.text.clear()

                val randomKey = mDb.reference.push().key

                mDb.reference.child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomKey.toString())
                    .setValue(message)
                    .addOnSuccessListener {
                        mDb.reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(randomKey.toString())
                            .setValue(message)
                            .addOnSuccessListener {

                            }
                        val lastMsgObj: HashMap<String, Any> = HashMap()
                        lastMsgObj["lastMsg"] = message.message
                        lastMsgObj["lastMsgTime"] = message.timestamp

                        mDb.reference.child("chats")
                            .child(senderRoom)
                            .updateChildren(lastMsgObj)
                        mDb.reference.child("chats")
                            .child(receiverRoom)
                            .updateChildren(lastMsgObj)
                    }
            }
        }


        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.data != null) {
                    mSelectedImage = result.data?.data!!
                    val calendar = Calendar.getInstance()
                    val mRef = FirebaseStorage.getInstance().reference.child("chats")
                        .child("${calendar.timeInMillis}")

                    mProgressDialog.show()

                    mRef.putFile(mSelectedImage!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            mRef.downloadUrl.addOnSuccessListener { uri ->
                                val message =
                                    Message(
                                        message = "photo",
                                        senderID = senderUid,
                                        timestamp = Date().time,
                                        imageUrl = uri.toString()
                                    )

                                val randomKey = mDb.reference.push().key

                                mDb.reference.child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(randomKey.toString())
                                    .setValue(message)
                                    .addOnSuccessListener {
                                        mProgressDialog.dismiss()
                                        mDb.reference.child("chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(randomKey.toString())
                                            .setValue(message)
                                            .addOnSuccessListener {

                                            }
                                        val lastMsgObj: HashMap<String, Any> = HashMap()
                                        lastMsgObj["lastMsg"] = message.message
                                        lastMsgObj["lastMsgTime"] = message.timestamp

                                        mDb.reference.child("chats")
                                            .child(senderRoom)
                                            .updateChildren(lastMsgObj)
                                        mDb.reference.child("chats")
                                            .child(receiverRoom)
                                            .updateChildren(lastMsgObj)
                                    }
                            }
                        } else {
                            mProgressDialog.dismiss()
                        }
                    }
                }
            }

        binding?.ivAttach!!.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }

    }
}