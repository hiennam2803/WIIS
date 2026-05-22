package vhdt.wiis.model
sealed class SyncState {
    object     Idle     : SyncState()
    object     Loading  : SyncState()
    object     NoPlugin : SyncState()
    data class Success(val items: List<ScheduleItem>) : SyncState()
    data class Error(val message: String) : SyncState()
}