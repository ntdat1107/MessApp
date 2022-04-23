package com.example.messapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messapp.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private var binding: ActivityForgotPasswordBinding? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar!!.title = "Reset password"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("Sending email...")

        mAuth = FirebaseAuth.getInstance()

        binding?.btnSend!!.setOnClickListener {
            val email = binding?.etEmail!!.text.toString()
            if (TextUtils.isEmpty(email.trim { it <= ' ' })) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            } else {
                mProgressDialog.show()
                mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        mProgressDialog.dismiss()
                        Toast.makeText(this, "Send email successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        mProgressDialog.dismiss()
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}