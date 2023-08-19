package code.roy.retromusic.activities.bugreport.model

import androidx.annotation.Keep
import code.roy.retromusic.activities.bugreport.model.github.ExtraInfo

@Keep
class Report(
    val title: String,
    private val description: String,
    private val deviceInfo: DeviceInfo?,
    private val extraInfo: ExtraInfo,
) {
    fun getDescription(): String {
        return """
            $description
            
            -
            
            ${deviceInfo?.toMarkdown()}
            
            ${extraInfo.toMarkdown()}
            """.trimIndent()
    }
}
