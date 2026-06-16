#!/usr/bin/env kotlin

/**
 * バージョンの検証
 * @author MORIMORI0317
 */

@file:DependsOn("com.vdurmont:semver4j:3.1.0")

import com.vdurmont.semver4j.Semver
import com.vdurmont.semver4j.SemverException

val tag = args[0]
val allTag = args[1]

val version = tag.substring(1)
val allVersions = allTag.lines()
    .filter { it != tag }
    .map { it.substring(1) }

println("バージョン: $version")
println("全バージョン: $allVersions")
println()


fun parseSemver(versionText: String): Semver {
    try {
        return Semver(versionText)
    } catch (_: SemverException) {
    }

    // Semverを採用する前のバージョン文字列だった場合に変換を試みる (1.8 -> 1.8.0)
    if (versionText.split(".").size == 2) {
        try {
            return Semver("$versionText.0")
        } catch (_: SemverException) {
        }
    }

    throw IllegalStateException("Semver変換失敗: ${versionText}")
}

val versionSemver = Semver(version)
val allVersionsSemver = allVersions.map { parseSemver(it) }

if (versionSemver.build != null) {
    throw Exception("ビルドナンバーが含まれています: ${versionSemver.build}")
}

val verSuffixes: Array<String> = versionSemver.suffixTokens
val availableSuffixes = listOf("alpha", "beta")

if (verSuffixes.isNotEmpty()) {
    // サフィックス検証

    if (!availableSuffixes.contains(verSuffixes[0])) {
        throw Exception("サフィックスは${availableSuffixes.joinToString(", ")}のいずれかである必要があります")
    }

    if (verSuffixes.size != 2 || verSuffixes[0].isNumber() || !verSuffixes[1].isNumber()) {
        throw Exception("サフィックスはreleaseType.numberにする必要があります")
    }

    // beta.1が存在しない場合にbeta.2が付けられた場合
    val suffixNumber = verSuffixes[1].toInt()
    if (suffixNumber > 1) {
        val preVersion =
            Semver("${versionSemver.major}.${versionSemver.minor}.${versionSemver.patch}-${verSuffixes[0]}.${suffixNumber - 1}")
        if (!allVersionsSemver.contains(preVersion)) {
            throw Exception("以前のバージョンが存在しません: $preVersion")
        }
    }

    val stabilityOrder = listOf("release", "beta", "alpha")

    allVersionsSemver
        .forEach {
            if (toVersionOnly(it) != toVersionOnly(versionSemver)) {
                return@forEach
            }

            var itFirstSuffix = "release"
            if (it.suffixTokens.isNotEmpty() && stabilityOrder.contains(it.suffixTokens[0])) {
                itFirstSuffix = it.suffixTokens[0]
            }

            val verFirstSuffixOrder = stabilityOrder.indexOf(verSuffixes[0])
            val itFirstSuffixOrder = stabilityOrder.indexOf(itFirstSuffix)

            // beta.1が既に存在する状態でalpha.1が付けられた場合など
            if (itFirstSuffixOrder < verFirstSuffixOrder) {
                throw Exception("さらに安定しているバージョンが存在します: $it")
            }

            // beta.3が既に存在する状態でbeta.1が付けられた場合など
            if (itFirstSuffix == verSuffixes[0]) {
                var suffixNumber = 0
                if (it.suffixTokens.size == 2 && it.suffixTokens[1].isNumber()) {
                    suffixNumber = it.suffixTokens[1].toInt()

                    if (suffixNumber >= verSuffixes[1].toInt()) {
                        throw Exception("さらに新しいサフィックス番号のバージョンが存在します: $it")
                    }
                }
            }
        }
}

if (allVersionsSemver.isNotEmpty()) {

    val latestVer = allVersionsSemver.maxWith { a, b -> a.compareTo(b) }
    if (latestVer.isGreaterThan(versionSemver)) {
        throw Exception("さらに新しいバージョンが存在します: $latestVer")
    }

    val preDictPreVer = versionSemver.let {
        if (it.patch > 0) {
            return@let Semver("${it.major}.${it.minor}.${it.patch - 1}")
        } else {
            if (it.minor > 0) {
                return@let Semver("${it.major}.${it.minor - 1}.0")
            } else {
                if (it.major <= 0) {
                    throw Exception("0.0.0!?")
                }
                return@let Semver("${it.major - 1}.0.0")
            }
        }
    }

    val allNonSuffixVersions = allVersionsSemver.map { Semver(toVersionOnly(it)) }.distinct()
    if (allNonSuffixVersions.any { it.isLowerThan(preDictPreVer) } && allNonSuffixVersions.none {
            it.isGreaterThanOrEqualTo(preDictPreVer) && it.isLowerThan(version)
        }) {
        throw Exception("以前のバージョンが存在しません: $preDictPreVer")
    }
} else {
    println("初期バージョン")
}

fun toVersionOnly(targetSemVer: Semver): String {
    return "${targetSemVer.major}.${targetSemVer.minor}.${targetSemVer.patch}"
}

fun String.isNumber(): Boolean {
    return this.all { r -> r.isDigit() }
}