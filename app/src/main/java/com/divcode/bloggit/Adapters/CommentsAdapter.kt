package com.divcode.bloggit.Adapters

import android.content.Context
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
import com.divcode.bloggit.R
import com.divcode.bloggit.Utils.Comment
import com.divcode.bloggit.Utils.CommonUtils
import com.divcode.bloggit.Utils.FireStoreUtility
import com.google.firebase.firestore.FirebaseFirestore

class CommentsAdapter(val comments: MutableList<Comment>, val context: Context): RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.comment_card, parent, false)
        return CommentsViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        Log.d("FIRESTOREUTILITY", "Comment Card Created/Binded")
        val currentComment = comments[position]
        holder.commentContent.text = currentComment.comment
        holder.dateTv.text = currentComment.date

        FireStoreUtility.getUserName(currentComment.userId){result ->
            holder.userName.text = result
        }
        if(CommonUtils.loadedProfiles.contains(currentComment.userId)){
            holder.profilePic.setImageBitmap(CommonUtils.loadedProfiles[currentComment.userId])
        }else{
            FireStoreUtility.getProfile(currentComment.userId){ path ->
                Glide.with(context).asBitmap()
                    .load(path)
                    .into<CustomTarget<Bitmap>>(object: CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        holder.profilePic.setImageBitmap(resource)
                        CommonUtils.loadedProfiles.put(currentComment.userId, resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Glide.with(context).clear(holder.profilePic)
                    }

                })
            }
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CommentsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val profilePic: ImageView = itemView.findViewById(R.id.comment_profile_image)
        val commentContent: TextView = itemView.findViewById(R.id.comment_content)
        val dateTv: TextView = itemView.findViewById(R.id.comment_date)
        val userName: TextView = itemView.findViewById(R.id.comment_user_name)
    }
}