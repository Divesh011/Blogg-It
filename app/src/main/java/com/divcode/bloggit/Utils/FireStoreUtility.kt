package com.divcode.bloggit.Utils

import android.util.Log

//Single Ton Class which Provides all necessary network call functions to upload, get, delete profile, blogs and Comments and add User to database
object FireStoreUtility {
    private var user = CommonUtils.currentUser

    //For debugging
    private val TAG = "FIRESTOREUTILITY"

    // all the functions are Firestore functions Asynchronously called so the order of execution of program was not as expected like it updated blog UI before the blog was even
    // available
    // to solve this, each method takes a lambda function and call the lambda function every time the asynchronous result is available. I updated the UI through these lambda functions

    // this method will fetch all the blogs from firestore database
    fun getBlogs(callBack: (blog: Blog)->Unit) {
        var blog: Blog

        CommonUtils.getDBInstance().collection("users/").get()
            .addOnSuccessListener { documentSnapshots ->
                for (userid in documentSnapshots) {

                    CommonUtils.getDBInstance().collection("users/${userid.id}/Blogs")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                blog = Blog(userid.id, document.id, document.data["Content"].toString(), date =document.data["Date"].toString(), views = document.data["Views"]as Long, likes = document.data["Likes"] as Long, category = document.data["Category"].toString())
                                callBack(blog)
                            }
0
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting Blogs.", exception)
                        }
                }
            }
            .addOnFailureListener {

            }
    }

    //this method will provide the user profile based on the user ID
    fun getProfile(uid: String, callBack: (String) -> Unit){
        CommonUtils.getDBInstance().document("users/$uid/").get().addOnSuccessListener { result ->
            callBack(result.data?.get("ProfilePic").toString())
        }
    }

//    this method will provide the user Name based on the user ID
    fun getUserName(uid: String, callBack: (String) -> Unit){
        CommonUtils.getDBInstance().document("users/$uid/").get().addOnSuccessListener { result ->
            callBack(result.data?.get("DisplayName").toString())
            Log.d(TAG, result.data.toString())
        }
    }

//    this method will provide all the comments of a Blog based on the user ID, Blog Title
    fun getComments(uid: String, blogTitle: String, callBack: (comment: Comment) -> Unit){
        CommonUtils.getDBInstance().collection("users/${uid}/Blogs/$blogTitle/comments/").get()
            .addOnSuccessListener { result->
                for (document in result){
                    callBack(Comment(document.data["uid"].toString(), document.data["comment"].toString(), document.data["date"].toString()))
                    Log.d(TAG, "Comment loaded")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to load comment")
            }
    }

//    this method will create a new Blog document in FireStore database
    fun postBlog(blog: Blog, callBack: (Unit) -> Unit){
        val blogMap: HashMap<String, Any> =  hashMapOf("Content" to blog.content, "Date" to blog.date, "Category" to blog.category, "Likes" to blog.likes, "Views" to blog.views)
        CommonUtils.getDBInstance().collection("users").document(user?.uid.toString()).collection("Blogs").document( blog.title)
            .set(blogMap)
            .addOnSuccessListener {
                callBack(Unit)
                return@addOnSuccessListener
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to Post Blog")
                return@addOnFailureListener
            }
    }

    // deletes specific Blog
    fun deleteBlog(blog: Blog, callBack: (Unit) -> Unit)
    {
        //first delete the comments
        CommonUtils.getDBInstance().collection("users").document(user?.uid.toString()).collection("Blogs").document( blog.title).collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                for (result in documents) {
                    CommonUtils.getDBInstance()
                        .document("users/${blog.userId}/Blogs/${blog.title}/comments/${result.id}/").delete()
                    CommonUtils.userLikedBlogs.remove("${blog.userId}/${blog.title}")
                    updateUsersLikedBlogs()
                }

            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to Delete Blog")
                return@addOnFailureListener
            }

        // delete the blog entirely now
        CommonUtils.getDBInstance().collection("users").document(user?.uid.toString()).collection("Blogs").document( blog.title)
            .delete()
            .addOnSuccessListener {
                callBack(Unit)
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to Delete Blog")
                return@addOnFailureListener
            }

    }

    // adds comment for a blog
    fun sendComment(uid:String, blogTitle: String, comment: Comment, callBack: (Unit) -> Unit){
        CommonUtils.getDBInstance().collection("users/${comment.userId}/Blogs/$blogTitle/comments/").add(hashMapOf("uid" to uid,
            "comment" to comment.comment, "date" to comment.date))
            .addOnSuccessListener {
                callBack(Unit)
            }
            .addOnFailureListener {  }
    }

    // increases views of particular blog
    fun increaseViews(uid: String, blogTitle: String, currentViews: Long){
        CommonUtils.getDBInstance().document("users/$uid/Blogs/$blogTitle/").update("Views", currentViews+1)
    }

    // increases Likes of particular blog
    fun increaseLike(uid: String, blogTitle: String, currentLikes: Long) {
        CommonUtils.getDBInstance().document("users/$uid/Blogs/$blogTitle/")
            .update("Likes", currentLikes + 1)
    }

    // Decreases Like of particular blog
    fun decreaseLike(uid: String, blogTitle: String, currentLikes: Long){
        CommonUtils.getDBInstance().document("users/$uid/Blogs/$blogTitle/").update("Likes", currentLikes-1)
    }

//    Updates the blog
    fun updateBlog(oldBlog: Blog, newBlog: Blog, callBack: (Unit) -> Unit){

        //check if updated blog's Title is equal to old blog's Title or not
        // if yes then we only need to replace the old blog with new blog only
        // but if they are not equal then we have to create a new blog with contents of updated Blog and also add old blogs's comments to it then delete the old blog

        if(oldBlog.title == newBlog.title) {
            CommonUtils.getDBInstance().collection("users").document(user?.uid.toString())
                .collection("Blogs").document(oldBlog.title)
                .delete()
                .addOnSuccessListener {
                    postBlog(newBlog) {
                        callBack(Unit)
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to Update Blog")
                    return@addOnFailureListener
                }
        }
        else{
            postBlog(newBlog) {
                getComments(newBlog.userId, oldBlog.title){comment ->
                    sendComment(newBlog.userId, newBlog.title, comment){
                    }
                }
            }

            deleteBlog(oldBlog){
                callBack(Unit)
            }
        }
    }

    //update the list of blogs that current user has liked
    fun updateUsersLikedBlogs(){
        CommonUtils.getDBInstance().collection("users")
            .document(user?.uid.toString())
            .update("LikedBlogs", CommonUtils.userLikedBlogs as List<String>)
    }

    // provides the list of blogs that current user has liked
    fun getUserLikedBlogs(){
        CommonUtils.getDBInstance().collection("users")
            .document(user?.uid.toString())
            .get()
            .addOnSuccessListener { documentReference ->
                CommonUtils.userLikedBlogs = (documentReference.data?.get("LikedBlogs") as? MutableList<String>)!!
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error adding document", exception)
            }
    }

    // adds a newly signed in user to database
    fun addUser(callBack: (Unit) -> Unit){
        user = CommonUtils.currentUser
        var userFound = false
        CommonUtils.getDBInstance().collection("users").get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "USER FOUND")
                for (result in document) {
                    if(result.id == user?.uid){
                        userFound = true
                        break
                    }
                }
                if (userFound){
                    callBack(Unit)
                }else{
                    Log.d(TAG, "ADD USER CALLED")
                    val u1 = hashMapOf("DisplayName" to user?.displayName.toString(), "ProfilePic" to user?.photoUrl.toString(), "LikedBlogs" to listOf<String>())
                    CommonUtils.getDBInstance().collection("users")
                        .document(user?.uid.toString())
                        .set(u1)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added}")
                            callBack(Unit)
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error adding document", exception)
                        }
                }
            }
    }
}