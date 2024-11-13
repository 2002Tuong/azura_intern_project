package com.screentheme.app.utils.extensions

import android.os.Bundle
import androidx.navigation.NavDirections

class FragmentDirections private constructor() {
    companion object {
        fun action(bundle: Bundle, actionId: Int): NavDirections {
            return object : NavDirections {

                override val actionId: Int
                    get() = actionId
                override val arguments: Bundle
                    get() = bundle
            }
        }
    }
}
