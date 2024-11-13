package com.artgen.app.ui.screen.cropphoto

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artgen.app.R
import com.artgen.app.data.model.AspectRatio
import com.artgen.app.data.model.CropAspectRatio

@Composable
fun SelectCropRatio(
    modifier: Modifier = Modifier,
    onAspectRatioChange: (CropAspectRatio) -> Unit
) {
    val selectedCropIndex = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        onAspectRatioChange(ORIGINAL_ASPECT_RATIO)
    }

    LazyRow(modifier = modifier.padding(vertical = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        itemsIndexed(cropAspectRatios) { index, cropRatio ->
            val isSelected = index == selectedCropIndex.value
            val tintColor = if (isSelected) Color.White else Color(0xFF455187)
            val backgroundColor = if (isSelected) Color(0x33FFFFFF) else Color.Transparent

            Box(
                modifier = Modifier
                    .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .clickable {
                        selectedCropIndex.value = index
                        onAspectRatioChange(cropRatio)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(id = cropRatio.iconRes),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(tintColor) // Apply the tint color to the image
                    )
                    Text(
                        text = cropRatio.title,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight(600),
                            color = tintColor,
                            letterSpacing = 0.4.sp,
                        )
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewSelectCropRatio() {

}

val ORIGINAL_ASPECT_RATIO = CropAspectRatio("Original", iconRes = R.drawable.ic_ratio_original)
val FREE_ASPECT_RATIO = CropAspectRatio("Free", iconRes = R.drawable.ic_ratio_free)

val cropAspectRatios = listOf(
    ORIGINAL_ASPECT_RATIO,
    FREE_ASPECT_RATIO,
    CropAspectRatio("1:1", aspectRatio = AspectRatio(1f), iconRes = R.drawable.ic_ratio_1_1),
    CropAspectRatio("2:3", aspectRatio = AspectRatio(2f / 3f), iconRes = R.drawable.ic_ratio_2_3),
    CropAspectRatio("3:4", aspectRatio = AspectRatio(3f / 4f), iconRes = R.drawable.ic_ratio_3_4),
    CropAspectRatio("16:9", aspectRatio = AspectRatio(16f / 9f), iconRes = R.drawable.ic_ratio_16_9)
)