package com.adnan.remote.remoterepo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adnan.remote.remoterepo.ui.theme.RemoteRepoTheme

val ChatGreen = Color(0xFF1ED760)

@Composable
fun ChatBubbleIcon(
    modifier: Modifier = Modifier,
    size: Int = 83,
    tint: Color = ChatGreen
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier.size(
            width = with(density) { size.toDp() },
            height = with(density) { (size * 0.88f).toInt().toDp() }
        )
    ) {
        drawChatBubble(tint)
    }
}

private fun DrawScope.drawChatBubble(color: Color) {
    val scaleX = size.width / 83f
    val scaleY = size.height / 73f

    val path = Path().apply {
        // Scale and transform the original SVG path coordinates
        // Main outer bubble with tail
        moveTo(36.6567f * scaleX, 0.0168709f * scaleY)
        cubicTo(46.4666f * scaleX, 0.332381f * scaleY, 55.2368f * scaleX, 3.78957f * scaleY, 61.5089f * scaleX, 9.13982f * scaleY)
        cubicTo(67.916f * scaleX, 14.5975f * scaleY, 71.7509f * scaleX, 22.0287f * scaleY, 71.5146f * scaleX, 30.1313f * scaleY)
        cubicTo(71.2648f * scaleX, 38.2809f * scaleY, 66.9776f * scaleX, 45.4705f * scaleY, 60.2464f * scaleX, 50.5522f * scaleY)
        cubicTo(53.6434f * scaleX, 55.5333f * scaleY, 44.6707f * scaleX, 58.4668f * scaleY, 34.8878f * scaleX, 58.1849f * scaleY)
        cubicTo(32.2401f * scaleX, 58.1103f * scaleY, 29.6049f * scaleX, 57.7934f * scaleY, 27.0156f * scaleX, 57.2384f * scaleY)
        cubicTo(24.7407f * scaleX, 56.757f * scaleY, 22.5114f * scaleX, 56.0832f * scaleY, 20.3519f * scaleX, 55.2245f * scaleY)
        lineTo(4.52646f * scaleX, 61.4004f * scaleY)
        lineTo(9.81286f * scaleX, 48.8941f * scaleY)
        cubicTo(6.88481f * scaleX, 46.4021f * scaleY, 4.47246f * scaleX, 43.3674f * scaleY, 2.71031f * scaleX, 39.9591f * scaleY)
        cubicTo(0.822068f * scaleX, 36.284f * scaleY, -0.106592f * scaleX, 32.1958f * scaleY, 0.00972538f * scaleX, 28.0704f * scaleY)
        cubicTo(0.266281f * scaleX, 19.9544f * scaleY, 4.55347f * scaleX, 12.7715f * scaleY, 11.2847f * scaleX, 7.66967f * scaleY)
        cubicTo(17.8876f * scaleX, 2.70206f * scaleY, 26.8671f * scaleX, -0.251648f * scaleY, 36.6432f * scaleX, 0.0168709f * scaleY)
        close()

        // Inner bubble (hole)
        moveTo(36.5554f * scaleX, 4.1655f * scaleY)
        cubicTo(27.6908f * scaleX, 3.93054f * scaleY, 19.6835f * scaleX, 6.54189f * scaleY, 13.8097f * scaleX, 10.9725f * scaleY)
        cubicTo(8.05748f * scaleX, 15.3091f * scaleY, 4.39818f * scaleX, 21.391f * scaleY, 4.19564f * scaleX, 28.2114f * scaleY)
        cubicTo(4.1057f * scaleX, 31.6394f * scaleY, 4.88342f * scaleX, 35.035f * scaleY, 6.45738f * scaleX, 38.0862f * scaleY)
        cubicTo(8.19716f * scaleX, 41.4228f * scaleY, 10.6433f * scaleX, 44.3451f * scaleY, 13.6274f * scaleX, 46.652f * scaleY)
        lineTo(14.863f * scaleX, 47.6321f * scaleY)
        lineTo(12.2164f * scaleX, 53.9221f * scaleY)
        lineTo(20.3722f * scaleX, 50.7335f * scaleY)
        lineTo(21.1621f * scaleX, 51.0691f * scaleY)
        cubicTo(23.3207f * scaleX, 51.9776f * scaleY, 25.5609f * scaleX, 52.6811f * scaleY, 27.8528f * scaleX, 53.1703f * scaleY)
        cubicTo(30.2046f * scaleX, 53.6748f * scaleY, 32.5978f * scaleX, 53.9647f * scaleY, 35.0026f * scaleX, 54.0363f * scaleY)
        cubicTo(43.7795f * scaleX, 54.2914f * scaleY, 51.8813f * scaleX, 51.68f * scaleY, 57.7281f * scaleX, 47.2427f * scaleY)
        cubicTo(63.4736f * scaleX, 42.9061f * scaleY, 67.1329f * scaleX, 36.8309f * scaleY, 67.3421f * scaleX, 30.0373f * scaleY)
        cubicTo(67.5379f * scaleX, 23.2505f * scaleY, 64.2635f * scaleX, 16.9806f * scaleY, 58.7948f * scaleX, 12.3151f * scaleY)
        cubicTo(53.2046f * scaleX, 7.5287f * scaleY, 45.3526f * scaleX, 4.45415f * scaleY, 36.5689f * scaleX, 4.18563f * scaleY)
        close()

        // First text line
        moveTo(21.3309f * scaleX, 20.5855f * scaleY)
        lineTo(50.2002f * scaleX, 20.5855f * scaleY)
        cubicTo(51.2391f * scaleX, 20.8485f * scaleY, 52.1809f * scaleX, 21.6236f * scaleY, 52.1809f * scaleX, 22.6698f * scaleY)
        cubicTo(52.1809f * scaleX, 23.7161f * scaleY, 51.2391f * scaleX, 24.4912f * scaleY, 50.2002f * scaleX, 24.7542f * scaleY)
        lineTo(21.3309f * scaleX, 24.7542f * scaleY)
        cubicTo(20.292f * scaleX, 24.4912f * scaleY, 19.3502f * scaleX, 23.7161f * scaleY, 19.3502f * scaleX, 22.6698f * scaleY)
        cubicTo(19.3502f * scaleX, 21.6236f * scaleY, 20.292f * scaleX, 20.8485f * scaleY, 21.3309f * scaleX, 20.5855f * scaleY)
        close()

        // Second text line
        moveTo(21.3309f * scaleX, 32.1452f * scaleY)
        lineTo(41.538f * scaleX, 32.1452f * scaleY)
        cubicTo(42.5769f * scaleX, 32.4082f * scaleY, 43.5187f * scaleX, 33.1834f * scaleY, 43.5187f * scaleX, 34.2296f * scaleY)
        cubicTo(43.5187f * scaleX, 35.2758f * scaleY, 42.5769f * scaleX, 36.051f * scaleY, 41.538f * scaleX, 36.314f * scaleY)
        lineTo(21.3309f * scaleX, 36.314f * scaleY)
        cubicTo(20.292f * scaleX, 36.051f * scaleY, 19.3502f * scaleX, 35.2758f * scaleY, 19.3502f * scaleX, 34.2296f * scaleY)
        cubicTo(19.3502f * scaleX, 33.1834f * scaleY, 20.292f * scaleX, 32.4082f * scaleY, 21.3309f * scaleX, 32.1452f * scaleY)
        close()
    }

    drawPath(
        path = path,
        color = color
    )
}

@Composable
fun ChatBubbleComponent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ChatBubbleIcon(
            size = 83,
            tint = ChatGreen
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatBubblePreview() {
    RemoteRepoTheme {
        ChatBubbleComponent(
            modifier = Modifier.padding(16.dp)
        )
    }
}
