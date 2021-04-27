package com.shang.downloadmanagerdemo

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.shang.downloadmanagerdemo.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btDownload.setOnClickListener {
            DownloadAppUtil.downloadApp(
                this,
                "test_020m.zip",
                "http://http.speed.hinet.net/test_020m.zip",  //HiNet的下載測試,要是apk檔案才會自動安裝
                "下載中",
                "全力下載中．．．",
                object : DownloadAppUtil.DownloadListener {
                    override fun downloadStatus(progress: Int, status: Int, statusMsg: String) {
                        runOnUiThread {
                            mBinding.progressBar.progress = progress
                            mBinding.tvStatus.text = "Status:$statusMsg"
                        }
                    }

                    override fun downloadError(exception: Exception) {
                        runOnUiThread {
                            mBinding.tvStatus.text = exception.message
                        }
                    }
                }
            )
        }

        mBinding.btCancel.setOnClickListener {
            DownloadAppUtil.removeDownloadMission(this)
        }
    }


}