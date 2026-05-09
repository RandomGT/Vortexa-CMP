package androidx.activity.compose

import androidx.compose.runtime.Composable
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
}

@Composable
fun <I, O> rememberLauncherForActivityResult(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit,
): ActivityResultLauncher<I> = ActivityResultLauncher()

