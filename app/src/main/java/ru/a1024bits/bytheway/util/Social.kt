package ru.a1024bits.bytheway.util

import android.app.Activity
import android.graphics.Bitmap
import android.support.v4.app.FragmentActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crash.FirebaseCrash
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import com.vk.sdk.api.photo.VKImageParameters
import com.vk.sdk.api.photo.VKUploadImage
import com.vk.sdk.dialogs.VKShareDialog
import com.vk.sdk.dialogs.VKShareDialogBuilder
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment


/**
 * Created by Andrei_Gusenkov on 3/19/2018.
 */
class VK {
    /**
     * init vk_app and auth user
     */
    fun start(activity: Activity): Boolean {
        if (!VKSdk.isLoggedIn()) {
            VKSdk.login(activity, VKScope.WALL, VKScope.EMAIL, VKScope.NOHTTPS, VKScope.OFFLINE, VKScope.PHOTOS)
            return true;
        }
        return false;
    }

    fun postToWall(activity: FragmentActivity, text: String, bitmap: Bitmap? = null, linkUri: String) {

        val build = VKShareDialogBuilder()
                .setText(text)
                .setAttachmentLink("Опубликованно с помощью ByTheWay", linkUri) // fixme locale
                .setShareDialogListener(object : VKShareDialog.VKShareDialogListener {
                    override fun onVkShareComplete(postId: Int) {
                        FirebaseAnalytics.getInstance(activity).logEvent("${MyProfileFragment.TAG_ANALYTICS}_vkcomplete", null)
                    }

                    override fun onVkShareCancel() {
                        FirebaseAnalytics.getInstance(activity).logEvent("${MyProfileFragment.TAG_ANALYTICS}_vk_cancel", null)
                    }

                    override fun onVkShareError(error: VKError) {
                        FirebaseCrash.report(error.httpError)
                        FirebaseAnalytics.getInstance(activity).logEvent("${MyProfileFragment.TAG_ANALYTICS}_vk_error", null)
                    }
                })
        bitmap?.let {
            build.setAttachmentImages(arrayOf(VKUploadImage(it, VKImageParameters.pngImage())))
        }
        build.show(activity.fragmentManager, "VK")
    }
}
