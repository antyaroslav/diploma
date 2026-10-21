package ru.edu.qamid.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.edu.qamid.R
import ru.edu.qamid.dto.News
import ru.edu.qamid.viewmodel.NewsViewModel

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        val navHostView = findViewById<View>(R.id.nav_host_fragment)
        val statusBarBackground = findViewById<View>(R.id.status_bar_background)
        val navigationBarBackground = findViewById<View>(R.id.navigation_bar_background)
        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController

        fun applySystemBarInsets(isSplashScreen: Boolean) {
            window.statusBarColor = if (isSplashScreen) {
                Color.TRANSPARENT
            } else {
                ContextCompat.getColor(this, R.color.primary)
            }
            ViewCompat.setOnApplyWindowInsetsListener(navHostView) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                if (isSplashScreen) {
                    view.setPadding(0, 0, 0, 0)
                    statusBarBackground.layoutParams =
                        statusBarBackground.layoutParams.apply { height = 0 }
                    navigationBarBackground.layoutParams =
                        navigationBarBackground.layoutParams.apply { height = 0 }
                    statusBarBackground.requestLayout()
                    navigationBarBackground.requestLayout()
                } else {
                    view.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                    )
                    statusBarBackground.layoutParams =
                        (statusBarBackground.layoutParams as ViewGroup.LayoutParams).apply {
                            height = systemBars.top
                        }
                    navigationBarBackground.layoutParams =
                        (navigationBarBackground.layoutParams as ViewGroup.LayoutParams).apply {
                            height = systemBars.bottom
                        }
                    statusBarBackground.requestLayout()
                    navigationBarBackground.requestLayout()
                }
                insets
            }
            ViewCompat.requestApplyInsets(navHostView)
        }

        applySystemBarInsets(navController.currentDestination?.id == R.id.splashScreenFragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            applySystemBarInsets(destination.id == R.id.splashScreenFragment)
        }

        val newsViewModel: NewsViewModel by viewModels()

        val categories =
            listOf(
                News.Category(1, "Объявление", false),
                News.Category(2, "День рождения", false),
                News.Category(3, "Зарплата", false),
                News.Category(4, "Профсоюз", false),
                News.Category(5, "Праздник", false),
                News.Category(6, "Массаж", false),
                News.Category(7, "Благодарность", false),
                News.Category(8, "Нужна помощь", false)
            )

        newsViewModel.initializationListNewsCategories(categories)
    }
}
