package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.GeneratedPolicy
import com.example.domain.PolicyGeneratorEngine
import com.example.export.GrcPdfExporter
import com.example.ui.GrcViewModel
import com.example.ui.WorkspaceScreen
import com.example.ui.components.SpatialBackdrop
import com.example.ui.screens.GeneratorFormScreen
import com.example.ui.screens.PolicyWorkspaceScreen
import com.example.ui.theme.NaijaGrcTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NaijaGrcTheme {
                NaijaGrcApp()
            }
        }
    }
}

@Composable
fun NaijaGrcApp(
    viewModel: GrcViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingExportPolicies by remember { mutableStateOf<List<GeneratedPolicy>>(emptyList()) }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val profile = uiState.generatedProfile
        val policiesToWrite = pendingExportPolicies
        if (uri != null && profile != null && policiesToWrite.isNotEmpty()) {
            scope.launch {
                viewModel.setExporting(true)
                val result = GrcPdfExporter.writePoliciesToUri(
                    context = context,
                    uri = uri,
                    profile = profile,
                    policies = policiesToWrite
                )
                viewModel.setExporting(false)
                result.onSuccess {
                    viewModel.showStatusMessage(
                        if (policiesToWrite.size == 1) {
                            "${policiesToWrite.first().type.shortTitle} PDF saved!"
                        } else {
                            "All 3 policies exported to PDF bundle!"
                        }
                    )
                }.onFailure {
                    viewModel.showStatusMessage(context.getString(R.string.msg_pdf_error))
                }
            }
        }
    }

    SpatialBackdrop {
        AnimatedContent(
            targetState = uiState.activeScreen,
            transitionSpec = {
                if (targetState == WorkspaceScreen.POLICIES) {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 4 } + fadeOut())
                }
            },
            label = "main_workspace_transition"
        ) { screen ->
            when (screen) {
                WorkspaceScreen.FORM -> {
                    GeneratorFormScreen(
                        uiState = uiState,
                        onCompanyNameChange = viewModel::onCompanyNameChange,
                        onSizeSelected = viewModel::onSizeSelected,
                        onIndustrySelected = viewModel::onIndustrySelected,
                        onDpoEmailChange = viewModel::onDpoEmailChange,
                        onTogglePolicyType = viewModel::togglePolicyTypeSelection,
                        onSelectAllPolicies = viewModel::selectAllPolicyTypes,
                        onSelectCorePolicies = viewModel::selectCoreThreePolicies,
                        onApplyPreset = viewModel::applyQuickPreset,
                        onGenerateClick = viewModel::generatePolicies
                    )
                }

                WorkspaceScreen.POLICIES -> {
                    PolicyWorkspaceScreen(
                        uiState = uiState,
                        onBackToForm = viewModel::navigateBackToForm,
                        onSelectPolicy = viewModel::selectPolicyType,
                        onSelectTab = viewModel::setWorkspaceTab,
                        onToggleChecklistItem = viewModel::toggleChecklistItem,
                        onSearchChange = viewModel::onClauseSearchChange,
                        onCopyPolicy = { policy, profile ->
                            val text = PolicyGeneratorEngine.formatPolicyPlainText(policy, profile)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(policy.title, text))
                            viewModel.showStatusMessage(context.getString(R.string.msg_copied))
                        },
                        onCreateRevision = viewModel::createManualRevision,
                        onRevertVersion = viewModel::revertToVersion,
                        onExportActivePdf = { policy ->
                            val profile = uiState.generatedProfile ?: return@PolicyWorkspaceScreen
                            val singleList = listOf(policy)
                            pendingExportPolicies = singleList
                            savePdfLauncher.launch(
                                GrcPdfExporter.buildSuggestedFileName(profile.companyName, singleList)
                            )
                        },
                        onExportBundlePdf = {
                            val profile = uiState.generatedProfile ?: return@PolicyWorkspaceScreen
                            val allPolicies = uiState.generatedPolicies
                            if (allPolicies.isNotEmpty()) {
                                pendingExportPolicies = allPolicies
                                savePdfLauncher.launch(
                                    GrcPdfExporter.buildSuggestedFileName(profile.companyName, allPolicies)
                                )
                            }
                        },
                        onShareBundlePdf = {
                            val profile = uiState.generatedProfile ?: return@PolicyWorkspaceScreen
                            val allPolicies = uiState.generatedPolicies
                            if (allPolicies.isNotEmpty()) {
                                scope.launch {
                                    viewModel.setExporting(true)
                                    val result = GrcPdfExporter.createShareablePdfUri(
                                        context = context,
                                        profile = profile,
                                        policies = allPolicies
                                    )
                                    viewModel.setExporting(false)
                                    result.onSuccess { shareUri ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, shareUri)
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "${profile.companyName} — NaijaGRC NDPA 2023 & ISO 27001 Policy Pack"
                                            )
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Share GRC Policy PDF")
                                        )
                                    }.onFailure {
                                        viewModel.showStatusMessage(context.getString(R.string.msg_pdf_error))
                                    }
                                }
                            }
                        },
                        onDismissStatus = { viewModel.showStatusMessage(null) }
                    )
                }
            }
        }
    }
}
