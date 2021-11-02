/*
 * Copyright (C) 2020 Sonique Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.sonique.sqlcipherperformance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.builders.AssistActionBuilder
import fr.sonique.sqlcipherperformance.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(this, baseContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.insertsButton.setOnClickListener { _ ->
            viewModel.onInsertsClicked()
        }
        binding.selectsIndexedButton.setOnClickListener { _ ->
            viewModel.onSelectIndexedClicked()
        }
        binding.selectsNoindexButton.setOnClickListener { _ ->
            viewModel.onSelectNoIndexClicked()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.onCancelClicked()
        }

        binding.transactionCount.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    val length = when (progress) {
                        1 -> 10000
                        2 -> 50000
                        3 -> 100000
                        else -> 1000
                    }
                    viewModel.updateQuerySize(length)
                }
            }
        })

        binding.securityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.no_encryption -> {
                    viewModel.encrypted = false
                    viewModel.encryptedWithMemorySecurity = false
                    viewModel.runAll = false
                }
                R.id.encrypted -> {
                    viewModel.encrypted = true
                    viewModel.encryptedWithMemorySecurity = false
                    viewModel.runAll = false
                }
                R.id.encrypted_with_memory_security -> {
                    viewModel.encrypted = true
                    viewModel.encryptedWithMemorySecurity = true
                    viewModel.runAll = false
                }
                R.id.run_all -> {
                    viewModel.runAll = true
                }
            }
        }

        binding.results.setOnLongClickListener {
            try {
                val clipboard: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    ClipData.newPlainText("sqlperformanceresults", binding.results.text.toString())
                clipboard.setPrimaryClip(clip)

                Toast.makeText(baseContext, "Results copied", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            true
        }

        // LiveData
        viewModel.enableUI.observe(this, {
            setUIEnabled(it)
        })

        viewModel.results.observe(this, {
            binding.results.text = it

            // Scroll down
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        })

        viewModel.querySize.observe(this, {
            binding.count = it
        })

        // check for deeplinks
        intent?.handleIntent()
    }

    private fun setUIEnabled(enabled: Boolean) {
        binding.cancelButton.isEnabled = !enabled

        binding.controllers.referencedIds.forEach {
            binding.root.findViewById<View>(it).isEnabled = enabled
        }
    }

    /**
     * Handle new intents that are coming while the activity is on foreground since we set the
     * launchMode to be singleTask, avoiding multiple instances of this activity to be created.
     *
     * See [launchMode](https://developer.android.com/guide/topics/manifest/activity-element#lmode)
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.handleIntent()
    }

    /**
     * Handles the action from the intent base on the type.
     *
     * @receiver the intent to handle
     */
    private fun Intent.handleIntent() {
        when (action) {
            // When the action is triggered by a deep-link, Intent.Action_VIEW will be used
            Intent.ACTION_VIEW -> handleDeepLink(data)
            // Otherwise start the app as you would normally do.
            else -> Unit
        }
    }

    /**
     * Use the URI provided by the intent to handle the different deep-links
     */
    private fun handleDeepLink(data: Uri?) {
        // path is normally used to indicate which view should be displayed
        // i.e https://sonique.assistant.test/start?exerciseType="Running" -> path = "start"
        var actionHandled = true
        when (data?.path) {
            DeepLink.OPEN -> {
                val featureRequested = data.getQueryParameter(DeepLink.Params.FEATURE).orEmpty()
                viewModel.startRequestedFeature(featureRequested)
            }
            else -> {
                actionHandled = false
                Log.w("MainActivity", "DeepLink, path: ${data?.path} not handled")
                Unit
            }
        }

        notifyActionSuccess(actionHandled)
    }

    /**
     * Log a success or failure of the received action based on if your app could handle the action
     *
     * Required to help giving Assistant visibility over success or failure of an action sent to the app.
     * Otherwise, it can’t confidently send user’s to your app for fulfillment.
     */
    private fun notifyActionSuccess(succeed: Boolean) {

        intent.getStringExtra(DeepLink.Actions.ACTION_TOKEN_EXTRA)?.let { actionToken ->
            val actionStatus = if (succeed) {
                Action.Builder.STATUS_TYPE_COMPLETED
            } else {
                Action.Builder.STATUS_TYPE_FAILED
            }
            val action = AssistActionBuilder()
                .setActionToken(actionToken)
                .setActionStatus(actionStatus)
                .build()

            // Send the end action to the Firebase app indexing.
            FirebaseUserActions.getInstance(getApplicationContext()).end(action)
        }
    }

}

/**
 * Static object that defines the different deep-links
 */
object DeepLink {
    const val OPEN = "/open"

    /**
     * Parameter types for the deep-links
     */
    object Params {
        const val FEATURE = "feature"
    }

    object Actions {
        const val ACTION_TOKEN_EXTRA = "actions.fulfillment.extra.ACTION_TOKEN"
    }
}

@Suppress("UNCHECKED_CAST")
class MainActivityViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val context: Context,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return MainActivityViewModel(context) as T
    }
}