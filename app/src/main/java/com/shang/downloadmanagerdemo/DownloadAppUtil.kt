package com.shang.downloadmanagerdemo

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

object DownloadAppUtil {
    private var mTimer: Timer? = null
    var id: Long = -1
    var downloadApkPath = ""

    interface DownloadListener {
        fun downloadStatus(progress: Int, status: Int, statusMsg: String)
        fun downloadError(exception: Exception)
    }

    /**
     * Context = 上下文
     * ApkName = 下載檔案名稱命名
     * ApkUrl = 下載檔案的網址
     * Title = 通知欄的標題
     * Description = 通知欄的內容
     * DownloadListener = 下載狀態監聽
     */
    fun downloadApp(
        context: Context,
        apkName: String,
        apkUrl: String,
        title: String,
        description: String,
        downloadListener: DownloadListener? = null
    ) {
        val weakContext = WeakReference<Context>(context).get()
        downloadApkPath =
            weakContext?.getExternalFilesDir(null)?.absolutePath + File.separator + apkName
        deleteFile(downloadApkPath)

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle(title)
            .setNotificationVisibility(View.VISIBLE)
            .setDescription(description)
            .setMimeType("application/vnd.android.package-archive")
            .setDestinationUri(Uri.parse("file://$downloadApkPath"))

        val downloadManager =
            weakContext?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        downloadManager?.let { downloadManager ->
            id = downloadManager.enqueue(request)
            val query = DownloadManager.Query().setFilterById(id)

            mTimer = Timer()
            var cursor: Cursor? = null
            try {
                mTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        cursor = downloadManager.query(query)
                        cursor?.let {
                            it.moveToFirst()
                            val now =
                                it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val total =
                                it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val progress = ((now / total.toFloat()) * 100).toInt()
                            val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))

                            downloadListener?.downloadStatus(progress, status, getStatusMsg(status))
                            if (progress >= 100 && status == DownloadManager.STATUS_SUCCESSFUL) {
                                cursor?.close()
                                mTimer?.cancel()
                            }
                        }
                    }
                }, 0, 300)
            } catch (e: Exception) {
                e.printStackTrace()
                downloadListener?.downloadError(e)
            }
        }
    }

    //如果Activity離開想要銷毀任務的話可以呼叫這個方法
    fun removeDownloadMission(context: Context) {
        WeakReference<Context>(context).get()?.let { context ->
            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?)?.let {
                mTimer?.cancel()
                mTimer = null
                it.remove(id)
            }
        }
    }

    //刪除已經存在的相同檔案
    private fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getStatusMsg(status: Int): String {
        return when (status) {
            DownloadManager.STATUS_PENDING -> "下載等待啟動"
            DownloadManager.STATUS_RUNNING -> "下載中"
            DownloadManager.STATUS_PAUSED -> "下載暫停"
            DownloadManager.STATUS_SUCCESSFUL -> "下載成功"
            DownloadManager.STATUS_FAILED -> "下載失敗"
            else -> ""
        }
    }
}