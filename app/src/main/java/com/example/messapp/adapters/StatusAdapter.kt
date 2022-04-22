package com.example.messapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messapp.activities.MainActivity
import com.example.messapp.databinding.ItemUserStatusBinding
import com.example.messapp.models.UserStatus
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory

class StatusAdapter(
    private val context: Context,
    private val userStatusList: ArrayList<UserStatus>
) : RecyclerView.Adapter<StatusAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemUserStatusBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserStatusBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userStatus = userStatusList[position]

        val lastStatus = userStatus.statusList.last()
        Glide.with(context).load(lastStatus.image).centerCrop().into(holder.binding.circleImg)

        holder.binding.tvName.text = userStatus.name

        holder.binding.circularStatusView.setPortionsCount(userStatus.statusList.size)

        holder.binding.circularStatusView.setOnClickListener {
            val myStories = ArrayList<MyStory>()
            for (status in userStatus.statusList) {
                myStories.add(MyStory(status.image))
            }
            StoryView.Builder((context as MainActivity).supportFragmentManager)
                .setStoriesList(myStories)
                .setStoryDuration(3000)
                .setTitleText(userStatus.name)
                .setTitleLogoUrl(userStatus.profileImage)
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onTitleIconClickListener(position: Int) {
                        TODO("Not yet implemented")
                    }

                }).build()
                .show()
        }
    }

    override fun getItemCount(): Int {
        return userStatusList.size
    }
}