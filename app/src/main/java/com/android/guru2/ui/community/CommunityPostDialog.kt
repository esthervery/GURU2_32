package com.android.guru2.ui.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.guru2.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDialog(
    onDismiss: () -> Unit,
    onPost: (title: String, content: String, hashtag: String?, imageUrls: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var hashtag by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = (selectedImages + uris).take(5)
        }
    }

    val isFormValid = title.isNotBlank() && content.isNotBlank()

    Dialog(
        onDismissRequest = {
            if (title.isNotBlank() || content.isNotBlank()) {
                showCancelDialog = true
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 상단 바
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "글쓰기",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                showCancelDialog = true
                            } else {
                                onDismiss()
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "닫기",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // 제목
                    Text(
                        text = "제목",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("제목을 입력해 주세요.", color = Color.LightGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFE0E0E0),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 내용
                    Text(
                        text = "내용",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = content,
                        onValueChange = {
                            if (it.length <= 200) content = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("내용을 입력해 주세요.", color = Color.LightGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFE0E0E0),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0)
                        ),
                        maxLines = 8
                    )
                    Text(
                        text = "최대 글자수: ${content.length}/200자",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 해시태그
                    Text(
                        text = "해시태그 입력 (최대 1개)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = hashtag,
                        onValueChange = { hashtag = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("#참만보강아지", color = Color.LightGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFE0E0E0),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 이미지 등록
                    Text(
                        text = "이미지 등록",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "사진은 PNG, JPG 파일만 등록 가능합니다.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 이미지 선택 영역
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 추가 버튼
                        if (selectedImages.size < 5) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { imagePickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_image),
                                            contentDescription = "이미지 추가",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "최대 5장",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // 선택된 이미지들
                        items(selectedImages) { uri ->
                            Box(
                                modifier = Modifier.size(100.dp)
                            ) {
                                // 이미지 플레이스홀더 (Coil 없이)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0E0E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_image),
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { selectedImages = selectedImages - uri },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "삭제",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 등록하기 버튼
                    Button(
                        onClick = {
                            // TODO: 이미지 업로드 후 URL 리스트 생성
                            val imageUrls = selectedImages.map { it.toString() }
                            onPost(
                                title,
                                content,
                                hashtag.ifBlank { null },
                                imageUrls
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) Color(0xFFF0724A) else Color(0xFFCCCCCC)
                        ),
                        enabled = isFormValid,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "등록하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // 취소 확인 다이얼로그
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("작성을 취소하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    onDismiss()
                }) {
                    Text("확인", color = Color(0xFFF0724A))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("취소", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}