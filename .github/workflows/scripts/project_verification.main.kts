#!/usr/bin/env kotlin

/**
 * プロジェクトの検証
 * @author MORIMORI0317
 */


import java.io.File

val changeLog = args[0]

if (changeLog.lines().filter { it.trim().isNotEmpty() }
        .none { !it.trim().startsWith("### ") && it.trim().startsWith("- ") })
    throw Exception("チェンジログを記入してください。")

fun assertFile(pathStr: String) {
    if (!File(pathStr).exists())
        throw Exception("Required file does not exist/必要なファイルが存在しません: $pathStr")
}

assertFile("CHANGELOG.md")

println("Project verification completed!")
