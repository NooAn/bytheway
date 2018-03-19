package ru.a1024bits.bytheway.util

import android.widget.Toast
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters
import com.vk.sdk.api.VKApi
import com.vk.sdk.VKAccessToken
import android.support.v4.app.FragmentActivity
import com.vk.sdk.dialogs.VKShareDialog
import android.content.Intent
import android.net.Uri
import android.support.v4.view.MenuItemCompat.setContentDescription
import android.util.Log
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk

import com.vk.sdk.*;
import com.vk.sdk.dialogs.VKShareDialogBuilder

/**
 * Created by Andrei_Gusenkov on 3/19/2018.
 */
interface Callback {
    fun onSuccess(obj: Any)

    fun onError(error: String)
}

enum class Social {
    VK, FB, TW;

    fun share(activity: FragmentActivity, text: String, link: String, imageLink: String?, nameObj: String, lat: Double, lon: Double) {
        when (this) {
            VK -> {
                run {

                    // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://vkontakte.ru/share.php?url="+"http://play.google.com/store/apps/details?id="+activity.getPackageName()+"&title="+nameObj));
                    // activity.startActivity(browserIntent);

                    //                 String[] fingerprints = VKUtil.getCertificateFingerprint(activity, activity.getPackageName());
                    //                 Log.v("dibr debug hash",fingerprints[0]);
                    // AppDialog.createSimple1BtnDialog(activity,fingerprints[0],"","",null).show();

                    if (!VKSdk.isLoggedIn()) {
                        VKSdk.login(activity, *arrayOf(VKScope.WALL, VKScope.NOHTTPS, VKScope.OFFLINE))
                        vkPoster = object : Callback {
                            override fun onSuccess(obj: Any) {
                                Log.e("LOG", " onSuccess -> postToWall")
                                postToWall(activity, text, link, nameObj)
                            }

                            override fun onError(error: String) {
                                Log.e("LOG", " onError in vk api Callback")
                            }
                        }
                    } else {
                        postToWall(activity, text, link, nameObj)
                    }

                }
                return
            }
            FB -> {

//                val shareContent = ShareLinkContent.Builder()
//                shareContent.setContentDescription(text)
//                        .setContentTitle(nameObj)
//                shareContent.setContentUrl(Uri.parse(link))
//                if (imageLink != null && imageLink != "")
//                    shareContent.setImageUrl(Uri.parse(imageLink))
//                ShareDialog(activity).show(shareContent.build())

                return
            }
            TW -> {
                //один из линков всегда пусто, так что сумма линков - это актуальный линк
                //    String url = "http://twitter.com/share?url=" + Uri.encode(link + imageLink) + "&text=" + Uri.encode(text);
                val url = "http://twitter.com/share?text=" + Uri.encode(text)
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
            }
        }
    }

    private fun postToWall(activity: FragmentActivity, text: String, link: String, nameObj: String) {
        val dialog = VKShareDialog().setText(text)
        dialog.setAttachmentLink(nameObj, link)
        //   if (imageLink != null && !imageLink.equals(""))
        //      dialog.setAttachmentLink(nameObj, imageLink);
        dialog.show(activity.supportFragmentManager, "VK_SHARE_DIALOG")
    }

    private fun shareVk(text: String, ac: FragmentActivity, lat: Double, lon: Double) {
        val postToId = VKAccessToken.currentToken().userId
        val request = VKApi.wall().post(VKParameters.from(
                VKApiConst.OWNER_ID, postToId,
                VKApiConst.MESSAGE, text,
                VKApiConst.LAT, lat,
                VKApiConst.LONG, lon))
        // VKApiConst.ATTACHMENTS, " http://resources.guide.toptriptip.com/14bfa6bb14875e45bba028a21ed38046/clips/21-28-8.jpg"));
        request.attempts = 5
        request.executeWithListener(object : VKRequest.VKRequestListener() {
            override fun onComplete(response: VKResponse?) {
                Log.w("LOG", "Wall response  " + response!!.responseString)
                Toast.makeText(ac, "Запись отправлена", Toast.LENGTH_SHORT).show()
            }

            override fun attemptFailed(request: VKRequest?, attemptNumber: Int, totalAttempts: Int) {
                Log.d("LOG", "Wall response failed on $attemptNumber")
            }

            override fun onError(error: VKError?) {
                Log.e("LOG", "Wall error response  " + error!!.toString())
                Toast.makeText(ac, "Ошибка", Toast.LENGTH_SHORT).show()
            }

        })
    }

    companion object {
        var vkPoster: Callback? = null
            private set
    }
}