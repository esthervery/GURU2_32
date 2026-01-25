package com.android.guru2.ui.community

import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.guru2.R
import com.android.guru2.data.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

private val MainOrange = Color(0xFFF0724A)
private val TextPrimary = Color(0xFF1C0E09)
private val TextSecondary = Color(0xFF999999)
private val BackgroundWhite = Color(0xFFFFFCFB)
private val InputBackground = Color(0xFFF5F5F5)

@Serializable
data class CommunityPost(
    val id: String,
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("user_name")
    val userName: String = "",
    @SerialName("user_age")
    val userAge: String = "",
    val hashtag: String = "",
    @SerialName("profile_image_url")
    val profileImageUrl: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    val content: String = "",
    @SerialName("like_count")
    var likeCount: Int = 0,
    val date: String = "",
    val category: String = "",
    @SerialName("created_at")
    val createdAt: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBackToCalendar: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(1) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val allPosts = remember { mutableStateListOf<CommunityPost>() }
    val scope = rememberCoroutineScope()

    // 데이터 로드 함수
    fun loadPosts() {
        scope.launch {
            isLoading = true
            try {
                Log.d("CommunityScreen", "=== 데이터 로드 시작 ===")

                val dbPosts = SupabaseClientProvider.client
                    .from("community_posts")
                    .select(columns = Columns.ALL)
                    .decodeList<CommunityPost>()

                Log.d("CommunityScreen", "DB에서 로드된 게시글: ${dbPosts.size}개")

                allPosts.clear()

                // DB 데이터 추가
                allPosts.addAll(dbPosts)

                // 샘플 데이터도 추가
                val sampleHeart = getSamplePosts("walk_with_heart")
                val sampleTogether = getSamplePosts("walk_together")

                // 중복 제거 (ID 기준)
                val existingIds = allPosts.map { it.id }.toSet()
                sampleHeart.forEach { sample ->
                    if (!existingIds.contains(sample.id)) {
                        allPosts.add(sample)
                    }
                }
                sampleTogether.forEach { sample ->
                    if (!existingIds.contains(sample.id)) {
                        allPosts.add(sample)
                    }
                }

                // 날짜순 정렬
                val sorted = allPosts.sortedByDescending { it.date }
                allPosts.clear()
                allPosts.addAll(sorted)

                Log.d("CommunityScreen", "최종 게시글 수: ${allPosts.size}개")

            } catch (e: Exception) {
                Log.e("CommunityScreen", "데이터 로드 실패", e)

                // 에러 시에도 샘플 데이터는 보여주기
                allPosts.clear()
                allPosts.addAll(getSamplePosts("walk_with_heart"))
                allPosts.addAll(getSamplePosts("walk_together"))
            } finally {
                isLoading = false
            }
        }
    }

    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        loadPosts()
    }

    val posts = remember(selectedTab, allPosts.size) {
        val filtered = allPosts.filter {
            it.category == if (selectedTab == 1) "walk_with_heart" else "walk_together"
        }.sortedByDescending { it.date }

        Log.d("CommunityScreen", "탭${selectedTab} 필터링 결과: ${filtered.size}개")
        filtered
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
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToCalendar) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showWriteDialog = true }) {
                        Text(
                            text = "글쓰기",
                            style = MaterialTheme.typography.labelLarge,
                            color = MainOrange
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
            CommunityTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            SearchBar()

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "로딩 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MainOrange
                        )
                    }
                }
                posts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "아직 게시글이 없습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Button(
                                onClick = { loadPosts() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MainOrange
                                )
                            ) {
                                Text("새로고침", color = Color.White)
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostItem(post = post)
                        }
                    }
                }
            }
        }
    }

    if (showWriteDialog) {
        WritePostDialog(
            currentCategory = if (selectedTab == 1) "walk_with_heart" else "walk_together",
            onDismiss = { showWriteDialog = false },
            onSubmit = { title, content, hashtag, imageUri ->
                scope.launch {
                    try {
                        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

                        val newPost = CommunityPost(
                            id = UUID.randomUUID().toString(),
                            userId = "anonymous",
                            userName = "새 사용자",
                            userAge = "13살",
                            hashtag = hashtag.ifEmpty { "#새글" },
                            profileImageUrl = "https://via.placeholder.com/40",
                            imageUrl = imageUri?.toString() ?: "https://via.placeholder.com/328x160",
                            content = title,
                            likeCount = 0,
                            date = dateFormat.format(Date()),
                            category = if (selectedTab == 1) "walk_with_heart" else "walk_together"
                        )

                        Log.d("CommunityScreen", "=== 저장 시도 ===")
                        Log.d("CommunityScreen", "게시글: $newPost")

                        // Supabase에 저장
                        SupabaseClientProvider.client
                            .from("community_posts")
                            .insert(newPost)

                        Log.d("CommunityScreen", "저장 성공!")

                        // 로컬에 즉시 추가
                        allPosts.add(0, newPost)

                        showWriteDialog = false

                        // 저장 후 다시 로드
                        loadPosts()

                    } catch (e: Exception) {
                        Log.e("CommunityScreen", "저장 실패", e)

                        // 저장 실패해도 로컬에는 추가
                        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                        val newPost = CommunityPost(
                            id = UUID.randomUUID().toString(),
                            userId = "anonymous",
                            userName = "새 사용자",
                            userAge = "13살",
                            hashtag = hashtag.ifEmpty { "#새글" },
                            profileImageUrl = "https://via.placeholder.com/40",
                            imageUrl = imageUri?.toString() ?: "https://via.placeholder.com/328x160",
                            content = title,
                            likeCount = 0,
                            date = dateFormat.format(Date()),
                            category = if (selectedTab == 1) "walk_with_heart" else "walk_together"
                        )
                        allPosts.add(0, newPost)
                        showWriteDialog = false
                    }
                }
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
        style = MaterialTheme.typography.titleMedium,
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
                style = MaterialTheme.typography.bodySmall,
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
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun PostItem(post: CommunityPost) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(post.likeCount) }
    val scope = rememberCoroutineScope()

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Row {
                        Text(
                            text = post.userAge,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.hashtag,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "게시글 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InputBackground),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                            scope.launch {
                                try {
                                    isLiked = !isLiked
                                    likeCount = if (isLiked) likeCount + 1 else likeCount - 1

                                    SupabaseClientProvider.client
                                        .from("community_posts")
                                        .update({
                                            set("like_count", likeCount)
                                        }) {
                                            filter { eq("id", post.id) }
                                        }
                                } catch (e: Exception) {
                                    Log.e("PostItem", "좋아요 업데이트 실패", e)
                                }
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "좋아요",
                            tint = if (isLiked) MainOrange else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = likeCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = post.date,
                    style = MaterialTheme.typography.bodySmall,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "글쓰기",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = {
                        if (hasContent) {
                            showCancelDialog = true
                        } else {
                            onDismiss()
                        }
                    }) {
                        Text("✕", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Text("제목", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("제목을 입력해 주세요.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("내용", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("내용을 입력해 주세요.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("해시태그 입력(최대 1개)", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hashtag,
                        onValueChange = { hashtag = it },
                        placeholder = { Text("#참만보강아지") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 사진 선택 부분
                    Text("사진 등록", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedImageUri != null) {
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

                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "이미지 삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
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
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "이미지 추가",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "사진을 추가해 주세요",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }

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
                    Text(
                        "등록하기",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }

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

// 샘플 데이터 함수 (assets 이미지 사용)
private fun getSamplePosts(category: String): List<CommunityPost> {
    return if (category == "walk_with_heart") {
        listOf(
            CommunityPost(
                id = "sample_heart_1",
                userId = "sample",
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
                id = "sample_heart_2",
                userId = "sample",
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
                id = "sample_heart_3",
                userId = "sample",
                userName = "하늘이",
                userAge = "14살",
                hashtag = "#잊지않을게",
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
                id = "sample_together_1",
                userId = "sample",
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
                id = "sample_together_2",
                userId = "sample",
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
                id = "sample_together_3",
                userId = "sample",
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