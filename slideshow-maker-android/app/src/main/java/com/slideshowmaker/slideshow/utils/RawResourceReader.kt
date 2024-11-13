package com.slideshowmaker.slideshow.utils

import android.content.Context
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import java.io.BufferedReader
import java.io.InputStreamReader

object RawResourceReader {

    fun readTextFileFromRawResource(context: Context, resourceId: Int): String {

        val inStream = context.resources.openRawResource(resourceId)
        val inStreamReader = InputStreamReader(inStream)
        val buffer = BufferedReader(inStreamReader)

        var nextLine = ""
        val currentLine = StringBuilder()
        try {

            while (buffer.readLine().also { nextLine = it } != null) {
                currentLine.append(nextLine)
                currentLine.append("\n")
            }
/*            while (true) {

                nexLine  = bufferedReader.readLine()
                if(nexLine == null) break
                body.append(nexLine)
                body.append("\n")
            }*/
        } catch (e:Exception) {

        }
        return currentLine.toString()
    }

    fun readTextColorFile():ArrayList<String> {
        val textColors = ArrayList<String>()
        val inStream = VideoMakerApplication.getContext().resources.openRawResource(R.raw.text_color_list)
        val inStreamReader = InputStreamReader(inStream)
        val buffer = BufferedReader(inStreamReader)
        var nextLine = ""
        try {

            while (buffer.readLine().also { nextLine = it } != null) {
               textColors.add(nextLine.trim())
            }
/*            while (true) {

                nexLine  = bufferedReader.readLine()
                if(nexLine == null) break
                body.append(nexLine)
                body.append("\n")
            }*/
        } catch (e:Exception) {

        }
        return textColors
    }

}