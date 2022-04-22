package com.example.messapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messapp.R
import com.example.messapp.adapters.StatusAdapter
import com.example.messapp.adapters.UserAdapter
import com.example.messapp.databinding.ActivityMainBinding
import com.example.messapp.models.Status
import com.example.messapp.models.User
import com.example.messapp.models.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var mDb: FirebaseDatabase
    private var users: ArrayList<User> = ArrayList()
    private var userStatuses: ArrayList<UserStatus> = ArrayList()
    private lateinit var userAdapter: UserAdapter
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mDb = FirebaseDatabase.getInstance()
        users = ArrayList()

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                user = it.getValue(User::class.java)!!
            }

        userAdapter = UserAdapter(this, users)
        statusAdapter = StatusAdapter(this, userStatuses)

        binding?.rvUsers!!.adapter = userAdapter
        binding?.rvUsers!!.layoutManager = LinearLayoutManager(this)

        binding?.rvUserStatus!!.adapter = statusAdapter
        binding?.rvUserStatus!!.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("Uploading image...")

        val resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.data != null) {
                mProgressDialog.show()
                val date = Date()
                val mRef = FirebaseStorage.getInstance().reference.child("status")
                    .child("${date.time}")

                mRef.putFile(result.data!!.data!!).addOnCompleteListener {
                    if (it.isSuccessful) {
                        mRef.downloadUrl.addOnSuccessListener { Uri ->
                            val userStatus = UserStatus(user.name, user.profileImage, date.time)

                            val obj = HashMap<String, Any>()
                            obj["name"] = user.name
                            obj["profileImage"] = userStatus.profileImage
                            obj["lastUpdate"] = userStatus.lastUpdate

                            val status = Status(Uri.toString(), userStatus.lastUpdate)

                            mDb.reference.child("stories")
                                .child(user.uid)
                                .updateChildren(obj)
                            mDb.reference.child("stories")
                                .child(user.uid)
                                .child("statuses")
                                .push()
                                .setValue(status)
                            mProgressDialog.dismiss()
                        }
                    } else {
                        mProgressDialog.dismiss()
                        Toast.makeText(this, it.exception!!.message.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        binding?.bottomNavigationView!!.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.status -> {


                }
                else -> {
                }
            }
            false
        }

        binding?.ivStatus!!.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }

        mDb.reference.child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    users.clear()
                    for (data in snapshot.children) {
                        val user = data.getValue<User>()
                        if (user != null && user.uid != FirebaseAuth.getInstance().currentUser!!.uid && user.completed) {
                            users.add(user)
                        }
                    }
                    userAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("test", "error")
                }

            })

        mDb.reference.child("stories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userStatuses.clear()
                    for (data in snapshot.children) {
                        val status = UserStatus(
                            data.child("name").getValue(String::class.java).toString(),
                            data.child("profileImage").getValue(String::class.java).toString(),
                            data.child("lastUpdate").getValue(Long::class.java)!!
                        )
                        val statuses: ArrayList<Status> = ArrayList()
                        for (i in data.child("statuses").children) {
                            statuses.add(
                                Status(
                                    i.child("image").value.toString(),
                                    i.child("timeStamp").getValue(Long::class.java)!!
                                )
                            )
                        }
                        status.statusList = statuses
                        userStatuses.add(status)
                    }
                    statusAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("test", "error")
                }

            })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.header_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.profile -> {
                val intent = Intent(this, SetUpProfileActivity::class.java)
                intent.putExtra("image_profile", user.profileImage)
                intent.putExtra("name_profile", user.name)
                startActivity(intent)
            }

            R.id.logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Sign out")
                    .setMessage("Are you sure to sign out?")
                    .setPositiveButton(
                        "Yes"
                    ) { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val currentID: String = FirebaseAuth.getInstance().uid!!
        mDb.reference.child("presence").child(currentID).setValue("Online")
    }

    override fun onStop() {
        super.onStop()
        super.onResume()
        val currentID: String = FirebaseAuth.getInstance().uid!!
        mDb.reference.child("presence").child(currentID).setValue("Offline")
    }
}