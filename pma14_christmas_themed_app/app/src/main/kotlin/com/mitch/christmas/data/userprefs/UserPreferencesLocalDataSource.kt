package com.mitch.christmas.data.userprefs

import androidx.datastore.core.DataStore
import com.mitch.christmas.data.userprefs.UserPreferencesProtoModel.ChristmasThemePreferenceProto
import kotlinx.coroutines.flow.Flow

/**
 * [UserPreferencesLocalDataSource] is the mediator between [UserPreferencesProtoModel] Datastore
 * and the repo to exchange data from the Datastore file
 *
 * @property userPreferences is the actual [UserPreferencesProtoModel] Datastore
 */
class UserPreferencesLocalDataSource(
    private val userPreferences: DataStore<UserPreferencesProtoModel>
) {
    val protoPreferences: Flow<UserPreferencesProtoModel> = userPreferences.data
    suspend fun setTheme(theme: ChristmasThemePreferenceProto) {
        userPreferences.updateData {
            it.copy {
                this.theme = theme
            }
        }
    }
}
