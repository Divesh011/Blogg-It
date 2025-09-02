package com.divcode.bloggit.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.divcode.bloggit.Activities.ViewBlogActivity
import com.divcode.bloggit.R
import com.divcode.bloggit.Utils.Blog
import com.divcode.bloggit.Utils.CommonUtils
import com.divcode.bloggit.Utils.FireStoreUtility

class BlogAdapter(val context: Context, var blogs: MutableList<Blog>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.blogpostcard, parent, false)
        return BlogViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {

        val currentBlog = blogs[position]

        holder.blogTitle.text = currentBlog.title
        holder.viewsTv.text = currentBlog.views.toString()
        holder.blogContent.text = currentBlog.content
        holder.likesTv.text = currentBlog.likes.toString()
        holder.date.text = currentBlog.date
        holder.moreOptions.visibility = View.INVISIBLE

        val uid = currentBlog.userId

        // check if profile is already loaded if yes then use that otherwise make network call
        if (CommonUtils.loadedProfiles[uid] == null) {
            FireStoreUtility.getProfile(uid) { path ->
                Glide.with(context)
                    .asBitmap()
                    .load(path)
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            CommonUtils.loadedProfiles.put(uid, resource)
                            holder.profilePicture.setImageBitmap(resource)
                            Log.d("HOMEFRAG", "Glide Called")
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })
            }
        }else{
            holder.profilePicture.setImageBitmap(CommonUtils.loadedProfiles[uid])
        }
    }

    override fun getItemCount(): Int {
        return blogs.size
    }

    inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            // to open the Blog and start ViewBlogActivity
            itemView.setOnClickListener {
                val blogIndex = CommonUtils.loadedBlogs.indexOf(
                    blogs[adapterPosition]
                )
                Intent(context, ViewBlogActivity::class.java).apply{
                    putExtra("CURRENTBLOG", blogIndex)
                    if (CommonUtils.loadedBlogs[blogIndex].userId != CommonUtils.currentUser?.uid.toString()) {
                        FireStoreUtility.increaseViews(
                            CommonUtils.loadedBlogs[blogIndex].userId,
                            CommonUtils.loadedBlogs[blogIndex].title,
                            CommonUtils.loadedBlogs[blogIndex].views
                        )
                        CommonUtils.loadedBlogs[blogIndex].views += 1
                    }
                    context.startActivity(this)
                }
            }
        }
        val blogTitle: TextView = itemView.findViewById(R.id.blogTitle)
        val blogContent: TextView = itemView.findViewById(R.id.BlogContentShort)
        val date: TextView = itemView.findViewById(R.id.dateTv)
        val likesTv: TextView = itemView.findViewById(R.id.likesTv)
        val viewsTv: TextView = itemView.findViewById(R.id.viewsTv)
        val profilePicture: ImageView = itemView.findViewById(R.id.profile_image)

        val moreOptions: ImageView = itemView.findViewById(R.id.moreOptions)

    }

    }
