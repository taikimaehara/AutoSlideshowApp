package jp.techacademy.taiki.maehara.autoslideshowapp

import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val mumap = mutableMapOf<Int, Uri>()
    private var imageViewIndex = 0

    private var mSlideshowTimer: Timer? = null
    private var mHandler = Handler(Looper.getMainLooper())
    private var mSlideshowStatus = 0 //0:stop 1:play

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()

            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            // Android 6.0未満の場合
            getContentsInfo()

        }

        button_forward.setOnClickListener {
            //進むボタン押された際
            changeImageView(0)
        }

        button_back.setOnClickListener {
            //戻るボタン押された場合
            changeImageView(1)
        }

        button_play_stop.setOnClickListener {
            //再生/停止ボタン押された場合
            when (mSlideshowStatus) {
                0 -> {
                    //停止->再生
                    if (mumap.size == 0) {
                        Toast.makeText(this, "画像がありません", Toast.LENGTH_SHORT).show()
                    } else {
                        mSlideshowStatus = 1
                        button_play_stop.text = "停止"
                        button_forward.isEnabled = false
                        button_back.isEnabled = false
                        if (mSlideshowTimer == null) {
                            // タイマー作成
                            mSlideshowTimer = Timer()
                            // スライドショー開始
                            mSlideshowTimer!!.schedule(object : TimerTask() {
                                override fun run() {
                                    mHandler.post {
                                        changeImageView(0)
                                    }
                                }
                            }, 2000, 2000)
                        }
                    }
                }
                1 -> {
                    //再生->停止
                    mSlideshowStatus = 0
                    button_play_stop.text = "再生"
                    button_forward.isEnabled = true
                    button_back.isEnabled = true
                    if (mSlideshowTimer != null) {
                        mSlideshowTimer!!.cancel()
                        mSlideshowTimer = null
                    }
                }

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permission: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    //画像の切り替え
    //direction(0:進む 1:戻る)
    private fun changeImageView(direction: Int) {
        if (mumap.size == 0) {
            Toast.makeText(this, "画像がありません", Toast.LENGTH_SHORT).show()
        } else {
            when (direction) {
                0 -> {
                    imageViewIndex++
                    if (mumap.size <= imageViewIndex) {
                        imageViewIndex = 0
                    }
                }
                1 -> {
                    imageViewIndex--
                    if (imageViewIndex < 0) {
                        imageViewIndex = mumap.size - 1
                    }
                }
            }
            Log.d("test", imageViewIndex.toString())
            imageView.setImageURI(mumap.get(imageViewIndex))
        }
    }

    //画像コンテンツ情報の取得 及び 初期画像表示
    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        var cnt = 0

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                mumap.put(cnt, imageUri)
                cnt++

                Log.d("test", mumap.toString())

            } while (cursor!!.moveToNext())
        }
        cursor.close()

        if (mumap != null) {
            if (mumap.size == 0) {
                Toast.makeText(this, "画像がありません", Toast.LENGTH_SHORT).show()
            }
            imageView.setImageURI(mumap.get(imageViewIndex))
        }
    }
}