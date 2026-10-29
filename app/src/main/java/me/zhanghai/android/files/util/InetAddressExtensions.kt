/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import me.zhanghai.android.files.app.wifiManager
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import kotlin.reflect.KClass

fun KClass<InetAddress>.getLocalAddress(): InetAddress? {
    val wifiInfo = wifiManager.connectionInfo
    if (wifiInfo != null) {
        // WifiStateMachine doesn't support IPv6 as of P, so no need to get the original
        // InetAddress object with reflection.
        val address = wifiInfo.ipAddress
        if (address != 0) {
            return intToInetAddress(address)
        }
    }
    try {
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            if (!networkInterface.isUp || networkInterface.isLoopback) {
                continue
            }
            for (inetAddress in networkInterface.inetAddresses) {
                // Works for consumer IPv4 addresses.
                if (!inetAddress.isSiteLocalAddress) {
                    continue
                }
                return inetAddress
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    return null
}

/*
 * 枚举设备所有网卡(含车机热点 AP 网卡)的站点本地 IPv4 地址。
 * 车机可能同时有蜂窝/WLAN 客户端与热点多个网段，全部列出供用户选择。
 */
fun KClass<InetAddress>.getLocalAddresses(): List<InetAddress> {
    val addresses = mutableSetOf<InetAddress>()
    try {
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            if (!networkInterface.isUp || networkInterface.isLoopback) {
                continue
            }
            for (inetAddress in networkInterface.inetAddresses) {
                if (!inetAddress.isSiteLocalAddress) {
                    continue
                }
                addresses += inetAddress
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    return addresses
        .filterIsInstance<Inet4Address>()
        // 车机热点默认网段(192.168.43.x)排最前，其余按地址排序
        .sortedWith(
            compareBy(
                { if (it.hostAddress?.startsWith("192.168.43.") == true) 0 else 1 },
                { it.hostAddress }
            )
        )
}

/*
 * @see android.net.NetworkUtils#intToInetAddress(int)
 */
private fun intToInetAddress(hostAddress: Int): InetAddress {
    val addressBytes = byteArrayOf(
        (0xff and hostAddress).toByte(),
        (0xff and (hostAddress shr 8)).toByte(),
        (0xff and (hostAddress shr 16)).toByte(),
        (0xff and (hostAddress shr 24)).toByte()
    )
    return try {
        InetAddress.getByAddress(addressBytes)
    } catch (e: UnknownHostException) {
        throw AssertionError()
    }
}
