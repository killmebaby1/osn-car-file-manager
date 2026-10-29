/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.View
import android.view.View.OnCreateContextMenuListener
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.clipboardManager
import me.zhanghai.android.files.util.copyText
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

/**
 * 车机网络诊断：列出所有网卡(含 down/loopback)、所有 IPv4/IPv6 地址与路由表，
 * 用于排查车机热点网段与 App 所在网段不一致的问题。
 */
class FtpServerNetworkDiagnosticPreference : Preference {
    private val receiver = FtpServerUrl.createChangeReceiver(context) { update() }

    private var diagnosticText: String = ""

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        isPersistent = false
        update()
    }

    override fun onAttached() {
        super.onAttached()
        receiver.register()
    }

    override fun onDetached() {
        super.onDetached()
        receiver.unregister()
    }

    private fun update() {
        diagnosticText = buildDiagnosticText()
        summary = diagnosticText
    }

    private fun buildDiagnosticText(): String =
        buildString {
            append("=== 网卡与地址 ===\n")
            try {
                for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
                    append(networkInterface.name)
                    append(" [")
                    append(if (networkInterface.isUp) "up" else "down")
                    if (networkInterface.isLoopback) {
                        append(",loopback")
                    }
                    append("]\n")
                    val hasAddresses = networkInterface.inetAddresses.toList()
                    if (hasAddresses.isEmpty()) {
                        append("  (无地址)\n")
                    }
                    for (address in hasAddresses) {
                        append("  ")
                        append(address.hostAddress)
                        if (address is Inet4Address) {
                            append("  (IPv4)")
                        }
                        append('\n')
                    }
                }
            } catch (e: SocketException) {
                append(e.toString())
                append('\n')
            }
            append('\n')
            append("=== 路由表 (/proc/net/route) ===\n")
            try {
                val routes = java.io.File("/proc/net/route").readText()
                append(routes)
            } catch (e: Exception) {
                append(e.toString())
                append('\n')
            }
        }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.setOnCreateContextMenuListener(object : OnCreateContextMenuListener {
            override fun onCreateContextMenu(
                menu: ContextMenu,
                view: View,
                menuInfo: ContextMenuInfo?
            ) {
                val text = diagnosticText
                menu.setHeaderTitle(R.string.ftp_server_network_diagnostic_title)
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.ftp_server_network_diagnostic_copy)
                    .setOnMenuItemClickListener {
                        clipboardManager.copyText(text, context)
                        true
                    }
            }
        })
    }
}
