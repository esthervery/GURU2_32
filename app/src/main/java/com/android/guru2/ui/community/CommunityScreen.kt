package com.android.guru2.ui.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.*

// 색상 정의
private val MainOrange = Color(0xFFF0724A)
private val TextPrimary = Color(0xFF1C0E09)
private val TextSecondary = Color(0xFF999999)
private val BackgroundWhite = Color(0xFFFFFCFB)
private val InputBackground = Color(0xFFF5F5F5)

// 데이터 클래스
data class CommunityPost(
    val id: String,
    val userId: String = "",
    val userName: String,
    val userAge: String,
    val hashtag: String,
    val profileImageUrl: String,
    val imageUrl: String,
    val content: String,
    var likeCount: Int,
    val date: String,
    val category: String,
    val createdAt: String? = null,
    var isLiked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBackToCalendar: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(1) } // 0: 나란히, 1: 마음으로
    var showWriteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // 게시글 리스트 상태
    val allPosts = remember { mutableStateListOf<CommunityPost>() }

    // 초기 샘플 데이터 로드
    LaunchedEffect(Unit) {
        isLoading = true
        allPosts.addAll(getSamplePosts("walk_with_heart"))
        allPosts.addAll(getSamplePosts("walk_together"))
        isLoading = false
    }

    // 현재 탭의 게시글만 필터링
    val posts = remember(selectedTab, allPosts.size) {
        allPosts.filter {
            it.category == if (selectedTab == 1) "walk_with_heart" else "walk_together"
        }.sortedByDescending { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "이야기 방",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToCalendar) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showWriteDialog = true }) {
                        Text(
                            text = "글쓰기",
                            color = MainOrange,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 탭
            CommunityTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // 검색창
            SearchBar()

            // 게시글 리스트
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "로딩 중...",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(posts) { post ->
                        PostItem(post = post)
                    }
                }
            }
        }
    }

    // 글쓰기 다이얼로그
    if (showWriteDialog) {
        WritePostDialog(
            currentCategory = if (selectedTab == 1) "walk_with_heart" else "walk_together",
            onDismiss = { showWriteDialog = false },
            onSubmit = { title, content, hashtag, imageUri ->
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val newPost = CommunityPost(
                    id = UUID.randomUUID().toString(),
                    userId = "user123",
                    userName = "새 사용자",
                    userAge = "13살",
                    hashtag = hashtag.ifEmpty { "#새글" },
                    profileImageUrl = "file:///android_asset/여름이_프로필.jpg",
                    imageUrl = imageUri?.toString() ?: "file:///android_asset/여름이_썸네일.jpg",
                    content = title,
                    likeCount = 0,
                    date = dateFormat.format(Date()),
                    category = if (selectedTab == 1) "walk_with_heart" else "walk_together",
                    isLiked = false
                )
                allPosts.add(0, newPost)
                showWriteDialog = false
            }
        )
    }
}

@Composable
fun CommunityTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            TabItem(
                text = "나란히 걷기",
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(0) }
            )
            TabItem(
                text = "마음으로 걷기",
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(1) }
            )
        }

        // 인디케이터
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            val indicatorOffset by animateDpAsState(
                targetValue = if (selectedTab == 0) 0.dp else (LocalContext.current.resources.displayMetrics.widthPixels / 2).dp,
                label = "tab_indicator"
            )
            Box(
                modifier = Modifier
                    .width((LocalContext.current.resources.displayMetrics.widthPixels / 2).dp)
                    .height(2.dp)
                    .offset(x = indicatorOffset)
                    .background(TextPrimary)
            )
        }
    }
}

@Composable
fun TabItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = if (selected) TextPrimary else TextSecondary,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = {
            Text(
                text = "오늘 아이와 관련된 어떤 추억이 떠올랐나요?",
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = InputBackground,
            focusedContainerColor = InputBackground,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
            focusedBorderColor = MainOrange
        ),
        singleLine = true
    )
}

@Composable
fun PostItem(post: CommunityPost) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.likeCount) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 프로필 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 프로필 이미지
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.profileImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(InputBackground),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row {
                        Text(
                            text = post.userAge,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.hashtag,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 게시글 이미지 (328x160dp)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "게시글 이미지",
                modifier = Modifier
                    .width(328.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InputBackground),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 제목
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 좋아요 & 날짜
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "좋아요",
                            tint = if (isLiked) MainOrange else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = likeCount.toString(),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = post.date,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePostDialog(
    currentCategory: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var hashtag by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val hasContent = title.isNotEmpty() || content.isNotEmpty() || hashtag.isNotEmpty() || selectedImageUri != null

    Dialog(
        onDismissRequest = {
            if (hasContent) {
                showCancelDialog = true
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = BackgroundWhite
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 헤더 (고정)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "글쓰기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        if (hasContent) {
                            showCancelDialog = true
                        } else {
                            onDismiss()
                        }
                    }) {
                        Text("✕", fontSize = 20.sp)
                    }
                }

                // 스크롤 가능한 컨텐츠
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // 제목
                    Text("제목", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("제목을 입력해 주세요.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 내용
                    Text("내용", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("내용을 입력해 주세요.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 해시태그
                    Text("해시태그 (최대 1개)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hashtag,
                        onValueChange = { hashtag = it },
                        placeholder = { Text("#참만보강아지") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 이미지 업로드
                    Text("사진 (최대 5장)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedImageUri != null) {
                        // 이미지 미리보기
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "선택된 이미지",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // 삭제 버튼
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "이미지 삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // 이미지 선택 버튼
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = TextSecondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(InputBackground)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "이미지 추가",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "사진을 추가해 주세요",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }

                // 등록 버튼 (고정)
                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            onSubmit(title, content, hashtag, selectedImageUri)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    enabled = title.isNotEmpty() && content.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainOrange,
                        disabledContainerColor = TextSecondary
                    )
                ) {
                    Text("등록하기", color = Color.White, fontSize = 16.sp)
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
                    Text("확인", color = MainOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// 샘플 데이터
private fun getSamplePosts(category: String): List<CommunityPost> {
    return if (category == "walk_with_heart") {
        listOf(
            CommunityPost(
                id = "1",
                userName = "여름이",
                userAge = "13살",
                hashtag = "#애교쟁이",
                profileImageUrl = "file:///android_asset/여름이_프로필.jpg",
                imageUrl = "file:///android_asset/여름이_썸네일.jpg",
                content = "어젯밤 꿈속에 나타난 여름이",
                likeCount = 12,
                date = "2025.01.15",
                category = "walk_with_heart"
            ),
            CommunityPost(
                id = "2",
                userName = "별이",
                userAge = "15살",
                hashtag = "#믿먹지짱먹지",
                profileImageUrl = "file:///android_asset/별이_프로필.jpg",
                imageUrl = "file:///android_asset/별이_썸네일.jpg",
                content = "별이가 떠난 첫날밤, 세상이 멈춰버린 것만 같아요.",
                likeCount = 5,
                date = "2025.01.14",
                category = "walk_with_heart"
            ),
            CommunityPost(
                id = "3",
                userName = "하늘이",
                userAge = "14살",
                hashtag = "#잇지잉슬게",
                profileImageUrl = "file:///android_asset/하늘이_프로필.jpg",
                imageUrl = "file:///android_asset/하늘이_썸네일.jpg",
                content = "하늘이의 마지막 산책사진",
                likeCount = 5,
                date = "2025.01.12",
                category = "walk_with_heart"
            )
        )
    } else {
        listOf(
            CommunityPost(
                id = "4",
                userName = "토리",
                userAge = "10살",
                hashtag = "#느릿느릿토끼",
                profileImageUrl = "file:///android_asset/토리_프로필.jpg",
                imageUrl = "file:///android_asset/토리_썸네일.jpg",
                content = "토리와 오랜만에 산책!!",
                likeCount = 5,
                date = "2025.01.17",
                category = "walk_together"
            ),
            CommunityPost(
                id = "5",
                userName = "초코",
                userAge = "13살",
                hashtag = "#댕댕누우기",
                profileImageUrl = "file:///android_asset/초코_프로필.jpg",
                imageUrl = "file:///android_asset/초코_썸네일.jpg",
                content = "초코를 위한 천연 보양식",
                likeCount = 16,
                date = "2025.01.15",
                category = "walk_together"
            ),
            CommunityPost(
                id = "6",
                userName = "몽글이",
                userAge = "14살",
                hashtag = "#오늘도건강하자",
                profileImageUrl = "file:///android_asset/몽글이_프로필.jpg",
                imageUrl = "file:///android_asset/몽글이_썸네일.jpg",
                content = "14살 몽글이가 밥먹다 서성거려요.",
                likeCount = 5,
                date = "2025.01.11",
                category = "walk_together"
            )
        )
    }
}