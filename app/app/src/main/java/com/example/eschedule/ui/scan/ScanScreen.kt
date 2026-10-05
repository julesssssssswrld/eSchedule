package com.example.eschedule.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.eschedule.theme.AppBackground
import com.example.eschedule.ui.components.AppIcons
import com.example.eschedule.ui.components.PageTitle
import com.example.eschedule.ui.components.appShadow

// ── Design tokens ──────────────────────────────────────────────────────────────
private val OverlayBtnBg     = Color(0x99000000)
private val OverlayBtnSize   = 52.dp
private val OverlayIconSize  = 24.dp
private val TorchActiveColor = Color(0xFFFFE066)

@Composable
fun ScanScreen(
    captureRequested: Boolean = false,
    onCaptureHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ── Permission ────────────────────────────────────────────────────────────
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ── Camera state ─────────────────────────────────────────────────────────
    var cameraSelectors by remember {
        mutableStateOf(listOf(CameraSelector.DEFAULT_BACK_CAMERA))
    }
    var selectorIndex by remember { mutableIntStateOf(0) }
    var torchEnabled  by remember { mutableStateOf(false) }
    var activeCamera: Camera?              by remember { mutableStateOf(null) }
    var imageCaptureUc: ImageCapture?      by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
    }

    val currentSelector = cameraSelectors[selectorIndex]

    LaunchedEffect(currentSelector) { torchEnabled = false }

    // ── Shutter trigger ───────────────────────────────────────────────────────
    LaunchedEffect(captureRequested) {
        if (captureRequested && hasPermission) {
            val ic = imageCaptureUc
            if (ic != null) {
                ic.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                            // TODO: process image.toBitmap() for schedule parsing
                            image.close()
                        }
                        override fun onError(exc: ImageCaptureException) { /* log */ }
                    },
                )
            }
            onCaptureHandled()
        }
    }

    // ── Root ──────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp),
        ) {
            PageTitle(title = "Scan Class Schedule")

            // ── Camera preview card ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .appShadow(cornerRadius = 16.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                when {
                    !hasPermission -> {
                        Text(
                            text = "Camera permission required.\nTap to grant.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF999999),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 32.dp),
                        )
                    }
                    else -> {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                            },
                            update = { previewView ->
                                val future = ProcessCameraProvider.getInstance(context)
                                future.addListener({
                                    val provider = future.get()
                                    cameraProvider = provider
                                    provider.unbindAll()

                                    if (cameraSelectors.size == 1) {
                                        val infos = provider.getAvailableCameraInfos()
                                        val selectors = mutableListOf<CameraSelector>()
                                        if (infos.any { info ->
                                                runCatching {
                                                    CameraSelector.DEFAULT_BACK_CAMERA
                                                        .filter(infos).contains(info)
                                                }.getOrDefault(false)
                                            }
                                        ) selectors.add(CameraSelector.DEFAULT_BACK_CAMERA)
                                        if (infos.any { info ->
                                                runCatching {
                                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                                        .filter(infos).contains(info)
                                                }.getOrDefault(false)
                                            }
                                        ) selectors.add(CameraSelector.DEFAULT_FRONT_CAMERA)
                                        if (selectors.isNotEmpty()) cameraSelectors = selectors
                                    }

                                    val capture = ImageCapture.Builder().build()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }

                                    runCatching {
                                        activeCamera = provider.bindToLifecycle(
                                            lifecycleOwner,
                                            currentSelector,
                                            preview,
                                            capture,
                                        )
                                        imageCaptureUc = capture
                                    }
                                }, ContextCompat.getMainExecutor(context))
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // ── Disclaimer / hint text ────────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Unclear text, lighting, or unique table formats can cause errors—" +
                    "ensure all days, times, and room assignments match your official " +
                    "schedule before tapping save.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFAAAAAA),
                lineHeight = 15.sp,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )
        }

        // ── Floating overlay buttons — same bottom=120dp as EScheduleFab ──────
        if (hasPermission) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (cameraSelectors.size > 1) {
                    CameraOverlayButton(onClick = {
                        selectorIndex = (selectorIndex + 1) % cameraSelectors.size
                    }) {
                        Icon(
                            imageVector = AppIcons.FlipCamera,
                            contentDescription = "Switch camera",
                            tint = Color.White,
                            modifier = Modifier.size(OverlayIconSize),
                        )
                    }
                }

                val torchTint by animateColorAsState(
                    targetValue = if (torchEnabled) TorchActiveColor else Color.White,
                    animationSpec = tween(150),
                    label = "torchTint",
                )
                CameraOverlayButton(onClick = {
                    torchEnabled = !torchEnabled
                    activeCamera?.cameraControl?.enableTorch(torchEnabled)
                }) {
                    Icon(
                        imageVector = AppIcons.Torch,
                        contentDescription = "Toggle flashlight",
                        tint = torchTint,
                        modifier = Modifier.size(OverlayIconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraOverlayButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(OverlayBtnSize)
            .background(OverlayBtnBg, CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
