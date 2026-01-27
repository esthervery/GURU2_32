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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.guru2.PetInfo
import com.android.guru2.R
import com.android.guru2.data.SupabaseClientProvider
import com.android.guru2.data.model.CommunityPost
import com.android.guru2.data.repository.CommunityRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val MainOrange = Color(0xFFF0724A)
private val TextPrimary = Color(0xFF1C0E09)
private val TextSecondary = Color(0xFF999999)
private val BackgroundWhite = Color(0xFFFFFCFB)
private val InputBackground = Color(0xFFF5F5F5)

// 이미지 업로드(실패해도 글 등록은 가능)
private suspend fun uploadImageToSupabase(context: Context, uri: Uri, postId: String): String? {
    return try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) return null

        val fileName = "post_${postId}_${System.currentTimeMillis()}.jpg"
        val bucket = SupabaseClientProvider.client.storage.from("community-images")
        bucket.upload(fileName, bytes) { upsert = false }
        bucket.publicUrl(fileName)
    } catch (e: Exception) {
        Log.e("ImageUpload", "upload fail", e)
        null
    }
}

// ✅ 모든 좋아요 상태를 SharedPreferences에 저장 (DB + 샘플 통합)
private fun saveLikeStates(
    context: Context,
    userId: String,
    dbLikedPosts: Set<String>,
    sampleLikedPosts: Set<String>,
    likeCounts: Map<String, Int>
) {
    val prefs = context.getSharedPreferences("community_likes_$userId", Context.MODE_PRIVATE)
    prefs.edit().apply {
        // DB 게시글 좋아요 상태
        putStringSet("db_liked_posts", dbLikedPosts)
        // 샘플 데이터 좋아요 상태
        putStringSet("sample_liked_posts", sampleLikedPosts)
        // 좋아요 개수
        likeCounts.forEach { (postId, count) ->
            putInt("count_$postId", count)
        }
        apply()
    }
}

//  저장된 좋아요 상태 로드
private fun loadLikeStates(
    context: Context,
    userId: String
): Triple<Set<String>, Set<String>, Map<String, Int>> {
    val prefs = context.getSharedPreferences("community_likes_$userId", Context.MODE_PRIVATE)

    val dbLikedPosts = prefs.getStringSet("db_liked_posts", emptySet()) ?: emptySet()
    val sampleLikedPosts = prefs.getStringSet("sample_liked_posts", emptySet()) ?: emptySet()

    val likeCounts = mutableMapOf<String, Int>()

    // ✅ prefs에 저장된 count_로 시작하는 모든 키를 읽어서 복원
    prefs.all.forEach { (key, value) ->
        if (key.startsWith("count_") && value is Int) {
            val postId = key.removePrefix("count_")
            likeCounts[postId] = value
        }
    }

    // ✅ 샘플 게시글은 기본값 보강 (prefs에 없으면 기본 likeCount)
    getSamplePosts().forEach { post ->
        likeCounts.putIfAbsent(post.id, post.likeCount)
    }

    return Triple(dbLikedPosts, sampleLikedPosts, likeCounts)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBackToCalendar: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(1) } // 0=나란히, 1=마음으로
    var showWriteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showPostDetail by remember { mutableStateOf<CommunityPost?>(null) }

    val allPosts = remember { mutableStateListOf<CommunityPost>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val repository = remember { CommunityRepository() }

    var userPetInfo by remember { mutableStateOf<PetInfo?>(null) }
    var currentUserId by remember { mutableStateOf<String?>(null) }

    // DB 게시글 좋아요 상태 (서버 + 로컬 저장)
    val userLikedPosts = remember { mutableStateSetOf<String>() }

    // 샘플 데이터 좋아요 상태 (로컬 저장)
    val sampleLikedPosts = remember { mutableStateSetOf<String>() }

    // 모든 게시글의 좋아요 개수를 로컬에서 관리
    val localLikeCounts = remember { mutableStateMapOf<String, Int>() }

    // UI 갱신을 위한 트리거
    var refreshTrigger by remember { mutableStateOf(0) }

    // ✅ 사용자 정보 및 저장된 좋아요 상태 로드
    LaunchedEffect(Unit) {
        try {
            val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
            currentUserId = userId

            if (userId != null) {
                // 반려견 정보 로드
                val petData = SupabaseClientProvider.client.postgrest["petInfo"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<PetInfo>()
                userPetInfo = petData

                // ✅ 로컬에 저장된 좋아요 상태 먼저 로드 (즉시 UI 반영)
                val (savedDbLikes, savedSampleLikes, savedCounts) = loadLikeStates(context, userId)
                userLikedPosts.addAll(savedDbLikes)
                sampleLikedPosts.addAll(savedSampleLikes)
                localLikeCounts.putAll(savedCounts)

                // 서버에서 최신 DB 좋아요 목록 가져오기 (백그라운드 동기화)
                try {
                    val likes = SupabaseClientProvider.client.postgrest["post_likes"]
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<Map<String, String>>()

                    val serverLikedPosts = likes.mapNotNull { it["post_id"] }.toSet()

                    // 서버 데이터로 업데이트
                    userLikedPosts.clear()
                    userLikedPosts.addAll(serverLikedPosts)

                    // ✅ 서버 데이터를 로컬에 저장
                    saveLikeStates(context, userId, userLikedPosts, sampleLikedPosts, localLikeCounts.toMap())
                } catch (e: Exception) {
                    Log.e("CommunityScreen", "서버 좋아요 목록 로드 실패", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CommunityScreen", "초기화 실패", e)
        }
    }

    // 게시글 로드: DB(위) + 샘플(아래)
    fun loadPosts() {
        scope.launch {
            isLoading = true
            try {
                val dbPosts = SupabaseClientProvider.client
                    .from("community_posts")
                    .select(columns = Columns.ALL)
                    .decodeList<CommunityPost>()
                    .map { p -> if (p.likeCount < 0) p.copy(likeCount = 0) else p }
                    .sortedByDescending { it.createdAt ?: it.date }

                val samplePosts = getSamplePosts()

                // DB 게시글의 좋아요 개수 저장
                dbPosts.forEach { post ->
                    if (!localLikeCounts.containsKey(post.id)) {
                        localLikeCounts[post.id] = post.likeCount
                    }
                }

                // 샘플 데이터 중 아직 로드되지 않은 것만 추가
                samplePosts.forEach { sample ->
                    if (!localLikeCounts.containsKey(sample.id)) {
                        localLikeCounts[sample.id] = sample.likeCount
                    }
                }

                val dbIds = dbPosts.map { it.id }.toSet()
                val safeSamples = samplePosts.filter { it.id !in dbIds }
                val merged = dbPosts + safeSamples

                allPosts.clear()
                allPosts.addAll(merged)
            } catch (e: Exception) {
                Log.e("CommunityScreen", "load fail", e)
                Toast.makeText(context, "게시글 로드 실패", Toast.LENGTH_SHORT).show()

                allPosts.clear()
                val samplePosts = getSamplePosts()
                samplePosts.forEach { sample ->
                    if (!localLikeCounts.containsKey(sample.id)) {
                        localLikeCounts[sample.id] = sample.likeCount
                    }
                }
                allPosts.addAll(samplePosts)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadPosts() }

    // 모든 게시글에 로컬 좋아요 개수 적용
    val posts = remember(selectedTab, allPosts.size, localLikeCounts.size, refreshTrigger) {
        val categoryName = if (selectedTab == 1) "walk_with_heart" else "walk_together"
        val sampleIds = getSamplePosts().map { it.id }.toSet()

        val filtered = allPosts.filter { it.category == categoryName }
        val dbPart = filtered.filter { it.id !in sampleIds }.map { post ->
            post.copy(likeCount = localLikeCounts[post.id] ?: post.likeCount)
        }
        val samplePart = filtered.filter { it.id in sampleIds }.map { post ->
            post.copy(likeCount = localLikeCounts[post.id] ?: post.likeCount)
        }

        dbPart + samplePart
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("이야기 방", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToCalendar) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "back", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { showWriteDialog = true }) {
                        Text("글쓰기", style = MaterialTheme.typography.labelLarge, color = MainOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CommunityTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            SearchBar()

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("로딩 중...", color = MainOrange)
                    }
                }

                posts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("아직 게시글이 없습니다", color = TextSecondary)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { loadPosts() },
                                colors = ButtonDefaults.buttonColors(containerColor = MainOrange)
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
                            val isSample = post.id.startsWith("sample_")
                            PostItem(
                                post = post,
                                isLiked = if (isSample) sampleLikedPosts.contains(post.id) else userLikedPosts.contains(post.id),
                                onClick = { showPostDetail = post },
                                onToggleLike = { id ->
                                    //  샘플 데이터 좋아요 처리
                                    if (id.startsWith("sample_")) {
                                        if (sampleLikedPosts.contains(id)) {
                                            sampleLikedPosts.remove(id)
                                            localLikeCounts[id] = maxOf(0, (localLikeCounts[id] ?: 0) - 1)  // ✅ 최소값 0
                                        } else {
                                            sampleLikedPosts.add(id)
                                            localLikeCounts[id] = (localLikeCounts[id] ?: 0) + 1
                                        }
                                        // 로컬에 저장
                                        currentUserId?.let { userId ->
                                            saveLikeStates(context, userId, userLikedPosts, sampleLikedPosts, localLikeCounts.toMap())
                                        }
                                        refreshTrigger++
                                        return@PostItem
                                    }

                                        //  DB 게시글 좋아요 처리
                                    scope.launch {
                                        val userId = currentUserId
                                        if (userId == null) {
                                            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }

                                        // 로컬 상태 즉시 업데이트
                                        val wasLiked = userLikedPosts.contains(id)
                                        if (wasLiked) {
                                            userLikedPosts.remove(id)
                                            localLikeCounts[id] = maxOf(0, (localLikeCounts[id] ?: 0) - 1)  // ✅ 최소값 0
                                        } else {
                                            userLikedPosts.add(id)
                                            localLikeCounts[id] = (localLikeCounts[id] ?: 0) + 1
                                        }

                                        // 로컬에 즉시 저장
                                        saveLikeStates(context, userId, userLikedPosts, sampleLikedPosts, localLikeCounts.toMap())
                                        refreshTrigger++

                                        // 서버에 반영
                                        val ok = repository.toggleLike(id, userId)
                                        if (!ok) {
                                            // 실패 시 롤백
                                            if (wasLiked) {
                                                userLikedPosts.add(id)
                                                localLikeCounts[id] = (localLikeCounts[id] ?: 0) + 1
                                            } else {
                                                userLikedPosts.remove(id)
                                                localLikeCounts[id] = maxOf(0, (localLikeCounts[id] ?: 0) - 1)  // ✅ 최소값 0
                                            }
                                            saveLikeStates(context, userId, userLikedPosts, sampleLikedPosts, localLikeCounts.toMap())
                                            refreshTrigger++
                                            Toast.makeText(context, "좋아요 반영 실패", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // 성공하면 서버에서 최신 데이터 가져오기 (백그라운드)
                                            loadPosts()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 글쓰기
    if (showWriteDialog) {
        WritePostDialog(
            currentCategory = if (selectedTab == 1) "walk_with_heart" else "walk_together",
            onDismiss = { showWriteDialog = false },
            onSubmit = { title, content, hashtag, imageUri ->
                scope.launch {
                    try {
                        val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                        if (userId == null) {
                            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                            showWriteDialog = false
                            return@launch
                        }

                        val postId = UUID.randomUUID().toString()
                        val date = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
                        val uploadedUrl =
                            if (imageUri != null) uploadImageToSupabase(context, imageUri, postId) else null

                        val tag = hashtag.trim().let {
                            if (it.isEmpty()) "" else if (it.startsWith("#")) it else "#$it"
                        }

                        val newPost = CommunityPost(
                            id = postId,
                            userId = userId,
                            userName = userPetInfo?.pet_name ?: "사용자",
                            userAge = "${userPetInfo?.pet_age ?: 0}살",
                            hashtag = tag,
                            profileImageUrl = userPetInfo?.pet_image ?: "https://via.placeholder.com/40",
                            imageUrl = uploadedUrl,
                            title = title,
                            content = content,
                            likeCount = 0,
                            date = date,
                            category = if (selectedTab == 1) "walk_with_heart" else "walk_together",
                            createdAt = null
                        )

                        SupabaseClientProvider.client.from("community_posts").insert(newPost)

                        Toast.makeText(context, "등록 완료!", Toast.LENGTH_SHORT).show()
                        showWriteDialog = false
                        loadPosts()
                    } catch (e: Exception) {
                        Log.e("CommunityScreen", "insert fail", e)
                        Toast.makeText(context, "등록 실패", Toast.LENGTH_SHORT).show()
                        showWriteDialog = false
                    }
                }
            }
        )
    }

    // 상세보기
    showPostDetail?.let { post ->
        val displayPost = if (localLikeCounts.containsKey(post.id)) {
            post.copy(likeCount = localLikeCounts[post.id] ?: post.likeCount)
        } else {
            post
        }

        AlertDialog(
            onDismissRequest = { showPostDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(displayPost.profileImageUrl).crossfade(true).build(),
                        contentDescription = "profile",
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(InputBackground),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(displayPost.userName)
                        Text("${displayPost.userAge} ${displayPost.hashtag}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!displayPost.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(displayPost.imageUrl).crossfade(true).build(),
                            contentDescription = "post image",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(InputBackground),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (displayPost.title.isNotEmpty()) {
                        Text(displayPost.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }

                    Text(displayPost.content, color = TextPrimary)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(displayPost.date, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPostDetail = null }) { Text("닫기", color = MainOrange) }
            },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CommunityTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            TabItem("나란히 걷기", selectedTab == 0, Modifier.weight(1f)) { onTabSelected(0) }
            TabItem("마음으로 걷기", selectedTab == 1, Modifier.weight(1f)) { onTabSelected(1) }
        }

        val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
        val tabWidthDp = (screenWidth / 2f / LocalContext.current.resources.displayMetrics.density).dp
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedTab == 0) 0.dp else tabWidthDp,
            label = "tab_indicator"
        )

        Box(Modifier.fillMaxWidth().height(2.dp)) {
            Box(
                modifier = Modifier
                    .width(tabWidthDp)
                    .height(2.dp)
                    .offset(x = indicatorOffset)
                    .background(TextPrimary)
            )
        }
    }
}

@Composable
private fun TabItem(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = modifier.clickable(onClick = onClick).padding(bottom = 8.dp),
        textAlign = TextAlign.Center,
        color = if (selected) TextPrimary else TextSecondary,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("오늘 아이와 관련된 어떤 추억이 떠올랐나요?", color = TextSecondary) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
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
private fun PostItem(
    post: CommunityPost,
    isLiked: Boolean,
    onClick: () -> Unit,
    onToggleLike: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(post.profileImageUrl).crossfade(true).build(),
                    contentDescription = "profile",
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(InputBackground),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(post.userName, color = TextPrimary)
                    Text("${post.userAge} ${post.hashtag}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (!post.imageUrl.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .build(),
                    contentDescription = "post image",
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBackground),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))

            if (post.title.isNotEmpty()) {
                Text(post.title, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
            }

            Text(post.content, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleLike(post.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "like",
                            tint = if (isLiked) Color.Red else TextSecondary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(post.likeCount.toString(), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Text(post.date, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WritePostDialog(
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
    ) { uri -> selectedImageUri = uri }

    val hasContent = title.isNotEmpty() || content.isNotEmpty() || hashtag.isNotEmpty() || selectedImageUri != null

    Dialog(
        onDismissRequest = { if (hasContent) showCancelDialog = true else onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = BackgroundWhite
        ) {
            Column(Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("글쓰기", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { if (hasContent) showCancelDialog = true else onDismiss() }) {
                        Text("✕", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
                ) {
                    Text("제목", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("제목을 입력해 주세요.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("내용", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("내용을 입력해 주세요.") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("해시태그(선택)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hashtag,
                        onValueChange = { hashtag = it },
                        placeholder = { Text("#잠만보강아지") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("사진(선택)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    if (selectedImageUri != null) {
                        Box(Modifier.fillMaxWidth().height(120.dp)) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "selected",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .background(InputBackground)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.Add, contentDescription = "add", tint = TextSecondary, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("사진을 추가해 주세요", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }

                Button(
                    onClick = { if (title.isNotEmpty() && content.isNotEmpty()) onSubmit(title, content, hashtag, selectedImageUri) },
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    enabled = title.isNotEmpty() && content.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MainOrange, disabledContainerColor = TextSecondary)
                ) {
                    Text("등록하기", color = Color.White)
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("작성을 취소하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = { showCancelDialog = false; onDismiss() }) { Text("확인", color = MainOrange) }
            },
            dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("취소") } }
        )
    }
}

// 샘플 데이터 (DB에 넣지 않고 앱에서만 보여줌 / 항상 아래로 깔림)
private fun getSamplePosts(): List<CommunityPost> {
    return listOf(
        // 마음으로 걷기
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
            category = "walk_with_heart",
            createdAt = null
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
            category = "walk_with_heart",
            createdAt = null
        ),

        // 나란히 걷기
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
            category = "walk_together",
            createdAt = null
        )
    )
}