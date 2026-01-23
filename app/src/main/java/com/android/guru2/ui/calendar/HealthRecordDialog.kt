package com.android.guru2.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun HealthRecordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double) -> Unit
) {
    var symptom by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var foodIntake by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)  // 풀스크린!
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),  // 100% 차지!
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "닫기",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "건강 기록",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "취소",
                            modifier = Modifier.size(32.dp)  // 크기 키움!
                        )
                    }
                }

                Divider()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 이상증상
                    Text(
                        text = "이상증상",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    TextField(
                        value = symptom,
                        onValueChange = { symptom = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "예) 미열",
                                color = Color.LightGray
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 대소변
                    Text(
                        text = "대소변",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    TextField(
                        value = treatment,
                        onValueChange = { treatment = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "예) 보통 변 1회, 투명한 노란색 소변 3회",
                                color = Color.LightGray
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 식사량
                    Text(
                        text = "식사량",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    TextField(
                        value = foodIntake,
                        onValueChange = { foodIntake = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "예) 사료 300g, 캔 70g, 물 250ml",
                                color = Color.LightGray
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 몸무게
                    Text(
                        text = "몸무게",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = weight,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    weight = it
                                }
                            },
                            modifier = Modifier.width(120.dp),
                            placeholder = {
                                Text(
                                    text = "예) 4.2",
                                    color = Color.LightGray
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "kg",
                            fontSize = 16.sp
                        )
                    }
                }

                // 저장 버튼
                Button(
                    onClick = {
                        val weightValue = weight.toDoubleOrNull()
                        if (symptom.isNotEmpty() && treatment.isNotEmpty() &&
                            foodIntake.isNotEmpty() && weightValue != null) {
                            showSaveConfirmDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (symptom.isNotEmpty() && treatment.isNotEmpty() &&
                            foodIntake.isNotEmpty() && weight.toDoubleOrNull() != null) {
                            Color(0xFFF0724A)
                        } else {
                            Color(0xFFCCCCCC)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = symptom.isNotEmpty() && treatment.isNotEmpty() &&
                            foodIntake.isNotEmpty() && weight.toDoubleOrNull() != null
                ) {
                    Text(
                        text = "저장하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // 저장 확인 다이얼로그
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = null,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color(0xFFF0724A),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "저장 완료!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val weightValue = weight.toDoubleOrNull() ?: 0.0
                        onSave(symptom, treatment, foodIntake, weightValue)
                        showSaveConfirmDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "확인",
                        color = Color(0xFFF0724A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}