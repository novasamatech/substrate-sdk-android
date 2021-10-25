package jp.co.soramitsu.fearless_utils.icon

import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import org.spongycastle.jcajce.provider.digest.Blake2b
import java.lang.RuntimeException
import java.lang.StringBuilder
import kotlin.math.floor
import kotlin.math.sqrt

class IconGenerator {

    companion object {
        private val SCHEMES = mutableListOf(
            Scheme("target", 1, arrayOf(0, 28, 0, 0, 28, 0, 0, 28, 0, 0, 28, 0, 0, 28, 0, 0, 28, 0, 1)),
            Scheme("cube", 20, arrayOf(0, 1, 3, 2, 4, 3, 0, 1, 3, 2, 4, 3, 0, 1, 3, 2, 4, 3, 5)),
            Scheme("quazar", 16, arrayOf(1, 2, 3, 1, 2, 4, 5, 5, 4, 1, 2, 3, 1, 2, 4, 5, 5, 4, 0)),
            Scheme("flower", 32, arrayOf(0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 3)),
            Scheme("cyclic", 32, arrayOf(0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 6)),
            Scheme("vmirror", 128, arrayOf(0, 1, 2, 3, 4, 5, 3, 4, 2, 0, 1, 6, 7, 8, 9, 7, 8, 6, 10)),
            Scheme("hmirror", 128, arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 8, 6, 7, 5, 3, 4, 2, 11))
        )

        private const val MAIN_RADIUS = 32.0
        private const val RADIUS = 5
    }

    fun getSvgImage(
        id: ByteArray,
        sizeInPixels: Int,
        isAlternative: Boolean = false,
        backgroundColor: Int = "eeeeee".toInt(radix = 16)
    ): PictureDrawable {
        val circles = generateIconCircles(id, isAlternative, backgroundColor)

        val stringBuilder = StringBuilder()
        stringBuilder.append("<svg ")
        stringBuilder.append("viewBox='0 0 64 64' ")
        stringBuilder.append("width='$sizeInPixels' ")
        stringBuilder.append("height='$sizeInPixels' ")
        stringBuilder.append(">")
        circles.forEach {
            stringBuilder.append("<circle cx='${it.x}' cy='${it.y}' r='${it.radius}' fill='${it.colorString}' />")
        }
        stringBuilder.append("</svg>")
        val svg = SVG.getFromString(stringBuilder.toString())
        return PictureDrawable(svg.renderToPicture())
    }

    private fun generateIconCircles(
        id: ByteArray,
        isAlternative: Boolean = false,
        backgroundColor: Int
    ): List<Circle> {

        val r1 = if (isAlternative) {
            MAIN_RADIUS / 8 * 5
        } else {
            MAIN_RADIUS / 4 * 3
        }

        val rroot3o2 = r1 * sqrt(3.0) / 2
        val ro2 = r1 / 2
        val rroot3o4 = r1 * sqrt(3.0) / 4
        val ro4 = r1 / 4
        val r3o4 = r1 * 3 / 4

        val totalFreq = SCHEMES.fold(0) { summ, schema -> summ + schema.frequency }

        val zeroHash = Blake2b.Blake2b512().digest(ByteArray(32) { 0 })
        val idHash = Blake2b.Blake2b512().digest(id).mapIndexed { index, byte -> (byte + 256 - zeroHash[index]) % 256 }

        val sat = (floor(idHash[29].toDouble() * 70 / 256 + 26) % 80) + 30
        val d = floor((idHash[30].toDouble() + idHash[31].toDouble() * 256) % totalFreq)
        val scheme = findScheme(d.toInt())

        val palette = idHash.mapIndexed { index, byte ->
            val b = (byte + index % 28 * 58) % 256
            var resultColor = ""
            resultColor = when (b) {
                0 -> {
                    "#444"
                }
                255 -> {
                    "transparent"
                }
                else -> {
                    val h = floor(b.toDouble() % 64 * 360 / 64)
                    val array = arrayOf(53, 15, 35, 75)
                    val l = array[floor(b.toDouble() / 64).toInt()]
                    "hsl($h, $sat%, $l%)"
                }
            }
            resultColor
        }

        val rot = (idHash[28] % 6) * 3
        val colors = scheme.colors.mapIndexed { index, _ -> palette[scheme.colors[if (index < 18) (index + rot) % 18 else 18]] }
        var index = 0

        val mainCircleColor = String.format("#%08X", backgroundColor)

        return mutableListOf(
            Circle(MAIN_RADIUS, MAIN_RADIUS, mainCircleColor, MAIN_RADIUS.toInt()),
            Circle(MAIN_RADIUS, MAIN_RADIUS - r1, colors[index++], RADIUS),
            Circle(MAIN_RADIUS, MAIN_RADIUS - ro2, colors[index++], RADIUS),
            Circle(MAIN_RADIUS - rroot3o4, MAIN_RADIUS - r3o4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS - rroot3o2, MAIN_RADIUS - ro2, colors[index++], RADIUS),
            Circle(MAIN_RADIUS - rroot3o4, MAIN_RADIUS - ro4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS - rroot3o2, MAIN_RADIUS, colors[index++], RADIUS),
            Circle(MAIN_RADIUS - rroot3o2, MAIN_RADIUS + ro2, colors[index++], RADIUS),
            Circle(MAIN_RADIUS - rroot3o4, MAIN_RADIUS + ro4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS - rroot3o4, MAIN_RADIUS + r3o4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS, MAIN_RADIUS + r1, colors[index++], RADIUS),
            Circle(MAIN_RADIUS, MAIN_RADIUS + ro2, colors[index++], RADIUS),
            Circle(MAIN_RADIUS + rroot3o4, MAIN_RADIUS + r3o4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS + rroot3o2, MAIN_RADIUS + ro2, colors[index++], RADIUS),
            Circle(MAIN_RADIUS + rroot3o4, MAIN_RADIUS + ro4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS + rroot3o2, MAIN_RADIUS, colors[index++], RADIUS),
            Circle(MAIN_RADIUS + rroot3o2, MAIN_RADIUS - ro2, colors[index++], RADIUS),
            Circle(MAIN_RADIUS + rroot3o4, MAIN_RADIUS - ro4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS + rroot3o4, MAIN_RADIUS - r3o4, colors[index++], RADIUS),
            Circle(MAIN_RADIUS, MAIN_RADIUS, colors[index], RADIUS)
        )
    }

    private fun findScheme(d: Int): Scheme {
        var cum = 0
        SCHEMES.forEach {
            val n = it.frequency
            cum += n
            if (d < cum) {
                return it
            }
        }
        throw RuntimeException()
    }
}
