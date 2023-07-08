package com.example.android.memoization

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgument
import com.example.android.memoization.notifications.NotificationReceiver
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.NewFolderScreen
import com.example.android.memoization.ui.features.folderscreen.FoldersScreen
import com.example.android.memoization.ui.features.memoizationscreen.MemorizationScreen
import com.example.android.memoization.ui.features.addnewpair.AddNewPairScreen
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel
import com.example.android.memoization.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHost.navController
    }

//    private fun setUpNotifications(timeToTrigger: Long, repeatInterval: Long) {
//        val alarmMgr: AlarmManager? = this.getSystemService(ALARM_SERVICE) as? AlarmManager?
//
//        val requestCode = Date().time
//        val alarmIntent = Intent(this, NotificationReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            this,
//            requestCode.toInt(),
//            alarmIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        alarmMgr?.setInexactRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            timeToTrigger,
//            repeatInterval,
//            pendingIntent
//        )
//    }
}

