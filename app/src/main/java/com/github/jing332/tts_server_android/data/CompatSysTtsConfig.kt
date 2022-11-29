package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.util.FileUtils
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.Serializable

/* 旧配置 已弃用*/
@kotlinx.serialization.Serializable
data class CompatSysTtsConfig(
    var list: ArrayList<CompatSysTtsConfigItem>,
    var isSplitSentences: Boolean = true,
    var isMultiVoice: Boolean = false,
    var isReplace: Boolean = false,
    var timeout: Int = 5000,
    var minDialogueLength: Int = 0
) {
    companion object {
        private val filepath by lazy { "${App.context.filesDir.absolutePath}/system_tts_config.json" }
        fun read(): CompatSysTtsConfig? {
            return try {
                val file = File(filepath)
                if (!FileUtils.fileExists(file)) return null

                val str = File(filepath).readText()
                App.jsonBuilder.decodeFromString<CompatSysTtsConfig>(str)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        /**
         * 迁移旧的配置结构
         */
        fun migrationConfig(): Boolean {
            val compatConfig = read()
            compatConfig?.apply {
                list.forEach {
                    appDb.sysTtsDao.insert(
                        SysTts(
                            readAloudTarget = it.readAloudTarget,
                            tts = it.voiceProperty,
                            displayName = it.uiData.displayName,
                            isEnabled = it.isEnabled
                        )
                    )
                }
                SysTtsConfig.isMultiVoiceEnabled = isMultiVoice
                SysTtsConfig.isSplitEnabled = isSplitSentences
                SysTtsConfig.requestTimeout = timeout

                return deleteConfigFile()
            }
            return false
        }

        /**
         * return 是否成功
         */
        private fun deleteConfigFile(): Boolean {
            return try {
                File(filepath).delete()
            } catch (e: Exception) {
                return false
            }
        }
    }
}

/* 旧配置 已弃用*/
@kotlinx.serialization.Serializable
data class CompatSysTtsConfigItem(
    var uiData: UiData, /* UI显示数据 */
    var isEnabled: Boolean = false,  /* 是否启用 */
    @ReadAloudTarget var readAloudTarget: Int = ReadAloudTarget.DEFAULT,
    var voiceProperty: MsTTS, /* 朗读属性 */
) : Serializable {
    @kotlinx.serialization.Serializable
    data class UiData(
        var displayName: String,
    ) : Serializable
}