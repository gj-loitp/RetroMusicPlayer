package code.roy.retromusic.activities.bugreport.model.github

import android.text.TextUtils
import androidx.annotation.Keep

@Keep
class GithubLogin {
    val apiToken: String?
    val password: String?
    val username: String?

    constructor(username: String?, password: String?) {
        this.username = username
        this.password = password
        apiToken = null
    }

    constructor(apiToken: String?) {
        username = null
        password = null
        this.apiToken = apiToken
    }

    fun shouldUseApiToken(): Boolean {
        return TextUtils.isEmpty(username) || TextUtils.isEmpty(password)
    }
}
