package com.example.android.memoization

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android.memoization.notifications.NotificationReceiver
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.NewFolderScreen
import com.example.android.memoization.ui.composables.screens.FoldersScreen
import com.example.android.memoization.ui.composables.screens.MemorizationScreen
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.ui.viewmodel.StackViewModel
import com.example.android.memoization.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var folderViewModel: FolderViewModel
    @Inject
    lateinit var stackViewModel: StackViewModel

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val job = SupervisorJob()
        val scope = CoroutineScope(job)
        scope.launch {
            val prefs = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
            val timeToTrigger = prefs.getLong(TRIGGER_AT_LABEL, SystemClock.elapsedRealtime()) // TODO how to set to 12 pm by default
            val repeatInterval = prefs.getLong(REPEAT_INTERVAl_LABEL, day_in_millis)
            setUpNotifications(timeToTrigger, repeatInterval)
        }

        setContent {
            val navControllerObj = rememberNavController()

            MemoizationTheme {
                NavHost(
                    navController = navControllerObj,
                    startDestination = NavScreens.Folders.route
                ) {
                    composable(NavScreens.Folders.route) {
                        FoldersScreen(
                            navController = navControllerObj,
                            folderViewModel = folderViewModel,
                            stackViewModel = stackViewModel
                        )
                    }

                    composable(NavScreens.NewFolder.route) {
                        NewFolderScreen(
                            navController = navControllerObj
                        )
                    }

                    composable(NavScreens.Stack.route) {
                        StackScreen(
                            navContoller = navControllerObj,
                            viewModel = stackViewModel
                        )
                    }

                    composable(
                        NavScreens.NewPair.route,
                        arguments = listOf(navArgument(IS_EDIT_MODE) {
                            type = NavType.BoolType
                            defaultValue = false
                        })
                    ) {
                        AddNewPairScreen(
                            navController = navControllerObj,
                            viewModel = stackViewModel,
                            editMode = it.arguments?.getBoolean(IS_EDIT_MODE) ?: false
                        )
                    }

                    composable(NavScreens.Memorization.route) {
                        MemorizationScreen(
                            navController = navControllerObj,
                            viewModel = folderViewModel,
                            stackViewModel = stackViewModel
                        )
                    }
                }
            }
        }
    }

    private fun setUpNotifications(timeToTrigger: Long, repeatInterval: Long) {
        val alarmMgr: AlarmManager? = this.getSystemService(ALARM_SERVICE) as? AlarmManager?

        val requestCode = Date().time
        val alarmIntent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode.toInt(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmMgr?.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            timeToTrigger,
            repeatInterval,
            pendingIntent
        )
    }
}

