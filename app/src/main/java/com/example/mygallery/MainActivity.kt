package com.example.mygallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private val REQUEST_READ_EXTELNAL_STORAGE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //권한 부여 확인
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            //권한 허용 X
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                //이전에 이미 권한이 거부되었을 때 설명
                alert("사진 정보를 얻으려면 외부 저장소 권한이 필수로 필요합니다.", "권한이 필요한 이유"){
                    yesButton {
                        //권한 요청
                        ActivityCompat.requestPermissions(this@MainActivity,
                                                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), //대상 스토리지 갯수
                                                            REQUEST_READ_EXTELNAL_STORAGE) // 스토리지 대상
                    }

                    noButton {  }
                }.show()
            }
            else
            {
                //권한요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTELNAL_STORAGE
                )
            }
        }
        else
        {
            getAllPhotos()
        }


    }// end onCreate

    private fun getAllPhotos(){

        //모든 사진 정보 가져오기
        val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                null, // 가져올 항목 배열
                                 null, // 조건
                             null, // 조건
                                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC") //찍은 날짜 내림차순

        val fragments = ArrayList<Fragment>()

        if (cursor != null){

            while (cursor.moveToNext()){
                //사진 경로 Uri 가져오기
                val uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))

                Log.d("MainActivity",uri)
                //framgments 추가
                fragments.add(PhotoFragment.newInstance(uri))
            }
            cursor.close()
        }

        val adapter = MyPagerAdapter(supportFragmentManager)
        adapter.updateFragments(fragments)
        viewPager.adapter = adapter


        timer(period = 3000){
            runOnUiThread {
                if (viewPager.currentItem < adapter.count - 1){
                    viewPager.currentItem = viewPager.currentItem+1
                }
                else
                {
                    viewPager.currentItem = 0
                }

            }
        }//end timer

    }

    //허가 권한 결과 확인 및 설정
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){

            REQUEST_READ_EXTELNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //권한 허용됨
                    getAllPhotos()
                }
                else
                {
                    //권한거부
                    toast("권한 거부 됨")
                }

                return
            }
        }
    }
}
