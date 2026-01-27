package com.android.guru2.ui.community

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import com.android.guru2.PetInfo
import com.android.guru2.data.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.auth
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
    val imageUrl: String? = null,
    val title: String = "",  // 제목 추가
    val content: String = "",  // 내용
    @SerialName("like_count")
    var likeCount: Int = 0,
    val date: String = "",
    val category: String = "",
    @SerialName("created_at")
    val createdAt: String? = null
)

// Supabase Storage에 이미지 업로드하는 함수
suspend fun uploadImageToSupabase(context: Context, uri: Uri, postId: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()

        if (bytes == null) {
            Log.e("ImageUpload", "이미지 바이트 읽기 실패")
            return null
        }

        val fileName = "post_${postId}_${System.currentTimeMillis()}.jpg"

        // Supabase Storage의 'community-images' 버킷에 업로드
        val bucket = SupabaseClientProvider.client.storage.from("community-images")

        // upload 함수 수정: path와 data만 전달
        bucket.upload(fileName, bytes) {
            upsert = false
        }

        // 업로드된 이미지의 Public URL 가져오기
        val publicUrl = bucket.publicUrl(fileName)

        Log.d("ImageUpload", "업로드 성공: $publicUrl")
        publicUrl

    } catch (e: Exception) {
        Log.e("ImageUpload", "이미지 업로드 실패", e)
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBackToCalendar: () -> Unit
) {
    // selectedTab: 0 = "나란히 걷기", 1 = "마음으로 걷기"
    var selectedTab by remember { mutableStateOf(1) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showPostDetail by remember { mutableStateOf<CommunityPost?>(null) }

    val allPosts = remember { mutableStateListOf<CommunityPost>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 사용자의 반려견 정보 저장
    var userPetInfo by remember { mutableStateOf<PetInfo?>(null) }

    // 사용자의 반려견 정보 가져오기
    LaunchedEffect(Unit) {
        try {
            val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
            val userId = currentUser?.id

            if (userId != null) {
                val petData = SupabaseClientProvider.client.postgrest["petInfo"]
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeSingleOrNull<PetInfo>()

                userPetInfo = petData
                Log.d("CommunityScreen", "반려견 정보 로드: ${petData?.pet_name}")
            }
        } catch (e: Exception) {
            Log.e("CommunityScreen", "반려견 정보 로드 실패", e)
        }
    }

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
                allPosts.addAll(dbPosts)

                // 샘플 데이터 항상 추가 (중복 제거)
                val sampleHeart = getSamplePosts("walk_with_heart")
                val sampleTogether = getSamplePosts("walk_together")

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

                // 날짜순 정렬 (최신순)
                val sorted = allPosts.sortedByDescending {
                    it.createdAt ?: it.date
                }
                allPosts.clear()
                allPosts.addAll(sorted)

                Log.d("CommunityScreen", "최종 게시글 수: ${allPosts.size}개")

            } catch (e: Exception) {
                Log.e("CommunityScreen", "데이터 로드 실패", e)

                // 에러 시 샘플 데이터 표시
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

    // 탭에 따라 필터링된 게시글
    val posts = remember(selectedTab, allPosts.size) {
        val categoryName = if (selectedTab == 1) "walk_with_heart" else "walk_together"
        val filtered = allPosts.filter { it.category == categoryName }

        Log.d("CommunityScreen", "탭${selectedTab} (${categoryName}) 필터링 결과: ${filtered.size}개")
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
                            style = MaterialTheme.typography.bodyLarge,
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
                            PostItem(
                                post = post,
                                onClick = { showPostDetail = post }
                            )
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
                        val postId = UUID.randomUUID().toString()
                        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                        val currentDate = dateFormat.format(Date())

                        // 이미지를 Supabase Storage에 업로드 (이미지가 있을 때만)
                        var uploadedImageUrl: String? = null

                        if (imageUri != null) {
                            Log.d("CommunityScreen", "이미지 업로드 시작...")
                            uploadedImageUrl = uploadImageToSupabase(context, imageUri, postId)
                            if (uploadedImageUrl != null) {
                                Log.d("CommunityScreen", "이미지 업로드 성공: $uploadedImageUrl")
                            } else {
                                Log.e("CommunityScreen", "이미지 업로드 실패")
                            }
                        }

                        Log.d("CommunityScreen", "=== 저장할 데이터 확인 ===")
                        Log.d("CommunityScreen", "title: $title")
                        Log.d("CommunityScreen", "content: $content")
                        Log.d("CommunityScreen", "hashtag: $hashtag")

                        val newPost = CommunityPost(
                            id = postId,
                            userId = "anonymous",
                            userName = userPetInfo?.pet_name ?: "새 사용자",  // 반려견 이름 사용
                            userAge = "${userPetInfo?.pet_age ?: 0}살",  // 반려견 나이 사용
                            hashtag = if (hashtag.startsWith("#")) hashtag else "#$hashtag",
                            profileImageUrl = userPetInfo?.pet_image ?: "https://via.placeholder.com/40",  // 반려견 사진 사용
                            imageUrl = uploadedImageUrl,  // null이면 null로 저장
                            title = title,  // 제목
                            content = content,  // 내용
                            likeCount = 0,
                            date = currentDate,
                            category = if (selectedTab == 1) "walk_with_heart" else "walk_together",
                            createdAt = null
                        )

                        Log.d("CommunityScreen", "=== DB 저장 시도 ===")
                        Log.d("CommunityScreen", "게시글: $newPost")

                        // Supabase DB에 저장
                        SupabaseClientProvider.client
                            .from("community_posts")
                            .insert(newPost)

                        Log.d("CommunityScreen", "DB 저장 성공!")

                        // 토스트 메시지 표시
                        Toast.makeText(context, "등록 완료!", Toast.LENGTH_SHORT).show()

                        showWriteDialog = false

                        // 저장 후 다시 로드
                        loadPosts()

                    } catch (e: Exception) {
                        Log.e("CommunityScreen", "저장 실패", e)
                        showWriteDialog = false
                    }
                }
            }
        )
    }

    // 게시글 상세보기 다이얼로그
    showPostDetail?.let { post ->
        AlertDialog(
            onDismissRequest = { showPostDetail = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
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
                    Column {
                        Text(
                            text = post.userName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${post.userAge} ${post.hashtag}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // 이미지가 있으면 표시
                    if (!post.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(post.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "게시글 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(InputBackground),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // 제목 표시
                    if (post.title.isNotEmpty()) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    // 내용 표시
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "좋아요 ${post.likeCount}개",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = post.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPostDetail = null }) {
                    Text("닫기", color = MainOrange)
                }
            },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(16.dp)
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
            val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
            val indicatorOffset by animateDpAsState(
                targetValue = if (selectedTab == 0) 0.dp else (screenWidth / 2 / LocalContext.current.resources.displayMetrics.density).dp,
                label = "tab_indicator"
            )
            Box(
                modifier = Modifier
                    .width((screenWidth / 2 / LocalContext.current.resources.displayMetrics.density).dp)
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
fun PostItem(
    post: CommunityPost,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("community_likes", Context.MODE_PRIVATE)
    }

    // 저장된 좋아요 상태 불러오기
    var isLiked by remember {
        mutableStateOf(sharedPrefs.getBoolean("like_${post.id}", false))
    }
    var likeCount by remember { mutableStateOf(post.likeCount) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

            // 이미지 URL이 null이 아니고 비어있지 않으면 표시
            if (!post.imageUrl.isNullOrEmpty()) {

                Spacer(modifier = Modifier.height(12.dp))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .build(),
                    contentDescription = "게시글 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBackground),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 제목 표시
            if (post.title.isNotEmpty()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 내용 표시
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

                                    // SharedPreferences에 좋아요 상태 저장
                                    sharedPrefs.edit().putBoolean("like_${post.id}", isLiked).apply()

                                    SupabaseClientProvider.client
                                        .from("community_posts")
                                        .update({
                                            set("like_count", likeCount)
                                        }) {
                                            filter {
                                                eq("id", post.id)
                                            }
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
                        placeholder = { Text("#잠만보강아지") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

// 샘플 데이터
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
                title = "꿈속의 여름이",
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
                title = "별이의 첫날밤",
                content = "별이가 떠난 첫날밤, 세상이 멈춰버린 것만 같아요.",
                likeCount = 5,
                date = "2025.01.14",
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
                title = "토리와 산책",
                content = "토리와 오랜만에 산책!!",
                likeCount = 5,
                date = "2025.01.17",
                category = "walk_together"
            )
        )
    }
}