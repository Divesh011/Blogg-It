package com.divcode.bloggit.Adapters

import com.divcode.bloggit.R
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.divcode.bloggit.Activities.PublishBlogActivity
import com.divcode.bloggit.Activities.ViewBlogActivity
import com.divcode.bloggit.Utils.Blog
import com.divcode.bloggit.Utils.CommonUtils
import com.divcode.bloggit.Utils.FireStoreUtility

class MyBlogsAdapter(val myBlogs: MutableList<Blog>, val context: Context) : RecyclerView.Adapter<MyBlogsAdapter.MyBlogsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBlogsViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.blogpostcard, parent, false)
        return MyBlogsViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: MyBlogsViewHolder, position: Int) {
        val currentBlog = myBlogs[position]

        holder.blogTitle.text = currentBlog.title
        holder.viewsTv.text = currentBlog.views.toString()
        holder.blogContent.text = currentBlog.content
        holder.likesTv.text = currentBlog.likes.toString()
        holder.date.text = currentBlog.date

        val uid = currentBlog.userId
        Log.d("MYBLOGFRAG", "Creating")

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
        return myBlogs.size
    }

    inner class MyBlogsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        init {
            itemView.setOnClickListener {
                Intent(context, ViewBlogActivity::class.java).apply{
                    putExtra("CURRENTBLOG", myBlogs[adapterPosition])
                    FireStoreUtility.increaseViews(myBlogs[adapterPosition].userId, myBlogs[adapterPosition].title, myBlogs[adapterPosition].views)
                    myBlogs[adapterPosition].views += 1
                    context.startActivity(this)
                }
            }

            itemView.findViewById<ImageView>(R.id.moreOptions).setOnClickListener {
                val popupMenu = PopupMenu(context, it)
                popupMenu.inflate(R.menu.blog_options_menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId){
                        R.id.DeleteBlog -> {
                            FireStoreUtility.deleteBlog(myBlogs[adapterPosition]){
                                CommonUtils.loadedBlogs.remove(myBlogs[adapterPosition])
                                myBlogs.removeAt(adapterPosition)
                                notifyItemRemoved(adapterPosition)
                                notifyItemChanged(adapterPosition)
                                notifyItemRangeChanged(0, myBlogs.size-1)
                                Toast.makeText(context, "Blog Deleted!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        R.id.EditBlog -> {
                            Intent(context, PublishBlogActivity::class.java).apply{
                                putExtra("PURPOSE", "EDITBLOG")
                                putExtra("BLOGTOEDIT", myBlogs[adapterPosition])
                                context.startActivity(this)
                            }

                        }
                    }
                    return@setOnMenuItemClickListener true
                }
            }
            Log.d("MYBLOGFRAG", "ViewHolder")
        }
        
        val blogTitle: TextView = itemView.findViewById(R.id.blogTitle)
        val blogContent: TextView = itemView.findViewById(R.id.BlogContentShort)
        val date: TextView = itemView.findViewById(R.id.dateTv)
        val likesTv: TextView = itemView.findViewById(R.id.likesTv)
        val viewsTv: TextView = itemView.findViewById(R.id.viewsTv)
        val profilePicture: ImageView = itemView.findViewById(R.id.profile_image)

        
    }

}