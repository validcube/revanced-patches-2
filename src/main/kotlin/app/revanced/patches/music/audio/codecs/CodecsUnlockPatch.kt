package app.revanced.patches.music.audio.codecs

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.music.audio.codecs.fingerprints.AllCodecsReferenceFingerprint
import app.revanced.patches.music.audio.codecs.fingerprints.CodecsLockFingerprint
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    description = "Adds more audio codec options. The new audio codecs usually result in better audio quality.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            [
                "6.45.54",
                "6.51.53",
                "7.01.53",
                "7.02.52",
                "7.03.52",
                "7.04.54",
                "7.06.54",
                "7.07.52",
                "7.08.54",
                "7.10.52",
                "7.11.51",
                "7.13.53",
                "7.14.52",
                "7.15.51",
            ]
        )
    ]
)
@Deprecated("This patch is no longer needed as the feature is now enabled by default.")
object CodecsUnlockPatch : BytecodePatch(
    setOf(CodecsLockFingerprint, AllCodecsReferenceFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        val codecsLockResult = CodecsLockFingerprint.result!!

        val implementation = codecsLockResult.mutableMethod.implementation!!

        val scanResultStartIndex = codecsLockResult.scanResult.patternScanResult!!.startIndex
        val instructionIndex = scanResultStartIndex +
            if (implementation.instructions[scanResultStartIndex - 1].opcode == Opcode.CHECK_CAST) {
                // for 5.16.xx and lower
                -3
            } else {
                // since 5.17.xx
                -2
            }

        val allCodecsResult = AllCodecsReferenceFingerprint.result!!
        val allCodecsMethod =
            context.toMethodWalker(allCodecsResult.method)
                .nextMethod(allCodecsResult.scanResult.patternScanResult!!.startIndex)
                .getMethod()

        implementation.replaceInstruction(
            instructionIndex,
            "invoke-static {}, ${allCodecsMethod.definingClass}->${allCodecsMethod.name}()Ljava/util/Set;".toInstruction(),
        )
    }
}
