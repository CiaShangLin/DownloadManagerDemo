# DownloadManagerDemo

** 下載管理器範例，當要簡單的下載APK的時狀態監聽和下載完成後自動打開更新。 **

比較麻煩的點在於要稍微瞭解Uri這東西跟DownloadManager的配合，
尤其是Andoird7.0以上強迫改用FileProvider真的煩，需要在AndroidManifest.xml上進行一些配置，
而Android6.0以下可以直接去訪問檔案，只需要去AndroidManifest.xml宣告android.permission.WRITE_EXTERNAL_STORAGE，
因為是6.0以下所以只需要宣告也不需要特別去要求權限就能夠使用了。

這裡要注意DownloadManager的Request預設的是setDestinationInExternalFilesDir()相當等價於getExternalFilesDir(null)，
這個位置是每個app自己的內部資料夾，如果app被刪除的話下載在裡面的資料也都會被一起刪除。
如果是用setDestinationInExternalPublicDir()代表他是外部的資料夾，就算app刪除下載的檔案還是會存留，要訪問的話可能也需要權限。

    /**
	 * Context = 上下文
	 * ApkName = 下載檔案名稱命名
	 * ApkUrl = 下載檔案的網址
	 * Title = 通知欄的標題
	 * Description = 通知欄的內容
	 * DownloadListener = 下載狀態監聽
	 */
    DownloadAppUtil.downloadApp(Context,ApkName,ApkUrl,Title,Description,DownloadListener)



