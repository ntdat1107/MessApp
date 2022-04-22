package com.example.messapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar!!.hide()

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)

        mAuth = FirebaseAuth.getInstance()

        mProgressDialog.setMessage("Waiting...")

        checkIsLogin()

        binding?.tvSignUp?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding?.tvForgotPassword?.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding?.btnContinue!!.setOnClickListener {
            val email = binding?.etEmail!!.text.toString()
            val password = binding?.etPassword!!.text.toString()

            when {
                TextUtils.isEmpty(email.trim { it <= ' ' }) -> {
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(password.trim { it <= ' ' }) -> {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    mProgressDialog.show()
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(mAuth.currentUser!!.uid)
                                .get()
                                .addOnSuccessListener {
                                    mProgressDialog.dismiss()
                                    var isCompleted: Boolean = false
                                    for (data in it.children) {
                                        if (data.key.equals("completed")) {
                                            isCompleted = data.value!!.toString() == "true"
                                        }
                                    }
                                    if (isCompleted) {
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finishAffinity()
                                    } else {
                                        startActivity(
                                            Intent(
                                                this,
                                                SetUpProfileActivity::class.java
                                            )
                                        )
                                        finishAffinity()
                                    }
                                }
                        }
                        .addOnFailureListener {
                            mProgressDialog.dismiss()
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun checkIsLogin() {
        val user = mAuth.currentUser
        if (user != null) {
            FirebaseDatabase.getInstance().reference.child("users")
                .child(mAuth.currentUser!!.uid)
                .get()
                .addOnSuccessListener {

                    var isCompleted: Boolean = false
                    for (data in it.children) {
                        if (data.key.equals("completed")) {
                            isCompleted = data.value!!.toString() == "true"
                        }
                    }
                    if (isCompleted) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this, SetUpProfileActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener {
                    mProgressDialog.dismiss()
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }
}