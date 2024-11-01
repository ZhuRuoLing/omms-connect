package icu.takeneko.omms.connect.server.activity.minecraft.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.GsonUtils
import icu.takeneko.omms.client.data.controller.Controller
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.databinding.ActivityMinecraftServerControlBinding
import icu.takeneko.omms.connect.util.AssetsUtil
import icu.takeneko.omms.connect.util.getIconFromDesc

class MinecraftServerControlActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMinecraftServerControlBinding
    private lateinit var mcImage: ImageView
    private lateinit var mcText: TextView
    private lateinit var mcText2: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMinecraftServerControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val controller = GsonUtils.fromJson(intent.getStringExtra("data"), Controller::class.java)
        setSupportActionBar(binding.appBarMinecraftServerControl.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController =
            findNavController(R.id.nav_host_fragment_content_minecraft_server_control)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_status, R.id.nav_console, R.id.nav_control
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val header = navView.inflateHeaderView(R.layout.nav_header_minecraft_server_control)
        mcImage = header.findViewById(R.id.mc_image)
        mcText = header.findViewById(R.id.mc_text)
        mcText2 = header.findViewById(R.id.mc_text2)

        mcImage.setImageDrawable(
            intent.getStringExtra("server_type")?.let {
                this.getIconFromDesc(it)
            }
        )
        mcText.text = "${controller.name} (${controller.type.lowercase().replaceFirstChar { char -> char + ('A'.code - 'a'.code) }} Server)"
        mcText2.text = ""
        CacheDiskUtils.getInstance().put("mcinfo", intent.getStringExtra("data"))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            findNavController(R.id.nav_host_fragment_content_minecraft_server_control)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}