package com.example.botchat.converstation.ui.historySetting.handler

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.example.botchat.converstation.viewModel.VoidChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object SettingsDataHandler {

    @Composable
    fun fetchSettingsRealtime(viewModel: VoidChatViewModel): MutableState<List<Pair<String, Map<String, Any>>>> {
        val settingsList = remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return settingsList

        DisposableEffect(Unit) {
            val listener = viewModel.firestore.collection("void_settings")
                .document(userEmail)
                .collection("settings")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("SettingsDataHandler", "Firestore listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val allDocs = it.documents
                        val defaultDoc = allDocs.find { doc -> doc.getBoolean("isDefault") == true }
                        val others = allDocs.filterNot { doc -> doc.getBoolean("isDefault") == true }
                        val orderedDocs = if (defaultDoc != null) {
                            listOf(defaultDoc) + others.reversed()
                        } else {
                            allDocs.reversed()
                        }
                        val settings = orderedDocs.mapNotNull { doc ->
                            Pair(doc.id, doc.data ?: emptyMap())
                        }
                        settingsList.value = settings
                    }
                }

            onDispose {
                listener.remove()
            }
        }

        return settingsList
    }

    fun deleteSetting(viewModel: VoidChatViewModel, settingId: String, context: android.content.Context) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        viewModel.firestore.collection("void_settings")
            .document(userEmail)
            .collection("settings")
            .document(settingId)
            .delete()
            .addOnSuccessListener {
                viewModel.refreshConversation(context, false)
            }
    }

    fun setDefaultSetting(viewModel: VoidChatViewModel, settingId: String, context: android.content.Context) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        viewModel.firestore.collection("void_settings")
            .document(userEmail)
            .collection("settings")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = viewModel.firestore.batch()
                querySnapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isDefault", false)
                }
                val currentRef = viewModel.firestore
                    .collection("void_settings")
                    .document(userEmail)
                    .collection("settings")
                    .document(settingId)
                batch.update(currentRef, "isDefault", true)
                batch.commit().addOnSuccessListener {
                    viewModel.viewModelScope.launch {
                        viewModel.reloadUsedDoc(context,userEmail)
                    }
                }
            }
    }

    fun deleteAllNonDefaultSettings(viewModel: VoidChatViewModel, context: android.content.Context) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        viewModel.firestore.collection("void_settings")
            .document(userEmail)
            .collection("settings")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val isPinned = doc.getBoolean("isDefault") == true
                    if (!isPinned) {
                        doc.reference.delete()
                    }
                }
                viewModel.refreshConversation(context, false)
            }
    }
}