package code.roy.retromusic.ext

import android.app.Activity
import android.content.Intent
import android.net.Uri
import code.roy.retromusic.R

fun Activity.rateApp(
    packageName: String? = null
) {
    if (packageName.isNullOrEmpty()) {
        return
    }
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")
            )
        )
    } catch (e: android.content.ActivityNotFoundException) {
        e.printStackTrace()
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun Activity.moreApp(
    nameOfDeveloper: String = "Roy93Group"
) {
    val uri = "https://play.google.com/store/apps/developer?id=$nameOfDeveloper"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    this.startActivity(intent)
}

fun Activity.shareApp(
) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
        var sAux = "\nỨng dụng này rất bổ ích, thân mời bạn tải về cài đặt để trải nghiệm\n\n"
        sAux = sAux + "https://play.google.com/store/apps/details?id=" + this.packageName
        intent.putExtra(Intent.EXTRA_TEXT, sAux)
        this.startActivity(Intent.createChooser(intent, "Vui lòng chọn"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.share(
    msg: String
) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
        // String sAux = "\nỨng dụng này rất bổ ích, thân mời bạn tải về cài đặt để trải nghiệm\n\n";
        // sAux = sAux + "https://play.google.com/store/apps/details?id=" + activity.getPackageName();
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        this.startActivity(Intent.createChooser(intent, "Share via"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
