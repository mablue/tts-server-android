package com.github.jing332.tts_server_android.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(tableName = "replaceRule")
data class ReplaceRule(
    @kotlinx.serialization.Transient
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var name: String, //显示名称
    var isEnabled: Boolean = true, //是否启用
    var isRegex: Boolean = false, //是否正则
    var pattern: String, //匹配
    var replacement: String //替换为
) : java.io.Serializable