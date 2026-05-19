package com.jnetaol.subsync.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jnetaol.subsync.SubSyncApp
import com.jnetaol.subsync.data.model.SubtitleEntry
import com.jnetaol.subsync.data.model.SubtitleTrack
import com.jnetaol.subsync.data.model.VideoFile
import com.jnetaol.subsync.engine.SubtitleEngine
import com.jnetaol.subsync.engine.SubtitleMatch
import com.jnetaol.subsync.engine.Translator
import com.jnetaol.subsync.engine.VideoScanner
import com.jnetaol.subsync.logger.DebugLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    private val videoDao = SubSyncApp.instance.database.videoFileDao()
    private val trackDao = SubSyncApp.instance.database.subtitleTrackDao()
    private val entryDao = SubSyncApp.instance.database.subtitleEntryDao()

    val allVideos: StateFlow<List<VideoFile>> = videoDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredVideos: StateFlow<List<VideoFile>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) videoDao.getAll()
            else videoDao.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVideo = MutableStateFlow<VideoFile?>(null)
    val selectedVideo: StateFlow<VideoFile?> = _selectedVideo.asStateFlow()

    private val _subtitlesForVideo = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val subtitlesForVideo: StateFlow<List<SubtitleTrack>> = _subtitlesForVideo.asStateFlow()

    private val _currentEntries = MutableStateFlow<List<SubtitleEntry>>(emptyList())
    val currentEntries: StateFlow<List<SubtitleEntry>> = _currentEntries.asStateFlow()

    private val _subtitleMatches = MutableStateFlow<List<SubtitleMatch>>(emptyList())
    val subtitleMatches: StateFlow<List<SubtitleMatch>> = _subtitleMatches.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _syncOffset = MutableStateFlow(0L)
    val syncOffset: StateFlow<Long> = _syncOffset.asStateFlow()

    private val _showTranslation = MutableStateFlow(false)
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    private val _targetLanguage = MutableStateFlow("es")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    private val _searchTextQuery = MutableStateFlow("")
    val searchTextQuery: StateFlow<String> = _searchTextQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SubtitleEntry>>(emptyList())
    val searchResults: StateFlow<List<SubtitleEntry>> = _searchResults.asStateFlow()

    private val _editingEntry = MutableStateFlow<SubtitleEntry?>(null)
    val editingEntry: StateFlow<SubtitleEntry?> = _editingEntry.asStateFlow()

    fun scanVideos() {
        if (_isScanning.value) return
        _isScanning.value = true
        viewModelScope.launch {
            try {
                val found = VideoScanner.scanDirectories()
                val existing = videoDao.getAll().first()
                val existingPaths = existing.map { it.filePath }.toSet()

                val newVideos = found.filter { it.filePath !in existingPaths }.map { v ->
                    VideoFile(
                        filePath = v.filePath,
                        fileName = v.fileName,
                        fileSize = v.fileSize
                    )
                }

                if (newVideos.isNotEmpty()) {
                    videoDao.insertAll(newVideos)
                    DebugLogger.i("SS-050", "Added ${newVideos.size} new videos")
                }
            } catch (e: Exception) {
                DebugLogger.e("SS-050", "Scan failed", e)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectVideo(video: VideoFile) {
        _selectedVideo.value = video
        loadSubtitlesForVideo(video.id)
        searchMatches(video.fileName)
    }

    fun clearSelectedVideo() {
        _selectedVideo.value = null
        _subtitlesForVideo.value = emptyList()
        _subtitleMatches.value = emptyList()
        _currentEntries.value = emptyList()
    }

    private fun loadSubtitlesForVideo(videoId: Long) {
        viewModelScope.launch {
            trackDao.getByVideoId(videoId).collect { tracks ->
                _subtitlesForVideo.value = tracks
            }
        }
    }

    fun searchMatches(fileName: String) {
        viewModelScope.launch {
            _subtitleMatches.value = SubtitleEngine.searchMatches(fileName)
        }
    }

    fun downloadSubtitle(match: SubtitleMatch) {
        viewModelScope.launch {
            try {
                val video = _selectedVideo.value ?: return@launch
                DebugLogger.i("SS-060", "Downloading subtitle: ${match.language}")

                val entries = SubtitleEngine.generateMockSubtitles(match.entryCount)

                val track = SubtitleTrack(
                    videoFileId = video.id,
                    language = match.language,
                    fileName = "${video.fileName.removeSuffix(".mp4").removeSuffix(".mkv")}.${match.language.lowercase()}.srt",
                    entryCount = entries.size,
                    source = "download"
                )

                val trackId = trackDao.insert(track)
                val entriesWithTrack = entries.map { it.copy(trackId = trackId) }
                entryDao.insertAll(entriesWithTrack)

                DebugLogger.i("SS-061", "Subtitle downloaded with ${entries.size} entries")
            } catch (e: Exception) {
                DebugLogger.e("SS-060", "Download failed", e)
            }
        }
    }

    fun loadEntries(trackId: Long) {
        viewModelScope.launch {
            entryDao.getByTrackId(trackId).collect { entries ->
                _currentEntries.value = entries
            }
        }
    }

    private suspend fun loadEntriesList(trackId: Long): List<SubtitleEntry> {
        return entryDao.getByTrackIdList(trackId)
    }

    fun applySyncToCurrentEntries(offsetMs: Long) {
        viewModelScope.launch {
            val entries = _currentEntries.value
            val adjusted = SubtitleEngine.applySyncOffset(entries, offsetMs)
            for (entry in adjusted) {
                entryDao.update(entry)
            }
            _syncOffset.value += offsetMs
            DebugLogger.i("SS-070", "Sync applied: ${offsetMs}ms")
        }
    }

    fun scaleCurrentEntries(factor: Float) {
        viewModelScope.launch {
            val entries = _currentEntries.value
            val adjusted = SubtitleEngine.scaleTiming(entries, factor)
            for (entry in adjusted) {
                entryDao.update(entry)
            }
            DebugLogger.i("SS-071", "Scale applied: $factor")
        }
    }

    fun updateEntry(entry: SubtitleEntry) {
        viewModelScope.launch {
            entryDao.update(entry)
        }
    }

    fun translateCurrentEntries() {
        viewModelScope.launch {
            val entries = _currentEntries.value
            val translated = Translator.translateEntries(entries, _targetLanguage.value)
            for (entry in translated) {
                entryDao.update(entry)
            }
            DebugLogger.i("SS-080", "Translated ${entries.size} entries")
        }
    }

    fun setShowTranslation(show: Boolean) {
        _showTranslation.value = show
    }

    fun setTargetLanguage(lang: String) {
        _targetLanguage.value = lang
    }

    fun searchText(query: String) {
        _searchTextQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                _searchResults.value = entryDao.search(query)
            }
        }
    }

    fun exportSrt(trackId: Long): String? {
        return runBlockingMain {
            try {
                val entries = entryDao.getByTrackIdList(trackId)
                if (entries.isEmpty()) return@runBlockingMain null
                SubtitleEngine.writeSrt(entries, _showTranslation.value)
            } catch (e: Exception) {
                DebugLogger.e("SS-090", "Export failed", e)
                null
            }
        }
    }

    fun getEntriesForTrack(trackId: Long): List<SubtitleEntry> {
        return runBlockingMain {
            entryDao.getByTrackIdList(trackId)
        } ?: emptyList()
    }

    fun deleteTrack(track: SubtitleTrack) {
        viewModelScope.launch {
            entryDao.deleteByTrackId(track.id)
            trackDao.delete(track)
        }
    }

    fun deleteVideo(video: VideoFile) {
        viewModelScope.launch {
            trackDao.deleteByVideoId(video.id)
            videoDao.delete(video)
        }
    }

    fun setEditingEntry(entry: SubtitleEntry?) {
        _editingEntry.value = entry
    }

    fun clearAllData() {
        viewModelScope.launch {
            videoDao.deleteAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel() as T
            }
        }
    }
}

private fun <T> runBlockingMain(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking {
        block()
    }
}
