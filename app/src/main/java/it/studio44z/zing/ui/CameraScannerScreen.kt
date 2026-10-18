package it.studio44z.zing.ui

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CameraScannerScreen(
    qrEnabled: Boolean = true,
    vibrateOnScan: Boolean = true,
    onCodeDetected: (String) -> Unit,
    onClose: () -> Unit,
) {
    val S = LocalAppStrings.current
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) { createCameraPreview(context) }
    val providerFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val handled = remember { AtomicBoolean(false) }
    val latestOnCodeDetected = rememberUpdatedState(onCodeDetected)
    val scanner = remember(qrEnabled) {
        val builder = BarcodeScannerOptions.Builder()
        if (qrEnabled) {
            builder.setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX,
            )
        } else {
            builder.setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_ITF,
            )
        }
        BarcodeScanning.getClient(builder.build())
    }

    DisposableEffect(lifecycleOwner, previewView, providerFuture, scanner) {
        var disposed = false
        var boundProvider: ProcessCameraProvider? = null
        providerFuture.addListener({
            if (disposed) return@addListener
            try {
                val provider = providerFuture.get()
                if (disposed) return@addListener
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { proxy ->
                    if (handled.get()) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    val mediaImage = proxy.image
                    if (mediaImage == null) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    try {
                        val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                val raw = barcodes.firstNotNullOfOrNull {
                                    it.rawValue?.takeIf(String::isNotBlank) ?: it.displayValue?.takeIf(String::isNotBlank)
                                }
                                if (raw != null && handled.compareAndSet(false, true)) {
                                    if (vibrateOnScan) {
                                        runCatching { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }
                                    }
                                    latestOnCodeDetected.value(raw)
                                }
                            }
                            .addOnCompleteListener { proxy.close() }
                    } catch (_: Exception) {
                        proxy.close()
                    }
                }
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                boundProvider = provider
            } catch (_: Exception) {
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true
            runCatching { boundProvider?.unbindAll() }
            scanner.close()
            executor.shutdown()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView },
        )

        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = S.cancelBtn, tint = Color.White)
                }
                Column(Modifier.padding(start = 4.dp)) {
                    Text(S.scanTitle, color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text(S.scanSub, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(width = 280.dp, height = 180.dp)
                        .clip(RoundedCornerShape(22.dp)),
                ) {
                    Box(Modifier.fillMaxSize().background(Color.Transparent))
                }
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.size(width = 292.dp, height = 192.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CornerGuide(topLeft = true)
                        CornerGuide(topRight = true)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CornerGuide(bottomLeft = true)
                        CornerGuide(bottomRight = true)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text(S.scanOfflineNote, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onClose,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                ) {
                    Text(S.manualBarcodeBtn)
                }
            }
        }
    }
}

@Composable
private fun CornerGuide(topLeft: Boolean = false, topRight: Boolean = false, bottomLeft: Boolean = false, bottomRight: Boolean = false) {
    val shape = RoundedCornerShape(
        topStart = if (topLeft) 12.dp else 0.dp,
        topEnd = if (topRight) 12.dp else 0.dp,
        bottomStart = if (bottomLeft) 12.dp else 0.dp,
        bottomEnd = if (bottomRight) 12.dp else 0.dp,
    )
    Box(Modifier.size(28.dp).clip(shape).background(Color(0xFF7EE19E).copy(alpha = 0.95f)))
}

private fun createCameraPreview(context: Context): PreviewView = PreviewView(context).apply {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    scaleType = PreviewView.ScaleType.FILL_CENTER
    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
}