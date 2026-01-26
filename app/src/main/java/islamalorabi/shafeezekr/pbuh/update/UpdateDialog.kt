package islamalorabi.shafeezekr.pbuh.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import islamalorabi.shafeezekr.pbuh.R

@Composable
fun UpdateDialog(
    release: GithubRelease,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.6).dp
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { 
            Text(
                text = stringResource(R.string.update_available),
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = { 
            Text(
                text = stringResource(R.string.update_message, release.tagName) + "\n\n${release.body}",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDialogHeight)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text(
                    stringResource(R.string.update_button),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.later_button),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
