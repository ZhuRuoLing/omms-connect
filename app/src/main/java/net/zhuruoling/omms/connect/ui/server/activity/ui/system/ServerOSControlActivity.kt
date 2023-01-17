package net.zhuruoling.omms.connect.ui.server.activity.ui.system

import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.GsonUtils
import com.google.android.material.navigation.NavigationView
import net.zhuruoling.omms.client.system.SystemInfo
import net.zhuruoling.ommsconnect.R
import net.zhuruoling.ommsconnect.databinding.ActivityServerOscontrolBinding
import net.zhuruoling.omms.connect.ui.util.Assets
import net.zhuruoling.omms.connect.ui.util.getSystemType

class ServerOSControlActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityServerOscontrolBinding
    private lateinit var osImage:ImageView
    private lateinit var osText: TextView
    private lateinit var osText2: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerOscontrolBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        val info = GsonUtils.fromJson(intent.getStringExtra("data"), SystemInfo::class.java)
        setSupportActionBar(binding.appBarServerOscontrol.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_server_oscontrol)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val header = navView.inflateHeaderView(R.layout.nav_header_server_oscontrol)
        osImage = header.findViewById(R.id.os_image)
        osText = header.findViewById(R.id.control_server_os)
        osText2 = header.findViewById(R.id.control_os_server_intro)


        osText2.text = intent.getStringExtra("data")
        val type = getSystemType(info.osName)
        osImage.setImageDrawable(Assets.getServerIcon(type, this))
        osText.text = "${info.networkInfo.hostName}"
        osText2.text = "${info.osName} ${info.osVersion} ${info.osArch}"

        CacheDiskUtils.getInstance().put("sysinfo", intent.getStringExtra("data"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.server_o_s_control, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_server_oscontrol)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}