package com.shang.downloadmanagerdemo

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

/**
 * 主要負責下載完成後自動安裝更新
 * 記得要去AndroidManifest宣告receiver,不需要而外註冊就可以接收了
 * FileProvider也要去AndroidManifest需告,authorities不能與其他的apk重複
 * 重複的話會無法安裝apk
 **/
class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action && isDownloadComplete(context)) {
            //取得剛下載完的ID的,樣子,可以拿來比對是不是跟剛剛下載的是同一個ID
            try {
                val completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (completeDownloadId == DownloadAppUtil.id) {
                    val i = Intent(Intent.ACTION_VIEW)
                    val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //7.0以上要改用FileProvider麻煩死了
                        FileProvider.getUriForFile(
                            context,
                            context.resources.getString(R.string.fileProvider_authorities),
                            File(DownloadAppUtil.downloadApkPath)
                        )
                    } else {
                        //Android7.0以下需要這樣他才找得到apk的位置
                        Uri.fromFile(File(DownloadAppUtil.downloadApkPath))
                    }
                    i.setDataAndType(contentUri, "application/vnd.android.package-archive")
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(i)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //判斷是否下載完成,如果中途被取消會丟出例外直接回傳false
    private fun isDownloadComplete(context: Context): Boolean {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(DownloadAppUtil.id)
        var isComplete = false
        downloadManager.query(query)?.let {
            try {
                it.moveToFirst()
                val downloadByte =
                    it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val downloadTotal =
                    it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                isComplete =
                    (downloadByte == downloadTotal) && (status == DownloadManager.STATUS_SUCCESSFUL)
            }catch (e:Exception){
                e.printStackTrace()
                return false
            }finally {
                it.close()
            }
        }
        return isComplete
    }
}