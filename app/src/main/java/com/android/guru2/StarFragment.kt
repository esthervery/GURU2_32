package com.android.guru2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels // 👈 추가

class StarFragment : Fragment() {
    // 👈 공유 뷰 모델 연결 (HomeFragment와 동일하게 설정)
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_star, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 여기에 캐릭터 이미지를 관찰(observe)하는 코드를 추가하면
        // HomeFragment에서 불러온 캐릭터가 여기에도 똑같이 뜹니다.
    }
}