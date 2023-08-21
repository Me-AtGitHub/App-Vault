package com.example.calculater.activities

import android.os.Bundle
import com.example.calculater.databinding.ActivityCommonBinding

class CommonActivity : BaseActivity<ActivityCommonBinding>() {
    override fun getLayout(): ActivityCommonBinding {
        return ActivityCommonBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

}